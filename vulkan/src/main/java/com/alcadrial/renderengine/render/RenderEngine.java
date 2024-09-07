package com.alcadrial.renderengine.render;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import com.alcadrial.kurvilinear.SequentialMultiList;
import com.alcadrial.renderengine.core.RenderEngineCore;
import com.alcadrial.renderengine.render.util.Version;

public class RenderEngine extends RenderEngineCore {
	
	private static final Lock LOCK = new ReentrantLock();
	
	private static volatile SequentialMultiList<String> instanceLayers = new SequentialMultiList<>();
	private static volatile SequentialMultiList<String> instanceExtensions = new SequentialMultiList<>();
	
	private static volatile boolean debug = false;
	private static volatile boolean initializated = false;
	private static volatile Exception initException = null;
	private static volatile Vulkan vulkan = null;
	private static volatile ExecutorService executor = null;
	private static volatile boolean multiThreading = false;
	private static volatile Thread mainThread = null;
	private static volatile LinkedBlockingQueue<Runnable> mainThreadActions = null;
	
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
	 * @return true if the library is initialized, false otherwise
	 * @see #create(String, Version)
	 * 
	 */
	public static boolean initialize(String applicationName, Version version)
	{
		return new Builder(applicationName, version).initialize();
	}
	
	private static boolean initialize(Builder builder)
	{
		if (!initializated) return (builder.useWindowSystem ? GLFW.glfwInit() : true) && initVulkan(builder);
		return true;
	}
	
