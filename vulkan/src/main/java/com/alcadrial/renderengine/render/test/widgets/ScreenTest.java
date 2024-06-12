package com.alcadrial.renderengine.render.test.widgets;

import org.lwjgl.vulkan.EXTDepthRangeUnrestricted;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.test.RenderTest;
import com.alcadrial.renderengine.render.test.TestProperties;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.window.BlackScreen;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class ScreenTest extends RenderTest {
	
	public static void main(String[] args)
	{
		TestProperties.init(args);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		println("Render Engine initialization", RenderTest::initRenderEngine, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		
		println("Start", ScreenTest::start, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		
		println("Loop", ScreenTest::loop, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		
		println("End", ScreenTest::end, TestProperties.PRINT_LOADING_TIMES);
	}
	
	public static void start()
	{
		Vulkan vulkan = RenderEngine.getVulkan();
		VulkanWindow window = new VulkanWindow(1080, 720, "Vulkan");
		vulkan.setWindow(window);
		
		if (!pickPhysicalDevice(window)) return;
		
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
		deviceFeatures.wideLines(true);
		deviceFeatures.samplerAnisotropy(true);
		Device device = vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME, EXTDepthRangeUnrestricted.VK_EXT_DEPTH_RANGE_UNRESTRICTED_EXTENSION_NAME);
		window.init(device);
		window.setScreen(BlackScreen.BLACK_SCREEN);
	}
	
	public static void loop()
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		while (!window.shouldClose())
		{
			window.pollEvents();
			window.update();
		}
	}
	
	public static void end()
	{
		RenderEngine.terminate();
	}
}
