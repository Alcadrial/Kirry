package com.alcadrial.renderengine.test;

import java.io.File;
import java.io.IOException;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;

import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.PhysicalDevice;
import com.alcadrial.renderengine.RenderEngine;
import com.alcadrial.renderengine.Vulkan;
import com.alcadrial.renderengine.Window;
import com.alcadrial.renderengine.shader.Shader;
import com.alcadrial.renderengine.util.Properties;

public class Test {
	
	public static void main(String[] args)
	{
		Property.parseProperties(args, Properties.PROPERTIES);
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		if (!RenderEngine.initialize()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		// GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
		// GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_FALSE);
		
		Window window = new Window(1080, 720, "Test");
		
		Vulkan vulkan = RenderEngine.getVulkan();
		vulkan.setWindow(window);
		
		if (Properties.DEBUG.getValue())
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkDebugUtilsMessengerCreateInfoEXT info = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
				
				info.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
				info.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
				info.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
				info.pfnUserCallback(Test::debugCallback);
				
				if (!vulkan.activateDebugMessenger(info)) System.out.println("Debug messenger not active");
			}
		}
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
		window.getSwapChain().createFrameBuffers(window);
		vulkan.getDevice().createSwappingBuffers(2);
		
		while (!window.shouldClose())
		{
			window.pollEvents();
			vulkan.getDevice().draw();
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
