package com.alcadrial.renderengine.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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

import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.window.Window;

public class Vulkan implements AutoCloseable {
	
	private class ThreadData {
		
		private Window window;
		private Predicate<PhysicalDevice> physicalDeviceFilter;
		private List<PhysicalDevice> validPhysicalDevices;
		private PhysicalDevice physicalDevice;
		private Device device;
		
		public ThreadData()
		{
			window = defaultWindow;
			physicalDeviceFilter = defaultPhysicalDeviceFilter;
		}
	}
	
	private VkInstanceCreateInfo createInfo;
	private VkInstance instance;
	
	private long debugMessengerId;
	
	private Object physicalDevicesMonitor;
	private List<PhysicalDevice> physicalDevices;
	
	private Window defaultWindow;
	private Predicate<PhysicalDevice> defaultPhysicalDeviceFilter;
	
	private ConcurrentHashMap<Thread, ThreadData> data;
	
	private Device closingDevice;
	
	public Vulkan(VkInstanceCreateInfo createInfo)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			VulkanErrorUtils.assertNoError(VK10.vkCreateInstance(createInfo, null, p));
			
			this.createInfo = createInfo;
			instance = new VkInstance(p.get(0), createInfo);
		}
		defaultPhysicalDeviceFilter = d -> true;
		data = new ConcurrentHashMap<>();
		physicalDevicesMonitor = new Object();
	}
	
	public void setDefaultWindow(Window defaultWindow)
	{
		this.defaultWindow = defaultWindow;
	}
	
	public void setWindow(Window window)
	{
		getData().window = window;
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
	
	public void setDefaultPhysicalDeviceFilter(Predicate<PhysicalDevice> physicalDeviceFilter)
	{
		defaultPhysicalDeviceFilter = VulkanErrorUtils.nonNull(physicalDeviceFilter);
	}
	
	public void setPhysicalDeviceFilter(Predicate<PhysicalDevice> physicalDeviceFilter)
	{
		getData().physicalDeviceFilter = VulkanErrorUtils.nonNull(physicalDeviceFilter);
		getData().validPhysicalDevices = null;
	}
	
	public void pickPhysicalDevices(Comparator<PhysicalDevice> bestMatcher)
	{
		ThreadData threadData = getData();
		if (threadData.physicalDevice == null)
		{
			threadData.physicalDevice = getValidPhysicalDevices().stream().sorted(bestMatcher).findFirst().orElseGet(() -> null);
			VulkanErrorUtils.nonNull(threadData.physicalDevice, "No physical device has the required graphics properties");
		}
	}
	
	public List<PhysicalDevice> getValidPhysicalDevices()
	{
		ThreadData threadData = getData();
		if (threadData.validPhysicalDevices == null)
		{
			threadData.validPhysicalDevices = new LinkedList<>();
			for (PhysicalDevice device : getAvailablePhysicalDevices())
			{
				device.searchQueueSurfaceFamilyProperties(threadData.window.getSurface().getSurface());
				if (threadData.physicalDeviceFilter.test(device)) threadData.validPhysicalDevices.add(device);
			}
			threadData.validPhysicalDevices = List.copyOf(threadData.validPhysicalDevices);
		}
		return threadData.validPhysicalDevices;
	}
	
	public List<PhysicalDevice> getAvailablePhysicalDevices()
	{
		synchronized (physicalDevicesMonitor)
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
						physicalDevices.add(new PhysicalDevice(new VkPhysicalDevice(physicalDevicesArray.get(i), instance)));
					}
					physicalDevices = List.copyOf(physicalDevices);
				}
			}
		}
		return physicalDevices;
	}
	
	public Device getDevice()
	{
		ThreadData threadData = getData();
		if (closingDevice != null) return closingDevice;
		if (threadData.device == null)
		{
			VulkanErrorUtils.nonNull(threadData.physicalDevice, "Physical device must be picked before calling getDevice using #pickPhysicalDevices");
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VulkanErrorUtils.nonNull(threadData.physicalDevice.getQueueGraphicsFamilyProperties(), "Missing valid queue graphics family properties");
				
				VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(threadData.physicalDevice.indices(), stack);
				
				for (Integer index : threadData.physicalDevice.iterateIndices())
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
				try
				{
					VulkanErrorUtils.assertNoError(VK10.vkCreateDevice(threadData.physicalDevice.getVk(), createInfo, null, devicePointer), "Failed to create logical device");
				}
				catch (Exception e)
				{
					System.out.println(createInfo.pQueueCreateInfos().capacity());
					throw e;
				}
				threadData.device = new Device(new VkDevice(devicePointer.get(0), threadData.physicalDevice.getVk(), createInfo), threadData.physicalDevice);
				threadData.device.setGraphicsQueue(new Queue(threadData.device, threadData.physicalDevice.getQueueGraphicsFamilyIndex()));
				threadData.device.setPresentQueue(new Queue(threadData.device, threadData.physicalDevice.getQueueSurfaceFamilyIndex()));
			}
		}
		return threadData.device;
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
		return getData().physicalDevice;
	}
	
	public Window getWindow()
	{
		return getData().window;
	}
	
	private ThreadData getData()
	{
		return data.computeIfAbsent(Thread.currentThread(), k -> new ThreadData());
	}
	
	@Override
	public void close()
	{
		data.forEachValue(Long.MAX_VALUE, threadData -> {
			if (threadData.device != null)
			{
				closingDevice = threadData.device;
				threadData.device.close();
				closingDevice = null;
			}
			if (threadData.window != null) threadData.window.close();
		});
		if (debugMessengerId != 0) EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessengerId, null);
		for (PhysicalDevice physicalDevice : physicalDevices) physicalDevice.close();
		VK10.vkDestroyInstance(instance, null);
	}
}