	private static boolean initVulkan(Builder builder)
	{
		LOCK.lock();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			mainThreadActions = new LinkedBlockingQueue<>();
			multiThreading = builder.multiThreading;
			instanceLayers = builder.instanceLayers;
			if (instanceLayers.listsSize() == 0) instanceLayers.addList();
			instanceExtensions = builder.instanceExtensions;
			if (instanceExtensions.listsSize() == 0) instanceExtensions.addList();
			
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
			
			{
				PointerBuffer requiredInstances = builder.useWindowSystem ? GLFWVulkan.glfwGetRequiredInstanceExtensions() : null;
				
				int size = debug ? instanceExtensions.size() : instanceExtensions.size() - instanceExtensions.getList(0).size();
				PointerBuffer enlargedBuffer;
				
				if (requiredInstances == null)
				{
					enlargedBuffer = stack.mallocPointer(size);
				}
				else
				{
					enlargedBuffer = stack.mallocPointer(size + requiredInstances.remaining());
					while (requiredInstances.remaining() > 0) enlargedBuffer.put(requiredInstances.get());
				}
				if (debug) for (String name : instanceExtensions) enlargedBuffer.put(stack.UTF8(name));
				else for (String name : instanceExtensions.subLists(1, instanceExtensions.listsSize())) enlargedBuffer.put(stack.UTF8(name));
				enlargedBuffer.rewind();
				
				createInfo.ppEnabledExtensionNames(enlargedBuffer);
				createInfo.ppEnabledLayerNames(getInstanceLayers(stack));
				
				if (builder.messengerCallback != null)
				{
					VkDebugUtilsMessengerCreateInfoEXT messengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
					VkDebugUtilsMessengerCallbackEXTI callback = builder.messengerCallback;
					
					messengerCreateInfo.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
					messengerCreateInfo.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT);
					messengerCreateInfo.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
					messengerCreateInfo.pfnUserCallback((VkDebugUtilsMessengerCallbackEXTI) (messageSeverity, messageType, callbackDataId, userDataId) -> {
						if (debug) return callback.invoke(messageSeverity, messageType, callbackDataId, userDataId);
						return VK10.VK_FALSE;
					});
					
					createInfo.pNext(messengerCreateInfo.address());
				}
			}
			initializated = true;
			vulkan = new Vulkan(createInfo);
			executor = Executors.newCachedThreadPool();
			return true;
		}
		catch (Exception e)
		{
			mainThreadActions = null;
			initException = e;
			initializated = false;
			return false;
		}
		finally
		{
			LOCK.unlock();
		}
	}
	
	static PointerBuffer getInstanceLayers(MemoryStack stack)
	{
		PointerBuffer array = stack.mallocPointer(debug ? instanceLayers.size() : instanceLayers.size() - instanceLayers.getList(0).size());
		for (String string : debug ? instanceLayers : instanceLayers.subLists(1, instanceLayers.listsSize())) array.put(stack.UTF8(string));
		return array.rewind();
	}
	
	/**
	 * <b>MUST BE CALLED ONLY AFTER ANY THREAD HAS FINISHED USING THE LIBRARY.</b>
	 * <p>
	 * After calling terminate, {@link #initialize} may be called again.
	 */
	public static void terminate()
	{
		LOCK.lock();
		try
		{
			if (initializated)
			{
				if (executor != null) executor.shutdown();
				vulkan.close();
				vulkan = null;
				Future<?> f = requestMainThread(() -> {
					GLFW.glfwTerminate();
					if (executor != null)
					{
						try
						{
							executor.awaitTermination(1, TimeUnit.MILLISECONDS);
						}
						catch (InterruptedException e)
						{}
						executor.shutdownNow();
						executor = null;
					}
					mainThreadActions = null;
					initializated = false;
				});
				while (!f.isDone())
				{
					try
					{
						f.get();
					}
					catch (InterruptedException e)
					{}
					catch (ExecutionException e)
					{
						throw (RuntimeException) e.getCause();
					}
				}
			}
		}
		finally
		{
			LOCK.unlock();
		}
	}
	
	public static boolean isInitializated()
	{
		return initializated;
	}
	
	public static void assertLoaded()
	{
		if (!initializated) throw new RuntimeException("Using RenderEngine Vulkan without initialization, use RenderEngine#initialize before anything");
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
	
	public static Exception getLastInitializationException()
	{
		return initException;
	}
	
	public static Vulkan getVulkan()
	{
		return vulkan;
	}
	
	public static ExecutorService getExecutor()
	{
		return executor;
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
			mainThreadActions.add(() -> {
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
					throw (RuntimeException) e.getCause();
				}
			}
			if (interrupted) Thread.currentThread().interrupt();
		}
		else action.run();
	}
	
	public static Future<?> requestMainThread(Runnable action)
	{
		assertLoaded();
		CompletableFuture<?> f = new CompletableFuture<>();
		if (multiThreading && mainThread != Thread.currentThread())
		{
			mainThreadActions.add(() -> {
				action.run();
				f.complete(null);
			});
		}
		else
		{
			action.run();
			f.complete(null);
		}
		return f;
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
		mainThreadActions.add(() -> f.complete(action.get()));
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
					throw (RuntimeException) e.getCause();
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
		if (multiThreading && mainThread != Thread.currentThread()) mainThreadActions.add(() -> f.complete(action.get()));
		else f.complete(action.get());
		return f;
	}
	
	public static void serveRenderEngine()
	{
		assertLoaded();
		LOCK.lock();
		try
		{
			if (multiThreading)
			{
				if (mainThread != null) throw new DuplicateMainThreadException();
				mainThread = Thread.currentThread();
				while (initializated)
				{
					try
					{
						LOCK.unlock();
						mainThreadActions.take().run();
						LOCK.lock();
					}
					catch (InterruptedException e)
					{}
				}
				mainThread = null;
			}
		}
		finally
		{
			LOCK.unlock();
		}
	}
	
	public static void serveOnceRenderEngine()
	{
		assertLoaded();
		LOCK.lock();
		try
		{
			if (multiThreading)
			{
				if (mainThread != null) throw new DuplicateMainThreadException();
				mainThread = Thread.currentThread();
				while (initializated && mainThreadActions.peek() != null) mainThreadActions.poll().run();
				mainThread = null;
			}
		}
		finally
		{
			LOCK.unlock();
		}
	}
	
	public static class Builder {
		
		private String applicationName;
		private Version version;
		private boolean multiThreading;
		private boolean useWindowSystem;
		private VkDebugUtilsMessengerCallbackEXTI messengerCallback;
		private SequentialMultiList<String> instanceLayers;
		private SequentialMultiList<String> instanceExtensions;
		
		private Builder(String applicationName, Version version)
		{
			this.applicationName = applicationName;
			this.version = version;
			multiThreading = true;
			instanceLayers = new SequentialMultiList<>();
			instanceLayers.addList();
			instanceLayers.addList();
			instanceExtensions = new SequentialMultiList<>();
			instanceExtensions.addList();
			instanceExtensions.addList();
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
			List<String> list = instanceExtensions.getList(1);
			list.clear();
			list.addAll(extensions);
			return this;
		}
		
		public Builder instanceExtensionsDebug(String... extensions)
		{
			return instanceExtensionsDebug(Arrays.asList(extensions));
		}
		
		public Builder instanceExtensionsDebug(Collection<String> extensions)
		{
			if (debug)
			{
				List<String> list = instanceExtensions.getList(0);
				list.clear();
				list.addAll(extensions);
			}
			return this;
		}
		
		public Builder instanceLayers(String... extensions)
		{
			return instanceLayers(Arrays.asList(extensions));
		}
		
		public Builder instanceLayers(Collection<String> extensions)
		{
			List<String> list = instanceLayers.getList(1);
			list.clear();
			list.addAll(extensions);
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
				List<String> list = instanceLayers.getList(0);
				list.clear();
				list.addAll(extensions);
			}
			return this;
		}
		
		public boolean initialize()
		{
			Objects.requireNonNull(applicationName);
			Objects.requireNonNull(version);
			return RenderEngine.initialize(this);
		}
	}
}
