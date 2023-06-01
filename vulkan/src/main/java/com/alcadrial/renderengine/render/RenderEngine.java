package com.alcadrial.renderengine.render;

import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import com.alcadrial.renderengine.core.RenderEngineCore;

public class RenderEngine extends RenderEngineCore {
	
	private static final List<String> VALIDATION_LAYERS = List.of("VK_LAYER_KHRONOS_validation");
	private static final List<String> DEVICE_EXTENSIONS = List.of("VK_KHR_swapchain");
	
	private static volatile boolean debug = false;
	private static volatile boolean initializated = false;
	private static volatile Exception initException = null;
	private static volatile Vulkan vulkan = null;
	private static volatile VkDebugUtilsMessengerCallbackEXTI messenger = null;
	
	public static boolean initialize()
	{
		return initializated = !(!GLFW.glfwInit() || !initVulkan());
	}
	
	private static boolean initVulkan()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
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
				
				if (debug && messenger != null)
				{
					VkDebugUtilsMessengerCreateInfoEXT info = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
					
					info.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
					info.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
					info.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
					info.pfnUserCallback(messenger);
					
					createInfo.pNext(info.address());
				}
			}
			vulkan = new Vulkan(createInfo);
			return true;
		}
		catch (Exception e)
		{
			initException = e;
			return false;
		}
	}
	
	static PointerBuffer getValidationLayers(MemoryStack stack)
	{
		if (debug)
		{
			PointerBuffer array = stack.mallocPointer(VALIDATION_LAYERS.size());
			for (String string : VALIDATION_LAYERS) array.put(stack.UTF8(string));
			return array.rewind();
		}
		return stack.mallocPointer(0);
	}
	
	static PointerBuffer getDeviceExtensions(MemoryStack stack)
	{
		PointerBuffer array = stack.mallocPointer(DEVICE_EXTENSIONS.size());
		for (String string : DEVICE_EXTENSIONS) array.put(stack.UTF8(string));
		return array.rewind();
	}
	
	public static void terminate()
	{
		if (initializated)
		{
			vulkan.close();
			vulkan = null;
			GLFW.glfwTerminate();
			initializated = false;
		}
	}
	
	public static boolean isInitializated()
	{
		return initializated;
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
	
	public static void setDebugMessengerCallback(VkDebugUtilsMessengerCallbackEXTI messenger)
	{
		RenderEngine.messenger = messenger;
	}
}
