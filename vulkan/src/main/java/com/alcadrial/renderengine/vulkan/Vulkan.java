package com.alcadrial.renderengine.vulkan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

import com.alcadrial.renderengine.vulkan.util.VulkanErrorUtils;

public class Vulkan implements AutoCloseable {
	
	private Window window;
	
	private VkInstanceCreateInfo createInfo;
	private VkInstance instance;
	
	private long debugMessengerId;
	
	private Predicate<PhysicalDevice> physicalDeviceFilter;
	private List<PhysicalDevice> physicalDevices;
	private List<PhysicalDevice> validPhysicalDevices;
	private PhysicalDevice physicalDevice;
	
	private Device device;
	
	public Vulkan(VkInstanceCreateInfo createInfo)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			VulkanErrorUtils.assertNoError(VK10.vkCreateInstance(createInfo, null, p));
			
			this.createInfo = createInfo;
			instance = new VkInstance(p.get(0), createInfo);
		}
		physicalDeviceFilter = d -> true;
	}
	
	public void setWindow(Window window)
	{
		this.window = window;
	}
	
	public boolean activateDebugMessenger(VkDebugUtilsMessengerCreateInfoEXT info)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] p = new long[1];
			
			if (VK10.vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") == 0) return false;
			if (EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, info, null, p) != VK10.VK_SUCCESS) return false;
			
			createInfo.pNext(info.address());
			debugMessengerId = p[0];
			return true;
		}
	}
	
	public void setPhysicalDeviceFilter(Predicate<PhysicalDevice> physicalDeviceFilter)
	{
		this.physicalDeviceFilter = VulkanErrorUtils.nonNull(physicalDeviceFilter);
		validPhysicalDevices = null;
	}
	
	public void pickPhysicalDevices(Comparator<PhysicalDevice> bestMatcher)
	{
		if (physicalDevice == null)
		{
			physicalDevice = getValidPhysicalDevices().stream().sorted(bestMatcher).findFirst().orElseGet(() -> null);
			VulkanErrorUtils.nonNull(physicalDevice, "no physical device has the required graphics properties");
		}
	}
	
	public List<PhysicalDevice> getValidPhysicalDevices()
	{
		if (validPhysicalDevices == null)
		{
			validPhysicalDevices = new LinkedList<>();
			for (PhysicalDevice device : getAvailablePhysicalDevices()) if (physicalDeviceFilter.test(device)) validPhysicalDevices.add(device);
			validPhysicalDevices = List.copyOf(validPhysicalDevices);
		}
		return validPhysicalDevices;
	}
	
	public List<PhysicalDevice> getAvailablePhysicalDevices()
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
					physicalDevices.add(new PhysicalDevice(new VkPhysicalDevice(physicalDevicesArray.get(i), instance), window.getSurface().getSurface()));
				}
				physicalDevices = List.copyOf(physicalDevices);
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
				
				PointerBuffer devicePointer = stack.pointers(VK10.VK_NULL_HANDLE);
				VulkanErrorUtils.assertNoError(VK10.vkCreateDevice(physicalDevice.getDevice(), createInfo, null, devicePointer), "Failed to create logical device");
				device = new Device(window, new VkDevice(devicePointer.get(0), physicalDevice.getDevice(), createInfo), physicalDevice);
				device.setGraphicsQueue(new Queue(device, physicalDevice.getQueueGraphicsFamilyIndex()));
				device.setPresentQueue(new Queue(device, physicalDevice.getQueueSurfaceFamilyIndex()));
			}
		}
		return device;
	}
	
	public void swapBuffers()
	{
		
	}
	
	public VkInstance getInstance()
	{
		return instance;
	}
	
	public PhysicalDevice getPhysicalDevice()
	{
		return physicalDevice;
	}
	
	public Window getWindow()
	{
		return window;
	}
	
	@Override
	public void close()
	{
		if (debugMessengerId != 0) EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessengerId, null);
		if (device != null) device.close();
		for (PhysicalDevice physicalDevice : physicalDevices) physicalDevice.close();
		VK10.vkDestroyInstance(instance, null);
	}
}
