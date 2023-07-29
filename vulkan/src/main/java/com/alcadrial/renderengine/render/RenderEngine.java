package com.alcadrial.renderengine.render;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import com.alcadrial.renderengine.core.RenderEngineCore;
import com.alcadrial.renderengine.render.util.Version;

public class RenderEngine extends RenderEngineCore {
	
	private static final List<String> VALIDATION_LAYERS = List.of("VK_LAYER_KHRONOS_validation");
	private static final List<String> DEVICE_EXTENSIONS = List.of(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);
	
	private static volatile boolean debug = false;
	private static volatile boolean initializated = false;
	private static volatile Exception initException = null;
	private static volatile Vulkan vulkan = null;
	private static volatile ExecutorService executor = null;
	
	/**
	 * <p>Creates a builder that can be used to choose various aspects of the engine</p>
	 * <p>After all the data have been set, a call to {@link Builder#initialize()} will 
	 * initialize the engine using the values of the builder</p>
	 * <p>A builder can be used infinite times</p>
	 * 
	 * @param applicationName 
	 * @param version 
	 * 
	 * @return a new {@link Builder}
	 */
	public static Builder create(String applicationName, Version version)
	{
		return new Builder(applicationName, version);
	}
	
	/**
	 * <p>Initialize the library and return true if the library results as initialized after calling this method.</p>
	 * 
	 * <p>In order To terminate and release any resource allocated by the library, the function {@link #terminate()} must be called.</p>
	 * 
	 * <p>After a call to {@link #terminate()}, calling this function will re-initialize the library as of the first time</p>
	 * 
	 * @param applicationName 
	 * @param version 
	 * 
	 * @return true if the library is initialized, false otherwise
	 */
	public static boolean initialize(String applicationName, Version version)
	{
		return new Builder(applicationName, version).initialize();
	}
	
	private static boolean initialize(Builder builder)
	{
		if (!initializated) return initializated = !(!GLFW.glfwInit() || !initVulkan(builder));
		return true;
	}
	
	private static boolean initVulkan(Builder builder)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
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
				PointerBuffer requiredInstances = GLFWVulkan.glfwGetRequiredInstanceExtensions();
				
				PointerBuffer enlargedBuffer = stack.mallocPointer(requiredInstances.capacity() + 1);
				enlargedBuffer.put(requiredInstances);
				enlargedBuffer.put(stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
				enlargedBuffer.rewind();
				
				createInfo.ppEnabledExtensionNames(requiredInstances);
				createInfo.ppEnabledLayerNames(getValidationLayers(stack));
				
				if (builder.messenger != null)
				{
					VkDebugUtilsMessengerCreateInfoEXT info = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
					
					info.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
					info.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
					info.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
					info.pfnUserCallback((VkDebugUtilsMessengerCallbackEXTI) (messageSeverity, messageType, callbackDataId, userDataId) -> {
						if (debug) return builder.messenger.invoke(messageSeverity, messageType, callbackDataId, userDataId);
						return VK10.VK_FALSE;
					});
					
					createInfo.pNext(info.address());
				}
			}
			vulkan = new Vulkan(createInfo);
			
			executor = Executors.newCachedThreadPool();
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
	
	/**
	 * <b>MUST BE CALLED ONLY AFTER ANY THREAD HAS FINISHED USING THE LIBRARY.</b>
	 * 
	 * <p>After calling terminate(), {@link #initialize()} may be called again.</p>
	 */
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
	
	public static ExecutorService getExecutor()
	{
		return executor;
	}
	
	public static void setDebugOutput(PrintStream debugOutput)
	{
		Debug.setDebugOutput(debugOutput);
	}
	
	public static class Builder {
		
		private String applicationName;
		private Version version;
		private VkDebugUtilsMessengerCallbackEXTI messenger;
		
		private Builder(String applicationName, Version version)
		{
			this.applicationName = applicationName;
			this.version = version;
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
		
		public Builder messenger(VkDebugUtilsMessengerCallbackEXTI messenger)
		{
			this.messenger = messenger;
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
