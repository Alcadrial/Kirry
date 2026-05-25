package com.alcadrial.renderengine.render;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.util.renderdoc.RenderDoc;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import com.alcadrial.renderengine.core.RenderEngineCore;
import com.alcadrial.renderengine.render.util.DebugMessengerSeverity;
import com.alcadrial.renderengine.render.util.DebugMessengerType;
import com.alcadrial.renderengine.render.util.FreeTypeErrorUtils;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.future.CompletableTaskHandler;
import com.alcadrial.renderengine.render.util.future.VulkanFuture;

public class RenderEngineVulkan extends RenderEngineCore {
	
	private static volatile List<String> instanceLayers = List.of();
	private static volatile List<String> instanceExtensions = List.of();
	
	private static volatile boolean debug = false;
	private static volatile boolean initializated = false;
	
	private static volatile @Nullable Exception initException = null;
	private static volatile @Nullable Vulkan vulkan = null;
	private static volatile long ftLibrary = 0;
	private static volatile @Nullable ExecutorService executor = null;
	private static volatile boolean multiThreading = false;
	private static volatile @Nullable Thread mainThread = null;
	private static volatile @Nullable LinkedBlockingQueue<Runnable> mainThreadActions = null;
	
	/**
	 * Creates a builder that can be used to choose various aspects of the engine.
	 * <p>
	 * After all the data have been set, a call to {@link Builder#initialize()} will 
	 * initialize the engine using the values of the builder.
	 * <p>
	 * A builder can be used infinite times.
	 * 
	 * @param applicationName 
	 * @param version 
	 * @return a new {@link Builder}
	 * @see #initialize(String, Version)
	 */
	public static Builder create(String applicationName, Version version)
	{
		return new Builder(applicationName, version);
	}
	
	/**
	 * Initialize the library and return true if the library results as initialized after calling this method.
	 * <p>
	 * In order To terminate and release any resource allocated by the library, the function {@link #terminate()} must be called.
	 * <p>
	 * After a call to terminate(), calling this function will re-initialize the library as of the first time.
	 * 
	 * @param applicationName 
	 * @param version 
	 * @throws VulkanException 
	 * @see #create(String, Version)
	 * 
	 */
	public static void initialize(String applicationName, Version version) throws VulkanException
	{
		new Builder(applicationName, version).initialize();
	}
	
