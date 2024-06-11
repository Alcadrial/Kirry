package com.alcadrial.renderengine.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.windows.Kernel32;
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

import com.alcadrial.renderengine.render.util.PhysicalDeviceNotFoundException;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class Vulkan implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	class ThreadData {
		
		VulkanWindow window;
		Predicate<PhysicalDevice> physicalDeviceFilter;
		List<PhysicalDevice> validPhysicalDevices;
		PhysicalDevice physicalDevice;
		Device device;
		long threadId;
		
		public ThreadData()
		{
			window = defaultWindow;
			physicalDeviceFilter = defaultPhysicalDeviceFilter;
			threadId = Kernel32.GetCurrentThreadId();
		}
	}
	
	private VkInstanceCreateInfo createInfo;
	private VkInstance instance;
	
	private long debugMessengerId;
	
	private Object physicalDevicesMonitor;
	private List<PhysicalDevice> physicalDevices;
	
	private VulkanWindow defaultWindow;
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
	
	public void setDefaultWindow(VulkanWindow defaultWindow)
	{
		this.defaultWindow = defaultWindow;
	}
	
	public void setWindow(VulkanWindow window)
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
		ThreadData threadData = getData();
		threadData.physicalDeviceFilter = VulkanErrorUtils.nonNull(physicalDeviceFilter);
		threadData.validPhysicalDevices = null;
	}
	
	public void pickPhysicalDevices(Comparator<PhysicalDevice> bestMatcher) throws PhysicalDeviceNotFoundException
	{
		ThreadData threadData = getData();
		if (threadData.physicalDevice == null)
		{
			threadData.physicalDevice = getValidPhysicalDevices().stream().sorted(bestMatcher).findFirst().orElse(null);
			if (threadData.physicalDevice == null) throw new PhysicalDeviceNotFoundException("No physical device has the required graphics properties");
		}
	}
	
	public void pickPhysicalDevices(int index) throws PhysicalDeviceNotFoundException
	{
		ThreadData threadData = getData();
		if (threadData.physicalDevice == null)
		{
			List<PhysicalDevice> validPhysicalDevices = getValidPhysicalDevices();
			if (index < 0 || index >= validPhysicalDevices.size()) throw new PhysicalDeviceNotFoundException("Unknown physical device");
			threadData.physicalDevice = validPhysicalDevices.get(index);
		}
	}
	
	public List<PhysicalDevice> getValidPhysicalDevices()
	{
		ThreadData threadData = getData();
		if (threadData.validPhysicalDevices == null)
		{
			threadData.validPhysicalDevices = new LinkedList<>();
			for (PhysicalDevice device : getAvailablePhysicalDevices()) if (threadData.physicalDeviceFilter.test(device)) threadData.validPhysicalDevices.add(device);
			threadData.validPhysicalDevices = List.copyOf(threadData.validPhysicalDevices);
		}
		return threadData.validPhysicalDevices;
	}
	
	/**
	 * 
	 * @return A list of all the available Physical Devices
	 */
	public List<PhysicalDevice> getAvailablePhysicalDevices()
	{
		synchronized (physicalDevicesMonitor)
		{
			if (physicalDevices == null)
			{
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					int[] p = new int[1];
					VulkanErrorUtils.assertNoError(VK10.vkEnumeratePhysicalDevices(instance, p, null), "Cannot list available physical devices");
					PointerBuffer physicalDevicesArray = stack.mallocPointer(p[0]);
					VulkanErrorUtils.assertNoError(VK10.vkEnumeratePhysicalDevices(instance, p, physicalDevicesArray), "Cannot list available physical devices");
					
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
	
	public void createDevice()
	{
		createDevice(VkPhysicalDeviceFeatures.calloc());
	}
	
	/**
	 * The deviceFeatures param is automatically freed at the end of the method
	 * after the creation of the device. 
	 * 
	 * 
	 * @param deviceFeatures
	 */
	public Device createDevice(VkPhysicalDeviceFeatures deviceFeatures, String... deviceExtensions)
	{
		ThreadData threadData = getData();
		if (threadData.device == null)
		{
			VulkanErrorUtils.nonNull(threadData.physicalDevice, "Physical device must be picked before calling getDevice using #pickPhysicalDevices");
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				QueueFamily graphicsQueue = VulkanErrorUtils.nonNull(threadData.physicalDevice.getQueueFamily(VK10.VK_QUEUE_GRAPHICS_BIT), "Missing valid graphics queue family");
				
				VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(threadData.physicalDevice.indices(), stack);
				
				for (int i = 0; i < queueCreateInfos.capacity(); i++)
				{
					VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get();
					queueCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
					queueCreateInfo.queueFamilyIndex(i);
					queueCreateInfo.pQueuePriorities(stack.floats(1));
				}
				queueCreateInfos.rewind();
				
				PointerBuffer deviceExtensionBuffer = stack.mallocPointer(deviceExtensions.length);
				for (String extension : deviceExtensions) deviceExtensionBuffer.put(stack.UTF8(extension));
				deviceExtensionBuffer.rewind();
				
				VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack);
				createInfo.sType(VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
				createInfo.pQueueCreateInfos(queueCreateInfos);
				createInfo.pEnabledFeatures(deviceFeatures);
				createInfo.ppEnabledLayerNames(RenderEngine.getInstanceLayers(stack));
				createInfo.ppEnabledExtensionNames(deviceExtensionBuffer);
				
				PointerBuffer devicePointer = stack.pointers(VK10.VK_NULL_HANDLE);
				VulkanErrorUtils.assertNoError(VK10.vkCreateDevice(threadData.physicalDevice.getVk(), createInfo, null, devicePointer), "Failed to create logical device");
				threadData.device = new Device(new VkDevice(devicePointer.get(0), threadData.physicalDevice.getVk(), createInfo), threadData.physicalDevice);
				
				Queue queue = new Queue(threadData.device, graphicsQueue.getIndex());
				threadData.device.setGraphicsQueue(queue);
				
				if (threadData.window != null)
				{
					QueueFamily presentationQueue = VulkanErrorUtils.nonNull(threadData.physicalDevice.getQueueFamily(0, threadData.window.getSurface()), "Missing valid presentation queue family");
					threadData.device.setPresentQueue(graphicsQueue.getIndex() == presentationQueue.getIndex() ? queue : new Queue(threadData.device, presentationQueue.getIndex()));
				}
				return threadData.device;
			}
			finally
			{
				deviceFeatures.free();
			}
		}
		return threadData.device;
	}
	
	public Device getDevice()
	{
		if (closingDevice != null) return closingDevice;
		return getData().device;
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
	
	public VulkanWindow getWindow()
	{
		return getData().window;
	}
	
	ThreadData getData()
	{
		return data.computeIfAbsent(Thread.currentThread(), t -> new ThreadData());
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
		if (physicalDevices != null) for (PhysicalDevice physicalDevice : physicalDevices) physicalDevice.close();
		VK10.vkDestroyInstance(instance, null);
	}
}
