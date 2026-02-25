package com.alcadrial.renderengine.render;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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

import com.alcadrial.renderengine.core.RenderEngineCore;
import com.alcadrial.renderengine.render.util.DebugMessengerSeverity;
import com.alcadrial.renderengine.render.util.DebugMessengerType;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanFuture;
import com.alcadrial.renderengine.render.util.exception.ExternalException;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanConsumer;
import com.alcadrial.renderengine.render.util.functional.VulkanFunction;
import com.alcadrial.renderengine.render.util.functional.VulkanRunnable;
import com.alcadrial.renderengine.render.util.functional.VulkanSupplier;

public class RenderEngine extends RenderEngineCore {
	
	private static final Lock LOCK = new ReentrantLock();
	
	private static volatile List<String> instanceLayers = List.of();
	private static volatile List<String> instanceExtensions = List.of();
	
	private static volatile boolean debug = false;
	private static volatile boolean initializated = false;
	
	private static volatile @Nullable Exception initException = null;
	private static volatile @Nullable Vulkan vulkan = null;
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
		if (!initializated && (builder.useWindowSystem ? GLFW.glfwInit() : true)) initVulkan(builder);
	}
	
	private static void initVulkan(Builder builder) throws VulkanException
	{
		LOCK.lock();
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
		finally
		{
			LOCK.unlock();
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
		VulkanFuture<?> f = null;
		LOCK.lock();
		try
		{
			if (initializated)
			{
				((@NonNull Vulkan) vulkan).close();
				if (executor != null) executor.shutdown();
				vulkan = null;
				f = requestMainThread(() -> {
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
						executor = null;
					}
					mainThreadActions = null;
					initializated = false;
				});
			}
		}
		finally
		{
			LOCK.unlock();
		}
		if (f != null) while (true)
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
	
	public static @Nullable Exception getLastInitializationException()
	{
		return initException;
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
	
	public static VulkanFuture<?> completedTask()
	{
		return new CompletedTaskHandler<>(null);
	}
	
	public static <V> VulkanFuture<V> completedTask(V value)
	{
		return new CompletedTaskHandler<>(value);
	}
	
	public static CompletableTaskHandler<?> completableTask()
	{
		return new CompletableTaskHandler<>();
	}
	
	public static VulkanFuture<?> submitAsync(VulkanRunnable action)
	{
		return new AsyncRunnableTaskHandler(action);
	}
	
	public static <V> VulkanFuture<V> submitAsync(Callable<V> action)
	{
		return new AsyncSupplierTaskHandler<>(action);
	}
	
	public static VulkanFuture<?> composeAfter(VulkanFuture<?> future, VulkanRunnable action)
	{
		return new AsyncDependentTaskHandler<>(future, v -> {
			action.run();
			return null;
		});
	}
	
	public static <V> VulkanFuture<V> composeAfter(VulkanFuture<?> future, VulkanSupplier<V> action)
	{
		return new AsyncDependentTaskHandler<>(future, v -> action.get());
	}
	
	public static <V> VulkanFuture<?> composeAfter(VulkanFuture<V> future, VulkanConsumer<V> action)
	{
		return new AsyncDependentTaskHandler<>(future, v -> {
			action.accept(v);
			return null;
		});
	}
	
	public static <V, W> VulkanFuture<W> composeAfter(VulkanFuture<V> future, VulkanFunction<V, W> action)
	{
		return new AsyncDependentTaskHandler<>(future, action);
	}
	
	private static class CompletedTaskHandler<V> implements VulkanFuture<V> {
		
		private V value;
		
		public CompletedTaskHandler(V value)
		{
			this.value = value;
		}
		
		@Override
		public void await() throws InterruptedException
		{}
		
		@Override
		public V get() throws VulkanException, InterruptedException
		{
			return value;
		}
	}
	
	public static class CompletableTaskHandler<V> implements VulkanFuture<V> {
		
		private CountDownLatch termination;
		@Nullable
		private VulkanException exception;
		@Nullable
		private V value;
		private boolean isCompleted;
		
		public CompletableTaskHandler()
		{
			termination = new CountDownLatch(1);
			isCompleted = false;
		}
		
		public void complete(V value)
		{
			if (!isCompleted)
			{
				this.value = value;
				termination.countDown();
			}
		}
		
		public void throwComplete(VulkanException exception)
		{
			if (!isCompleted)
			{
				this.exception = exception;
				termination.countDown();
			}
		}
		
		@Override
		public void await() throws InterruptedException
		{
			if (!isCompleted) termination.await();
		}
		
		@Override
		public V get() throws VulkanException, InterruptedException
		{
			await();
			if (exception != null) throw exception;
			return value;
		}
	}
	
	private static abstract class AsyncTaskHandler<V> implements VulkanFuture<V> {
		
		private CountDownLatch termination;
		@Nullable
		private V result;
		@Nullable
		private VulkanException exception;
		private StackTraceElement[] callerStackTrace;
		
		public AsyncTaskHandler()
		{
			termination = new CountDownLatch(1);
			result = null;
			callerStackTrace = new Exception().getStackTrace();
		}
		
		protected void start()
		{
			if (RenderEngine.debug) Thread.ofVirtual().name("Task thread").start(this::run);
			else Thread.startVirtualThread(this::run);
		}
		
		protected abstract V execute() throws VulkanException;
		
		private void run()
		{
			try
			{
				result = execute();
			}
			catch (VulkanException e)
			{
				StackTraceElement[] errorStackTrace = e.getStackTrace();
				StackTraceElement[] stackTrace = new StackTraceElement[errorStackTrace.length + callerStackTrace.length - 2];
				System.arraycopy(errorStackTrace, 0, stackTrace, 0, errorStackTrace.length);
				System.arraycopy(callerStackTrace, 2, stackTrace, errorStackTrace.length, callerStackTrace.length - 2);
				e.setStackTrace(stackTrace);
				e.printStackTrace();
				exception = e;
			}
			catch (Throwable e)
			{
				VulkanException exception = new ExternalException(e);
				StackTraceElement[] stackTrace = new StackTraceElement[callerStackTrace.length - 2];
				System.arraycopy(callerStackTrace, 2, stackTrace, 0, callerStackTrace.length - 2);
				exception.setStackTrace(stackTrace);
				exception.printStackTrace();
				this.exception = exception;
			}
			finally
			{
				termination.countDown();
			}
		}
		
		@Override
		public void await() throws InterruptedException
		{
			termination.await();
		}
		
		@Override
		public V get() throws VulkanException, InterruptedException
		{
			await();
			if (exception != null) throw exception;
			return result;
		}
	}
	
	private static class AsyncRunnableTaskHandler extends AsyncTaskHandler<@Nullable Void> {
		
		private VulkanRunnable action;
		
		public AsyncRunnableTaskHandler(VulkanRunnable action)
		{
			this.action = action;
			start();
		}
		
		@Override
		protected @Nullable Void execute() throws VulkanException
		{
			action.run();
			return null;
		}
	}
	
	private static class AsyncSupplierTaskHandler<V> extends AsyncTaskHandler<V> {
		
		private Callable<V> action;
		
		public AsyncSupplierTaskHandler(Callable<V> action)
		{
			this.action = action;
			start();
		}
		
		@Override
		protected V execute() throws VulkanException
		{
			try
			{
				return action.call();
			}
			catch (Exception e)
			{
				throw new ExternalException(e);
			}
		}
	}
	
	private static class AsyncDependentTaskHandler<V, W> extends AsyncTaskHandler<W> {
		
		private VulkanFuture<V> other;
		private VulkanFunction<V, W> action;
		
		public AsyncDependentTaskHandler(VulkanFuture<V> other, VulkanFunction<V, W> action)
		{
			this.other = other;
			this.action = action;
			start();
		}
		
		@Override
		protected W execute() throws VulkanException
		{
			other.join();
			return action.apply(other.forceGet());
		}
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
					throw (RuntimeException) e.getCause();
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
			CompletableTaskHandler<?> f = completableTask();
			getMainThreadActions().add(() -> {
				action.run();
				f.complete(null);
			});
			return f;
		}
		else
		{
			action.run();
			return completedTask();
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
						getMainThreadActions().take().run();
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
	
	/**
	 * Empties the queue of action and then returns.
	 */
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
				LinkedBlockingQueue<Runnable> actions = getMainThreadActions();
				while (initializated && actions.peek() != null) actions.poll().run();
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
			RenderEngine.initialize(this);
		}
	}
}
