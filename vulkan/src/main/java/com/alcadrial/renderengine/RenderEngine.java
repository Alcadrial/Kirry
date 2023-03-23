package com.alcadrial.renderengine;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.shader.Shader;

public class RenderEngine {
	
	private static final List<String> VALIDATION_LAYERS = List.of("VK_LAYER_KHRONOS_validation");
	private static final List<String> DEVICE_EXTENSIONS = List.of("VK_KHR_swapchain");
	
	public static PointerBuffer getValidationLayers(MemoryStack stack)
	{
		if (Properties.DEBUG.getValue())
		{
			PointerBuffer array = stack.mallocPointer(VALIDATION_LAYERS.size());
			for (String string : VALIDATION_LAYERS) array.put(stack.UTF8(string));
			return array.rewind();
		}
		return stack.mallocPointer(0);
	}
	
	public static PointerBuffer getDeviceExtensions(MemoryStack stack)
	{
		PointerBuffer array = stack.mallocPointer(DEVICE_EXTENSIONS.size());
		for (String string : DEVICE_EXTENSIONS) array.put(stack.UTF8(string));
		return array.rewind();
	}
	
	public static void main(String[] args)
	{
		Property.parseProperties(args, Properties.PROPERTIES);
		if (!GLFW.glfwInit()) throw new RuntimeException("Cannot initialize GLFW");
		
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		// GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
		// GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_FALSE);
		
		long windowId = VulkanErrorUtils.nonNull(GLFW.glfwCreateWindow(1080, 720, "Test", 0, 0), "Cannot create window");
		
		Vulkan instance;
		Device device;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			{
				VkApplicationInfo applicationInfo = VkApplicationInfo.calloc(stack);
				
				applicationInfo.sType(VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO);
				applicationInfo.pApplicationName(stack.UTF8Safe("Test"));
				applicationInfo.applicationVersion(VK10.VK_MAKE_API_VERSION(0, 1, 0, 0));
				applicationInfo.pEngineName(stack.UTF8Safe("RenderEngine"));
				applicationInfo.engineVersion(VK10.VK_MAKE_API_VERSION(0, 1, 0, 0));
				applicationInfo.apiVersion(VK10.VK_API_VERSION_1_0);
				
				VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack);
				
				createInfo.sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
				createInfo.pApplicationInfo(applicationInfo);
				
				{
					PointerBuffer requiredInstances = GLFWVulkan.glfwGetRequiredInstanceExtensions();
					
					PointerBuffer enlargedBuffer = stack.mallocPointer(requiredInstances.capacity() + 1);
					enlargedBuffer.put(requiredInstances);
					enlargedBuffer.put(stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
					enlargedBuffer.rewind();
					
					createInfo.ppEnabledExtensionNames(requiredInstances);
					createInfo.ppEnabledLayerNames(getValidationLayers(stack));
					
					if (Properties.DEBUG.getValue())
					{
						VkDebugUtilsMessengerCreateInfoEXT info = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
						
						info.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
						info.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
						info.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
						info.pfnUserCallback(RenderEngine::debugCallback);
						
						createInfo.pNext(info.address());
					}
				}
				instance = new Vulkan(windowId, createInfo);
			}
			if (Properties.DEBUG.getValue())
			{
				VkDebugUtilsMessengerCreateInfoEXT info = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
				
				info.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
				info.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
				info.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
				info.pfnUserCallback(RenderEngine::debugCallback);
				
				if (!instance.activateDebugMessenger(info)) System.out.println("Debug messenger not active");
			}
			instance.createSurface(windowId);
			instance.setPhysicalDeviceFilter(physicalDevice -> physicalDevice.getQueueGraphicsFamilyProperties() != null);
			instance.pickPhysicalDevices(PhysicalDevice.typeBestMatcher());
			device = instance.getDevice();
			SwapChain swapChain = device.getSwapChain();
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
				instance.close();
				GLFW.glfwDestroyWindow(windowId);
				GLFW.glfwTerminate();
				return;
			}
			
			device.buildGraphicPipeline(vertexShader, fragmentShader);
			swapChain.createFrameBuffers();
			device.createSwappingBuffers(2);
		}
		
		while (!GLFW.glfwWindowShouldClose(windowId))
		{
			GLFW.glfwPollEvents();
			device.draw();
		}
		
		instance.close();
		GLFW.glfwDestroyWindow(windowId);
		GLFW.glfwTerminate();
	}
	
	public static int debugCallback(int messageSeverity, int messageType, long callbackDataId, long userDataId)
	{
		VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataId);
		
		System.out.println("[Vulkan debug] " + callbackData.pMessageString());
		
		return VK10.VK_FALSE;
	}
}
