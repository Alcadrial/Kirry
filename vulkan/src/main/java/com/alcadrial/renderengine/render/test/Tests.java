package com.alcadrial.renderengine.render.test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;

import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.Window;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.util.Properties;

@Test
public class Tests {
	
	public static void main(String[] args)
	{
		Property.parseProperties(args, Properties.PROPERTIES);
		
		System.out.println("Start time = " + recordTime(Tests::start, TimeUnit.MICROSECONDS) / 1000D + "ms");
		System.out.println("Loop time = " + recordTime(Tests::loop, TimeUnit.MICROSECONDS) / 1000D + "ms");
		System.out.println("End time = " + recordTime(Tests::end, TimeUnit.MICROSECONDS) / 1000D + "ms");
	}
	
	public static void start()
	{
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		RenderEngine.setDebugMessengerCallback(Tests::debugCallback);
		
		System.out.println("Render Engine initialization time = " + recordTime(() -> {
			if (!RenderEngine.initialize()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		}, TimeUnit.MICROSECONDS) / 1000D + "ms");
		
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
		// GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
		// GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_FALSE);
		Vulkan vulkan = RenderEngine.getVulkan();
		
		System.out.println("Window creation time = " + recordTime(() -> vulkan.setWindow(new Window(1080, 720, "Test")), TimeUnit.MICROSECONDS) / 1000D + "ms");
		
		vulkan.setPhysicalDeviceFilter(physicalDevice -> physicalDevice.getQueueGraphicsFamilyProperties() != null);
		vulkan.pickPhysicalDevices(PhysicalDevice.typeBestMatcher());
		Window window = vulkan.getWindow();
		System.out.println("Window initialization time = " + recordTime(window::initialize, TimeUnit.MICROSECONDS) / 1000D + "ms");
		Shader vertexShader;
		Shader fragmentShader;
		try
		{
			vertexShader = new Shader(new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/shader.vert"), "main");
			fragmentShader = new Shader(new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/shader.frag"), "main");
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		window.setPipelineFrontOrientation(false);
		
		System.out.println("Graphics pipeline build time = " + recordTime(() -> window.buildGraphicsPipeline(vertexShader, fragmentShader), TimeUnit.MICROSECONDS) / 1000D + "ms");
	}
	
	public static void loop()
	{
		Device device = RenderEngine.getVulkan().getDevice();
		Window window = device.getWindow();
		float r = 0;
		float g = 0;
		float b = 0;
		float a = 0;
		while (!window.shouldClose())
		{
			window.pollEvents();
			device.draw();
			window.setClearColor(Color.of(r, g, b, a));
		}
	}
	
	public static void end()
	{
		RenderEngine.terminate();
	}
	
	public static int debugCallback(int messageSeverity, int messageType, long callbackDataId, long userDataId)
	{
		VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataId);
		
		System.out.println("[Vulkan debug] " + callbackData.pMessageString());
		
		return VK10.VK_FALSE;
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
}
