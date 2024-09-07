package com.alcadrial.renderengine.render.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.lwjgl.vulkan.EXTDebugReport;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;

import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.PhysicalDeviceNotFoundException;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class RenderTest {
	
	protected static final Object MONITOR = new Object();
	private static final String[] SEVERITIES = new String[] {"UNKNOWN", "VERBOSE", "INFO", "WARNING", "ERROR"};
	private static final String[] TYPES = new String[] {"UNKNOWN", "GENERAL", "VALIDATION", "PERFORMANCE"};
	
	public static void mainTest(String[] args, Runnable start, Runnable loop, Runnable end)
	{
		TestProperties.init(args);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		println("RenderEngine initialization", RenderTest::initRenderEngine, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		
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
			if (RenderEngine.isInitializated()) RenderEngine.terminate();
			throw t;
		}
	}
	
	public static void defaultLoop(BiConsumer<Long, Long> action)
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		String title = window.getTitle();
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
		long firstHighMilestone = 1000000000;
		long firstMilestone = 1000000000 / maxfps;
		
		long time = System.nanoTime();
		long current;
		long lastCurrent = time;
		long frame = 0;
		long milestone = firstMilestone;
		long highMilestone = firstHighMilestone;
		
		while (!window.shouldClose())
		{
			current = System.nanoTime();
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
	}
	
	public static void initRenderEngine()
	{
		RenderEngine.Builder builder = RenderEngine.create("Test", new Version(0, 0, 1, 0));
		builder.messengerCallback(RenderTest::debugCallback);
		builder.instanceLayersDebug("VK_LAYER_KHRONOS_validation");
		builder.instanceExtensionsDebug(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME, EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
		builder.multiThreading(false);
		builder.useWindowSystem(true);
		builder.initialize();
	}
	
	public static void initMultiThreadRenderEngine()
	{
		RenderEngine.Builder builder = RenderEngine.create("Test", new Version(0, 0, 1, 0));
		builder.messengerCallback(RenderTest::debugCallback);
		builder.instanceLayersDebug("VK_LAYER_KHRONOS_validation");
		builder.instanceExtensionsDebug(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME, EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
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
		
		Debug.debug("[" + SEVERITIES[i] + " " + TYPES[j] + "] " + callbackData.pMessageString());
		
		return VK10.VK_FALSE;
	}
	
	public static boolean pickPhysicalDevice(VulkanWindow window)
	{
		try
		{
			Vulkan vulkan = RenderEngine.getVulkan();
			vulkan.setPhysicalDeviceFilter(physicalDevice -> physicalDevice.getQueueFamily(VK10.VK_QUEUE_GRAPHICS_BIT | VK10.VK_QUEUE_COMPUTE_BIT, window.getSurface()) != null);
			if (TestProperties.DEVICE.isDefault()) vulkan.pickPhysicalDevices(PhysicalDevice.typeBestMatcher());
			else vulkan.pickPhysicalDevices(TestProperties.DEVICE.getValue());
			return true;
		}
		catch (PhysicalDeviceNotFoundException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return false;
		}
	}
	
	public static Texture createDepthTexture(int width, int height)
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
		Files.write(new File(shader.getFile().getPath() + ".bin").toPath(), shader.getLastCompilation().get().getBinary());
	}
	
	public static void println(String s, Runnable action, Property<Boolean> printProperty)
	{
		if (printProperty.getValue()) System.out.println(s + " time = " + recordTime(action, TimeUnit.MICROSECONDS) / 1000D + "ms");
		else action.run();
	}
	
	public static long recordTime(Runnable runnable, TimeUnit unit)
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
	
	public static double recordTime(Runnable runnable, TimeUnit unit, TimeUnit resolution)
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
}
