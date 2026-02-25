package com.alcadrial.renderengine.render.test.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;

import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanBiConsumer;
import com.alcadrial.renderengine.render.util.functional.VulkanRunnable;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class RenderTest {
	
	protected static final Object MONITOR = new Object();
	private static final String[] SEVERITIES = new String[] {"UNKNOWN", "VERBOSE", "INFO", "WARNING", "ERROR"};
	private static final String[] TYPES = new String[] {"UNKNOWN", "GENERAL", "VALIDATION", "PERFORMANCE"};
	private static final AtomicBoolean hasValidationError = new AtomicBoolean(false);
	
	public static void mainTest(String[] args, VulkanRunnable start, VulkanRunnable loop, VulkanRunnable end, boolean multithreaded)
	{
		try
		{
			TestProperties.init(args);
			//Properties.DEVICE_DESTRUCTION_LIST_THRESHOLD.setValue(0);
			
			if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
			
			println("RenderEngine initialization", multithreaded ? RenderTest::initMultiThreadRenderEngine : RenderTest::initRenderEngine, TestProperties.PRINT_LOADING_TIMES);
			if (!RenderEngine.isInitializated()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
			
			if (Properties.DEBUG.getValue())
			{
				Debug.setEnabledAllocationDebug(true);
				Debug.setEnabledDeallocationFail(true);
			}
			
			Thread mainThread = null;
			
			if (multithreaded)
			{
				mainThread = Thread.ofPlatform().name("Main").start(RenderEngine::serveRenderEngine);
			}
			
			try
			{
				println("Start", start, TestProperties.PRINT_LOADING_TIMES);
				if (!RenderEngine.isInitializated()) return;
				
				println("Loop", loop, TestProperties.PRINT_LOADING_TIMES);
				if (!RenderEngine.isInitializated()) return;
				
				println("End", end, TestProperties.PRINT_LOADING_TIMES);
			}
			catch (Throwable t)
			{
				try
				{
					if (RenderEngine.isInitializated()) RenderEngine.terminate();
				}
				catch (Throwable e)
				{
					e.printStackTrace(System.out);
				}
				throw t;
			}
			finally
			{
				//if (RenderEngine.isInitializated()) throw new IllegalStateException("RenderEngine not terminated");
				if (mainThread != null) mainThread.join();
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.out);
		}
	}
	
	public static void defaultLoop(VulkanBiConsumer<Long, Long> action) throws VulkanException
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		String title = window.getTitle();
		
		VulkanException[] lastError = new VulkanException[1];
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
		long firstHighMilestone = 1000000000;
		long firstMilestone = 1000000000 / maxfps;
		if (firstMilestone <= 0) firstMilestone = 1;
		
		long time = System.nanoTime();
		long[] lastCurrent = {time};
		long frame = 0;
		long milestone = firstMilestone;
		long highMilestone = firstHighMilestone;
		
		window.setCallback((GLFWWindowRefreshCallbackI) w -> {
			try
			{
				long current = System.nanoTime();
				long enlapsed = current - time;
				long enlapsedFrame = current - lastCurrent[0];
				action.accept(enlapsed, enlapsedFrame);
			}
			catch (VulkanException e)
			{
				lastError[0] = e;
			}
		});
		
		while (!window.shouldClose() && !hasValidationError.get())
		{
			long current = System.nanoTime();
			long enlapsed = current - time;
			long enlapsedFrame = current - lastCurrent[0];
			long pauseTime = milestone - enlapsed;
			if (pauseTime > 0)
			{
				try
				{
					synchronized (window)
					{
						window.wait(pauseTime / 1000000, (int) (pauseTime % 1000000));
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace(System.out);
				}
				continue;
			}
			milestone += (1 - pauseTime / firstMilestone) * firstMilestone;
			if (enlapsed >= highMilestone)
			{
				window.setTitle(title + " fps=" + frame);
				frame = 0;
				highMilestone += (enlapsed - highMilestone + firstHighMilestone) / firstHighMilestone * firstHighMilestone;
			}
			println("Frame " + frame, () -> action.accept(enlapsed, enlapsedFrame), TestProperties.PRINT_FRAME_TIMES);
			
			window.pollEvents();
			if (lastError[0] != null) throw lastError[0];
			frame++;
			lastCurrent[0] = current;
		}
		window.getDevice().waitIdle();
	}
	
	public static void staticLoop(VulkanBiConsumer<Long, Long> action) throws VulkanException
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		VulkanException[] lastError = new VulkanException[1];
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
		long firstMilestone = 1000000000 / maxfps;
		if (firstMilestone <= 0) firstMilestone = 1;
		
		long time = System.nanoTime();
		long[] lastCurrent = {time};
		long milestone = firstMilestone;
		
		window.setCallback((GLFWWindowRefreshCallbackI) w -> {
			try
			{
				long current = System.nanoTime();
				long enlapsed = current - time;
				long enlapsedFrame = current - lastCurrent[0];
				action.accept(enlapsed, enlapsedFrame);
			}
			catch (VulkanException e)
			{
				lastError[0] = e;
			}
		});
		
		while (!window.shouldClose() && !hasValidationError.get())
		{
			long current = System.nanoTime();
			long enlapsed = current - time;
			long pauseTime = milestone - enlapsed;
			if (pauseTime > 0)
			{
				try
				{
					synchronized (window)
					{
						window.wait(pauseTime / 1000000, (int) (pauseTime % 1000000));
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace(System.out);
				}
				continue;
			}
			milestone += (1 - pauseTime / firstMilestone) * firstMilestone;
			
			window.pollEvents();
			if (lastError[0] != null) throw lastError[0];
			lastCurrent[0] = current;
		}
		window.getDevice().waitIdle();
	}
	
	public static void noEventsloop(VulkanBiConsumer<Long, Long> action) throws VulkanException
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		String title = window.getTitle();
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
		long firstHighMilestone = 1000000000;
		long firstMilestone = 1000000000 / maxfps;
		
		long time = System.nanoTime();
		long lastCurrent = time;
		long frame = 0;
		long milestone = firstMilestone;
		long highMilestone = firstHighMilestone;
		
		while (!window.shouldClose() && !hasValidationError.get())
		{
			long current = System.nanoTime();
			long enlapsed = current - time;
			long enlapsedFrame = current - lastCurrent;
			long pauseTime = milestone - enlapsed;
			if (pauseTime > 0)
			{
				try
				{
					synchronized (window)
					{
						window.wait(pauseTime / 1000000, (int) (pauseTime % 1000000));
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace(System.out);
				}
				continue;
			}
			milestone += (1 - pauseTime / firstMilestone) * firstMilestone;
			if (enlapsed >= highMilestone)
			{
				window.setTitle(title + " fps=" + frame);
				frame = 0;
				highMilestone += (enlapsed - highMilestone + firstHighMilestone) / firstHighMilestone * firstHighMilestone;
			}
			println("Frame " + frame, () -> action.accept(enlapsed, enlapsedFrame), TestProperties.PRINT_FRAME_TIMES);
			
			frame++;
			lastCurrent = current;
		}
		window.getDevice().waitIdle();
	}
	
	public static void defaultAnimationLoop(VulkanBiConsumer<Long, Long> action) throws VulkanException
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		String title = window.getTitle();
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		VulkanException[] lastError = new VulkanException[1];
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
		long period = 1000000000 / maxfps;
		if (period <= 0) period = 1;

		long firstHighMilestone = 1000000000;
		long time = System.nanoTime();
		long[] lastCurrent = {time};
		long[] frame = { 0 };
		long[] highMilestone = {firstHighMilestone};
		
		service.scheduleAtFixedRate(() -> {
			try
			{
				long current = System.nanoTime();
				long enlapsed = current - time;
				long enlapsedFrame = current - lastCurrent[0];
				if (enlapsed >= highMilestone[0])
				{
					window.setTitle(title + " fps=" + frame);
					frame[0] = 0;
					highMilestone[0] += (enlapsed - highMilestone[0] + firstHighMilestone) / firstHighMilestone * firstHighMilestone;
				}
				println("Frame " + frame, () -> action.accept(enlapsed, enlapsedFrame), TestProperties.PRINT_FRAME_TIMES);
				frame[0]++;
				lastCurrent[0] = current;
			}
			catch (VulkanException e)
			{
				lastError[0] = e;
				service.shutdown();
				window.setShouldClose(true);
			}
		}, 0, period, TimeUnit.NANOSECONDS);
		
		while (!window.shouldClose() && !hasValidationError.get())
		{
			window.pollEvents();
		}
		try
		{
			service.shutdown();
			if (!service.awaitTermination(500, TimeUnit.MILLISECONDS)) service.shutdownNow();
		}
		catch (InterruptedException e)
		{
			service.shutdownNow();
			return;
		}
		
		window.getDevice().waitIdle();
	}
	
	public static void initRenderEngine() throws VulkanException
	{
		RenderEngine.Builder builder = RenderEngine.create("Test", new Version(0, 0, 1, 0));
		builder.messengerCallback(RenderTest::debugCallback);
		builder.instanceLayersDebug("VK_LAYER_KHRONOS_validation");
		builder.instanceExtensionsDebug(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
		builder.multiThreading(false);
		builder.useWindowSystem(true);
		builder.initialize();
	}
	
	public static void initMultiThreadRenderEngine() throws VulkanException
	{
		RenderEngine.Builder builder = RenderEngine.create("Test", new Version(0, 0, 1, 0));
		builder.messengerCallback(RenderTest::debugCallback);
		builder.instanceLayersDebug("VK_LAYER_KHRONOS_validation");
		builder.instanceExtensionsDebug(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
		builder.multiThreading(true);
		builder.useWindowSystem(true);
		builder.initialize();
	}
	
	public static int debugCallback(int messageSeverity, int messageType, long callbackDataId, long userDataId)
	{
		VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataId);
		
		int i = switch (messageSeverity)
		{
			case 0x0001 -> 1;
			case 0x0010 -> 2;
			case 0x0100 -> 3;
			case 0x1000 -> 4;
			default -> 0;
		};
		int j = switch (messageType)
		{
			case 0b001 -> 1;
			case 0b010 -> 2;
			case 0b100 -> 3;
			default -> 0;
		};
		if (messageSeverity == EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
		{
			hasValidationError.set(true);
		}
		
		Debug.debug("[" + SEVERITIES[i] + " " + TYPES[j] + "] " + callbackData.pMessageString());
		
		return VK10.VK_FALSE;
	}
	
	public static Texture createDepthTexture(int width, int height) throws VulkanException
	{
		Texture depthTexture = Texture.create2dTexture(RenderEngine.getVulkan().getDevice(), width, height, VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
		depthTexture.changeLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL).join();
		return depthTexture;
	}
	
	public static File getFile(String path)
	{
		return new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/" + path);
	}
	
	public static PreparedShader loadShader(String path) throws IOException
	{
		Shader shader = new Shader(getFile(path));
		ShaderInfo info = ShaderInfo.fromJson(getFile(path + ".json"));
		if (TestProperties.FORCE_RECOMPILE_SHADERS.getValue()) shader.compile(info);
		else
		{
			File binFile = getFile(path + ".bin");
			if (binFile.canRead()) shader.loadCompiled(binFile);
			else shader.compile(info);
		}
		if (!shader.isCompilationSuccessfull()) throw new IOException("Shader " + shader.getFile() + " has errors");
		return new PreparedShader(shader, info);
	}
	
	public static void saveShaderBinary(Shader shader) throws IOException
	{
		Files.write(new File(VulkanErrorUtils.nonNull(shader.getFile()).getPath() + ".bin").toPath(), shader.getLastCompilation().get().binary());
	}
	
	public static void println(String s, VulkanRunnable action, Property<Boolean> printProperty) throws VulkanException
	{
		if (printProperty.getValue()) System.out.println(s + " time = " + recordTime(action, TimeUnit.MICROSECONDS) / 1000D + "ms");
		else action.run();
	}
	
	public static long recordTime(VulkanRunnable runnable, TimeUnit unit) throws VulkanException
	{
		long startTime;
		TimeUnit localUnit;
		if (unit == TimeUnit.MICROSECONDS || unit == TimeUnit.NANOSECONDS)
		{
			startTime = System.nanoTime();
			localUnit = TimeUnit.NANOSECONDS;
		}
		else
		{
			startTime = System.currentTimeMillis();
			localUnit = TimeUnit.MILLISECONDS;
		}
		
		runnable.run();
		
		long endTime;
		if (localUnit == TimeUnit.NANOSECONDS) endTime = System.nanoTime();
		else endTime = System.currentTimeMillis();
		
		return unit.convert(endTime - startTime, localUnit);
	}
	
	public static double recordTime(VulkanRunnable runnable, TimeUnit unit, TimeUnit resolution) throws VulkanException
	{
		long startTime;
		TimeUnit localUnit;
		if (resolution == TimeUnit.MICROSECONDS || resolution == TimeUnit.NANOSECONDS)
		{
			startTime = System.nanoTime();
			localUnit = TimeUnit.NANOSECONDS;
		}
		else
		{
			startTime = System.currentTimeMillis();
			localUnit = TimeUnit.MILLISECONDS;
		}
		
		runnable.run();
		
		long endTime;
		if (localUnit == TimeUnit.NANOSECONDS) endTime = System.nanoTime();
		else endTime = System.currentTimeMillis();
		
		return (double) resolution.convert(endTime - startTime, localUnit) / resolution.convert(1, unit);
	}
	
	public static abstract class Logic {
		
		public abstract void load() throws IOException;
		public abstract void start() throws VulkanException;
		public abstract void loop() throws VulkanException;
		public abstract void end() throws VulkanException;
	}
}
