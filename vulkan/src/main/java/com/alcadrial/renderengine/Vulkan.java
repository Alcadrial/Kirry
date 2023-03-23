package com.alcadrial.renderengine;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import com.alcadrial.renderengine.shader.CompiledShader;
import com.alcadrial.renderengine.shader.Shader;
import com.alcadrial.renderengine.shader.ShaderType;

public class Vulkan implements AutoCloseable {
	
	private long window;
	private VkInstanceCreateInfo createInfo;
	private VkInstance instance;
	private long debugMessengerId;
	private PhysicalDevice physicalDevice;
	private List<PhysicalDevice> physicalDevices;
	private long surface;
	private Device device;
	
	public Vulkan(long window, VkInstanceCreateInfo createInfo)
	{
		this.window = window;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			VulkanErrorUtils.assertNoError(VK10.vkCreateInstance(createInfo, null, p));
			
			this.createInfo = createInfo;
			instance = new VkInstance(p.get(0), createInfo);
		}
	}
	
	public boolean activateDebugMessenger(VkDebugUtilsMessengerCreateInfoEXT info)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			LongBuffer p = stack.longs(VK10.VK_NULL_HANDLE);
			
			if (VK10.vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") == 0) return false;
			if (EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, info, null, p) != VK10.VK_SUCCESS) return false;
			
			createInfo.pNext(info.address());
			debugMessengerId = p.get(0);
			return true;
		}
	}
	
	public void createSurface(long window)
	{
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(GLFWVulkan.glfwCreateWindowSurface(instance, window, null, p), "Failed to create window surface");
		surface = p[0];
	}
	
	public void pickPhysicalDevices()
	{
		if (physicalDevice == null)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				int[] p = new int[1];
				VK10.vkEnumeratePhysicalDevices(instance, p, null);
				PointerBuffer physicalDevicesArray = stack.mallocPointer(p[0]);
				VK10.vkEnumeratePhysicalDevices(instance, p, physicalDevicesArray);
				
				for (PhysicalDevice device : getPhysicalDevices())
				{
					if (device.getQueueGraphicsFamilyProperties() != null)
					{
						physicalDevice = device;
						return;
					}
				}
				VulkanErrorUtils.nonNull(physicalDevice, "no physical device has the required graphics properties");
			}
		}
	}
	
	public List<PhysicalDevice> getPhysicalDevices()
	{
		if (physicalDevices == null)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				int[] p = new int[1];
				VK10.vkEnumeratePhysicalDevices(instance, p, null);
				PointerBuffer physicalDevicesArray = stack.mallocPointer(p[0]);
				VK10.vkEnumeratePhysicalDevices(instance, p, physicalDevicesArray);
				
				physicalDevices = new ArrayList<>(p[0]);
				for (int i = 0; i < p[0]; i++)
				{
					physicalDevices.add(new PhysicalDevice(new VkPhysicalDevice(physicalDevicesArray.get(i), instance), surface));
				}
			}
		}
		return physicalDevices;
	}
	
	public Device getDevice()
	{
		if (device == null)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VulkanErrorUtils.nonNull(physicalDevice.getQueueGraphicsFamilyProperties(), "Missing valid queue graphics family properties");
				
				VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(physicalDevice.indices(), stack);
				
				for (Integer index : physicalDevice.iterateIndices())
				{
					VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get();
					queueCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
					queueCreateInfo.queueFamilyIndex(index);
					queueCreateInfo.pQueuePriorities(stack.floats(1));
				}
				queueCreateInfos.rewind();
				
				VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);
				
				VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack);
				
				createInfo.sType(VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
				createInfo.pQueueCreateInfos(queueCreateInfos);
				createInfo.pEnabledFeatures(deviceFeatures);
				createInfo.ppEnabledLayerNames(RenderEngine.getValidationLayers(stack));
				createInfo.ppEnabledExtensionNames(RenderEngine.getDeviceExtensions(stack));
				
				PointerBuffer pDevice = stack.pointers(VK10.VK_NULL_HANDLE);
				VulkanErrorUtils.assertNoError(VK10.vkCreateDevice(physicalDevice.getDevice(), createInfo, null, pDevice), "Failed to create logical device");
				device = new Device(instance, window, surface, new VkDevice(pDevice.get(0), physicalDevice.getDevice(), createInfo), physicalDevice);
				device.setGraphicsQueue(new Queue(device, physicalDevice.getQueueGraphicsFamilyIndex()));
				device.setPresentQueue(new Queue(device, physicalDevice.getQueueSurfaceFamilyIndex()));
			}
		}
		return device;
	}
	
	public void buildGraphicPipeline(Shader vertexShader, Shader fragmentShader)
	{
		VulkanErrorUtils.match(vertexShader.getType(), ShaderType.VERTEX, "Not a vertex shader");
		VulkanErrorUtils.match(fragmentShader.getType(), ShaderType.FRAGMENT, "Not a fragment shader");
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			CompiledShader vertexCompiled = vertexShader.getOrCompile();
			CompiledShader fragmentCompiled = fragmentShader.getOrCompile();
			
			if (vertexCompiled.hasErrors()) throw new IllegalStateException("Shader " + vertexShader.getFile() + " cannot be used ");
			if (fragmentCompiled.hasErrors()) throw new IllegalStateException("Shader " + fragmentShader.getFile() + " cannot be used ");
			
			long vertShaderModule = createShaderModule(vertexCompiled.getBinary());
			long fragShaderModule = createShaderModule(fragmentCompiled.getBinary());
			
			ByteBuffer entryPoint = stack.UTF8("main");
			
			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
			
			VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
			
			vertShaderStageInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
			vertShaderStageInfo.stage(VK10.VK_SHADER_STAGE_VERTEX_BIT);
			vertShaderStageInfo.module(vertShaderModule);
			vertShaderStageInfo.pName(entryPoint);
			
			VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);
			
			fragShaderStageInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
			fragShaderStageInfo.stage(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			fragShaderStageInfo.module(fragShaderModule);
			fragShaderStageInfo.pName(entryPoint);
			
			VK10.vkDestroyShaderModule(device.getDevice(), vertShaderModule, null);
			VK10.vkDestroyShaderModule(device.getDevice(), fragShaderModule, null);
		}
	}
	
	private long createShaderModule(byte[] binary)
	{
		return 0;
	}
	
	@Override
	public void close()
	{
		if (debugMessengerId != 0) EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessengerId, null);
		if (device != null) device.close();
		if (surface != 0) KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
		VK10.vkDestroyInstance(instance, null);
	}
}
