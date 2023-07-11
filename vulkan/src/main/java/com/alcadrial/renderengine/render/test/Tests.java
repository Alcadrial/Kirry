package com.alcadrial.renderengine.render.test;

import java.io.File;
import java.io.IOException;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;

import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.core.util.color.Color;
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
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		RenderEngine.setDebugMessengerCallback(Tests::debugCallback);
		
		if (!RenderEngine.initialize()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		// GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
		// GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_FALSE);
		
		Window window = new Window(1080, 720, "Test");
		
		Vulkan vulkan = RenderEngine.getVulkan();
		vulkan.setWindow(window);
		
		vulkan.setPhysicalDeviceFilter(physicalDevice -> physicalDevice.getQueueGraphicsFamilyProperties() != null);
		vulkan.pickPhysicalDevices(PhysicalDevice.typeBestMatcher());
		window.initialize();
		Shader vertexShader;
		Shader fragmentShader;
		try
		{
			vertexShader = new Shader(new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/shader.vert"));
			fragmentShader = new Shader(new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/shader.frag"));
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		window.buildGraphicsPipeline(vertexShader, fragmentShader);
		
		float f = 0;
		while (!window.shouldClose())
		{
			window.pollEvents();
			vulkan.getDevice().draw();
			window.setClearColor(Color.of(f, 0, 0, 0));
			f += 0.001F;
		}
		
		RenderEngine.terminate();
	}
	
	public static int debugCallback(int messageSeverity, int messageType, long callbackDataId, long userDataId)
	{
		VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataId);
		
		System.out.println("[Vulkan debug] " + callbackData.pMessageString());
		
		return VK10.VK_FALSE;
	}
}