	private static void initialize(Builder builder) throws VulkanException
	{
		if (initializated) return;
		if (builder.useWindowSystem && !GLFW.glfwInit())
		{
			GLFW.glfwTerminate();
			if (!GLFW.glfwInit())
			{
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					PointerBuffer p = stack.mallocPointer(1);
					GLFW.glfwGetError(p);
					throw new VulkanException("Failed to init GLFW: " + p.getStringUTF8());
				}
			}
		}
		if (builder.useFont)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				PointerBuffer p = stack.mallocPointer(1);
				int error = FreeType.FT_Init_FreeType(p);
				if (error != 0)
				{
					GLFW.glfwTerminate();
					throw new VulkanException("Failed to init FreeType (" + error + ")");
				}
				ftLibrary = p.get(0);
			}
		}
		if (RenderEngineVulkan.debug)
		{
			try
			{
				RenderDoc.create();
			}
			catch (Exception e)
			{
				System.err.println(e.getLocalizedMessage());
			}
		}
		try
		{
			initVulkan(builder);
		}
		catch (VulkanException e)
		{
			GLFW.glfwTerminate();
			FreeType.FT_Done_Library(ftLibrary);
			ftLibrary = 0;
			throw e;
		}
	}
	
	private static void initVulkan(Builder builder) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			mainThreadActions = new LinkedBlockingQueue<>();
			multiThreading = builder.multiThreading;
			instanceLayers = builder.instanceLayers;
			instanceExtensions = builder.instanceExtensions;
			
			VkApplicationInfo applicationInfo = VkApplicationInfo.calloc(stack);
			
			applicationInfo.sType(VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO);
			applicationInfo.pApplicationName(stack.UTF8Safe(builder.applicationName));
			applicationInfo.applicationVersion(VK10.VK_MAKE_API_VERSION(builder.version.variant(), builder.version.major(), builder.version.minor(), builder.version.patch()));
			applicationInfo.pEngineName(stack.UTF8Safe("RenderEngine"));
			applicationInfo.engineVersion(VK10.VK_MAKE_API_VERSION(0, 0, 1, 0));
			applicationInfo.apiVersion(VK13.VK_API_VERSION_1_3);
			
			VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack);
			
			createInfo.sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
			createInfo.pApplicationInfo(applicationInfo);
			
			boolean debugUtilsExtensionEnabled = false;
			
			{
				PointerBuffer requiredInstances = builder.useWindowSystem ? GLFWVulkan.glfwGetRequiredInstanceExtensions() : null;
				
				PointerBuffer enlargedBuffer;
				
				if (requiredInstances == null)
				{
					enlargedBuffer = stack.mallocPointer(instanceExtensions.size());
				}
				else
				{
					enlargedBuffer = stack.mallocPointer(instanceExtensions.size() + requiredInstances.remaining());
					while (requiredInstances.remaining() > 0) enlargedBuffer.put(requiredInstances.get());
				}
				for (String name : instanceExtensions)
				{
					if (EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME.equals(name)) debugUtilsExtensionEnabled = true;
					enlargedBuffer.put(stack.UTF8(name));
				}
				enlargedBuffer.rewind();
				
				createInfo.ppEnabledExtensionNames(enlargedBuffer);
				createInfo.ppEnabledLayerNames(getInstanceLayers(stack));
				
				VkDebugUtilsMessengerCallbackEXTI messengerCallback = builder.messengerCallback;
				if (messengerCallback != null)
				{
					VkDebugUtilsMessengerCreateInfoEXT messengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
					int severity = 0;
					int type = 0;
					for (DebugMessengerSeverity s : builder.messengerSeverities) severity |= s.getBit();
					for (DebugMessengerType t : builder.messengerTypes) type |= t.getBit();
					
					messengerCreateInfo.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
					messengerCreateInfo.messageSeverity(severity);
					messengerCreateInfo.messageType(type);
					messengerCreateInfo.pfnUserCallback((VkDebugUtilsMessengerCallbackEXTI) (messageSeverity, messageType, callbackDataId, userDataId) -> {
						if (debug) return messengerCallback.invoke(messageSeverity, messageType, callbackDataId, userDataId);
						return VK10.VK_FALSE;
					});
					
					createInfo.pNext(messengerCreateInfo.address());
				}
			}
			initializated = true;
			vulkan = new Vulkan(createInfo, debugUtilsExtensionEnabled);
			executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("VirtualTask-", 0).factory());
		}
		catch (Exception e)
		{
			mainThreadActions = null;
			initException = e;
			initializated = false;
			if (e instanceof VulkanException ve) throw ve;
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		}
	}
	
	static PointerBuffer getInstanceLayers(MemoryStack stack)
	{
		PointerBuffer array = stack.mallocPointer(instanceLayers.size());
		for (String string : instanceLayers) array.put(stack.UTF8(string));
		return array.rewind();
	}
	
	/**
	 * <b>MUST BE CALLED ONLY AFTER ANY THREAD HAS FINISHED USING THE LIBRARY.</b>
	 * <p>
	 * After calling terminate, {@link #initialize} may be called again.
	 */
	public static void terminate()
	{
		if (initializated)
		{
			Debug.debug("Terminating RenderEngine");
			if (executor != null) executor.shutdown();
			((@NonNull Vulkan) vulkan).close();
			vulkan = null;
			if (ftLibrary != 0)
			{
				FreeType.FT_Done_Library(ftLibrary);
				ftLibrary = 0;
			}
			VulkanFuture<?> f = requestMainThread(() -> {
				GLFW.glfwTerminate();
				if (executor != null)
				{
					try
					{
						executor.awaitTermination(1, TimeUnit.MILLISECONDS);
					}
					catch (InterruptedException e)
					{}
					((@NonNull ExecutorService) executor).shutdownNow();
					Debug.debug("Terminated RenderEngine");
					executor = null;
				}
				getMainThreadActions().add(() -> Thread.currentThread().interrupt());
				mainThreadActions = null;
				initializated = false;
			});
			while (true)
			{
				try
				{
					f.get();
					break;
				}
				catch (InterruptedException e)
				{}
				catch (VulkanException e)
				{
					throw new IllegalStateException(e);
				}
			}
		}
	}
	
	public static boolean isInitializated()
	{
		return initializated;
	}
	
	public static void assertLoaded()
	{
		if (!initializated) throw new RuntimeException(initializationErrorMessage());
	}
	
	public static String initializationErrorMessage()
	{
		return "Using RenderEngine Vulkan without initialization, use RenderEngine#initialize before anything";
	}
	
	public static String initializationFTErrorMessage()
	{
		return "To use font-related features RenderEngine.Builder#useFont must be set to true before initialization";
	}
	
	public static void setWindowHint(int hint, boolean value)
	{
		assertLoaded();
		requestMainThread(() -> GLFW.glfwWindowHint(hint, value ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE));
	}
	
	public static void setWindowHint(int hint, int value)
	{
		assertLoaded();
		requestMainThread(() -> GLFW.glfwWindowHint(hint, value));
	}
	
	public static boolean isMultiThreading()
	{
		return multiThreading;
	}
	
	public static boolean isDebug()
	{
		return debug;
	}
	
	public static void enableDebug()
	{
		debug = true;
	}
	
	public static void disableDebug()
	{
		debug = false;
	}
	
	public static @Nullable Exception getLastInitializationException()
	{
		return initException;
	}
	
	public static long getFTLibrary()
	{
		return FreeTypeErrorUtils.nonNull(ftLibrary, initializationFTErrorMessage());
	}
	
	public static Vulkan getVulkan()
	{
		return VulkanErrorUtils.nonNull(vulkan, initializationErrorMessage());
	}
	
	public static @Nullable Vulkan getUnsafeVulkan()
	{
		return vulkan;
	}
	
	public static ExecutorService getExecutor()
	{
		return Objects.requireNonNull(executor, initializationErrorMessage());
	}
	
	private static LinkedBlockingQueue<Runnable> getMainThreadActions()
	{
		return (@NonNull LinkedBlockingQueue<Runnable>) mainThreadActions;
	}
	
	public static void setDebugOutput(PrintStream debugOutput)
	{
		Debug.setDebugOutput(debugOutput);
	}
	
	public static void awaitRequestMainThread(Runnable action)
	{
		assertLoaded();
		if (multiThreading && mainThread != Thread.currentThread())
		{
			CompletableFuture<?> f = new CompletableFuture<>();
			getMainThreadActions().add(() -> {
				action.run();
				f.complete(null);
			});
			boolean interrupted = false;
			while (!f.isDone())
			{
				try
				{
					f.get();
				}
				catch (InterruptedException e)
				{
					interrupted = true;
				}
				catch (ExecutionException e)
				{
					throw (@NonNull RuntimeException) e.getCause();
				}
			}
			if (interrupted) Thread.currentThread().interrupt();
		}
		else action.run();
	}
	
	public static VulkanFuture<?> requestMainThread(Runnable action)
	{
		assertLoaded();
		if (multiThreading && mainThread != Thread.currentThread())
		{
			CompletableTaskHandler<?> f = VulkanFuture.completableTask();
			getMainThreadActions().add(() -> {
				action.run();
				f.complete(null);
			});
			return f;
		}
		else
		{
			action.run();
			return VulkanFuture.completedTask();
		}
	}
	
	public static boolean awaitRequestMainThread(BooleanSupplier action)
	{
		return awaitRequestMainThread((Supplier<Boolean>) action::getAsBoolean);
	}
	
	public static int awaitRequestMainThread(IntSupplier action)
	{
		return awaitRequestMainThread((Supplier<Integer>) action::getAsInt);
	}
	
	public static long awaitRequestMainThread(LongSupplier action)
	{
		return awaitRequestMainThread((Supplier<Long>) action::getAsLong);
	}
	
	public static double awaitRequestMainThread(DoubleSupplier action)
	{
		return awaitRequestMainThread((Supplier<Double>) action::getAsDouble);
	}
	
	public static <T> T awaitRequestMainThread(Supplier<T> action)
	{
		assertLoaded();
		if (!multiThreading || mainThread == Thread.currentThread()) return action.get();
		
		CompletableFuture<T> f = new CompletableFuture<>();
		getMainThreadActions().add(() -> f.complete(action.get()));
		boolean interrupted = false;
		try
		{
			do
			{
				try
				{
					return f.get();
				}
				catch (InterruptedException e)
				{
					interrupted = true;
				}
				catch (ExecutionException e)
				{
					throw (@NonNull RuntimeException) e.getCause();
				}
			} while (!f.isDone());
			try
			{
				return f.get();
			}
			catch (Exception e)
			{
				throw new IllegalStateException();
			}
		}
		finally
		{
			if (interrupted) Thread.currentThread().interrupt();
		}
	}
	
	public static <T> Future<T> requestMainThread(Supplier<T> action)
	{
		assertLoaded();
		CompletableFuture<T> f = new CompletableFuture<>();
		if (multiThreading && mainThread != Thread.currentThread()) getMainThreadActions().add(() -> f.complete(action.get()));
		else f.complete(action.get());
		return f;
	}
	
	/**
	 * This function return only after a call to {@link #terminate()} from another thread.
	 */
	public static void serveRenderEngine()
	{
		assertLoaded();
		if (multiThreading)
		{
			if (mainThread != null) throw new DuplicateMainThreadException();
			mainThread = Thread.currentThread();
			while (initializated)
			{
				try
				{
					for (LinkedBlockingQueue<Runnable> mainThreadActions = getMainThreadActions();;) mainThreadActions.take().run();
				}
				catch (InterruptedException e)
				{}
			}
			mainThread = null;
		}
	}
	
	/**
	 * Empties the queue of action and then returns.
	 */
	public static void serveOnceRenderEngine()
	{
		assertLoaded();
		if (multiThreading)
		{
			if (mainThread != null) throw new DuplicateMainThreadException();
			mainThread = Thread.currentThread();
			LinkedBlockingQueue<Runnable> actions = getMainThreadActions();
			while (initializated && actions.peek() != null) ((@NonNull Runnable) actions.poll()).run();
			mainThread = null;
		}
	}
	
	public static class Builder {
		
		private String applicationName;
		private Version version;
		private boolean multiThreading;
		private boolean useWindowSystem;
		private boolean useFont;
		private @Nullable VkDebugUtilsMessengerCallbackEXTI messengerCallback;
		private EnumSet<DebugMessengerSeverity> messengerSeverities;
		private EnumSet<DebugMessengerType> messengerTypes;
		private List<String> instanceLayers;
		private List<String> instanceExtensions;
		private int debugLayersCount;
		private int debugExtensionsCount;
		
		private Builder(String applicationName, Version version)
		{
			this.applicationName = applicationName;
			this.version = version;
			multiThreading = true;
			useWindowSystem = false;
			useFont = false;
			instanceLayers = new LinkedList<>();
			debugLayersCount = 0;
			instanceExtensions = new LinkedList<>();
			debugExtensionsCount = 0;
			messengerSeverities = EnumSet.range(DebugMessengerSeverity.ERROR, DebugMessengerSeverity.INFO);
			messengerTypes = EnumSet.range(DebugMessengerType.PERFORMANCE, DebugMessengerType.VALIDATION);
		}
		
		public Builder applicationName(String applicationName)
		{
			this.applicationName = applicationName;
			return this;
		}
		
		public Builder version(Version version)
		{
			this.version = version;
			return this;
		}
		
		public Builder multiThreading(boolean multiThreading)
		{
			this.multiThreading = multiThreading;
			return this;
		}
		
		public Builder useWindowSystem(boolean useWindowSystem)
		{
			this.useWindowSystem = useWindowSystem;
			return this;
		}
		
		public Builder useFont(boolean useFont)
		{
			this.useFont = useFont;
			return this;
		}
		
		public Builder messengerCallback(VkDebugUtilsMessengerCallbackEXTI messengerCallback)
		{
			this.messengerCallback = messengerCallback;
			return this;
		}
		
		public Builder instanceExtensions(String... extensions)
		{
			return instanceExtensions(Arrays.asList(extensions));
		}
		
		public Builder instanceExtensions(Collection<String> extensions)
		{
			for (int i = debugExtensionsCount; i < instanceExtensions.size(); i++) instanceExtensions.removeFirst();
			instanceExtensions.addAll(0, extensions);
			return this;
		}
		
		public Builder instanceExtensionsDebug(String... extensions)
		{
			if (debug) instanceExtensionsDebug(Arrays.asList(extensions));
			return this;
		}
		
		public Builder instanceExtensionsDebug(Collection<String> extensions)
		{
			if (debug)
			{
				for (int i = 0; i < debugExtensionsCount; i++) instanceExtensions.removeLast();
				instanceExtensions.addAll(extensions);
				debugExtensionsCount = extensions.size();
			}
			return this;
		}
		
		public Builder instanceLayers(String... extensions)
		{
			return instanceLayers(Arrays.asList(extensions));
		}
		
		public Builder instanceLayers(Collection<String> extensions)
		{
			for (int i = debugLayersCount; i < instanceLayers.size(); i++) instanceLayers.removeFirst();
			instanceLayers.addAll(0, extensions);
			return this;
		}
		
		public Builder instanceLayersDebug(String... extensions)
		{
			return instanceLayersDebug(Arrays.asList(extensions));
		}
		
		public Builder instanceLayersDebug(Collection<String> extensions)
		{
			if (debug)
			{
				for (int i = 0; i < debugLayersCount; i++) instanceLayers.removeLast();
				instanceLayers.addAll(extensions);
				debugLayersCount = extensions.size();
			}
			return this;
		}
		
		public Builder messengerSeverities(DebugMessengerSeverity... severities)
		{
			if (debug) Collections.addAll(messengerSeverities, severities);
			return this;
		}
		
		public Builder messengerTypes(DebugMessengerType... types)
		{
			if (debug) Collections.addAll(messengerTypes, types);
			return this;
		}
		
		public void initialize() throws VulkanException
		{
			RenderEngineVulkan.initialize(this);
		}
	}
}
