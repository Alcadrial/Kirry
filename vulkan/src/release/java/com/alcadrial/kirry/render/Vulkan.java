package com.alcadrial.kirry.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.linux.UNISTD;
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

import com.alcadrial.kirry.core.util.OperativeSystem;
import com.alcadrial.kirry.render.queue.QueueFamily;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.PhysicalDeviceNotFoundException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.VulkanWindow;

import tools.jackson.databind.ObjectMapper;

public class Vulkan implements AutoCloseable {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	class ThreadData {
		
		@Nullable
		VulkanWindow window;
		ConcurrentLinkedDeque<VulkanWindow> newWindows;
		Predicate<PhysicalDevice> physicalDeviceFilter;
		@Nullable
		List<PhysicalDevice> validPhysicalDevices;
		@Nullable
		PhysicalDevice physicalDevice;
		@Nullable
		Device device;
		long threadId;
		
		public ThreadData()
		{
			window = defaultWindow;
			newWindows = new ConcurrentLinkedDeque<>();
			physicalDeviceFilter = defaultPhysicalDeviceFilter;
			threadId = Integer.toUnsignedLong(switch (OperativeSystem.OS)
				{
					case WINDOWS -> Kernel32.GetCurrentThreadId();
					case LINUX -> UNISTD.gettid();
					default -> -1;
				});
		}
	}
	
	private VkInstanceCreateInfo createInfo;
	private VkInstance instance;
	
	private boolean debugUtilsExtensionEnabled;
	
	private long debugMessengerId;
	
	private Object physicalDevicesMonitor;
	private @Nullable List<PhysicalDevice> physicalDevices;
	
	private @Nullable VulkanWindow defaultWindow;
	private Predicate<PhysicalDevice> defaultPhysicalDeviceFilter;
	private @Nullable Device closingDevice;
	
	private ConcurrentHashMap<Thread, ThreadData> data;
	private @Nullable ObjectMapper mapper;
	
	public Vulkan(VkInstanceCreateInfo createInfo, boolean debugUtilsExtensionEnabled) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			VulkanErrorUtils.assertNoError(VK10.vkCreateInstance(createInfo, null, p));
			
			this.createInfo = createInfo;
			instance = new VkInstance(p.get(0), createInfo);
		}
		this.debugUtilsExtensionEnabled = debugUtilsExtensionEnabled;
		defaultPhysicalDeviceFilter = _ -> true;
		data = new ConcurrentHashMap<>();
		physicalDevicesMonitor = new Object();
	}
	
	public void setObjectMapper(ObjectMapper mapper)
	{
		this.mapper = mapper;
	}
	
	/**
	 * Thread-unsafe, concurrent access with the same oldWindow must be externally synchronized
	 */
	public void replaceWindow(VulkanWindow oldWindow, VulkanWindow newWindow)
	{
		if (defaultWindow == oldWindow) defaultWindow = newWindow;
		ThreadData localData = getData();
		if (localData.window == oldWindow) localData.window = newWindow;
		data.forEachValue(2048, data -> {
			if (data.newWindows.peekLast() == oldWindow)
			{
				data.newWindows.addLast(newWindow);
			}
		});
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
		long[] p = new long[1];
		
		if (VK10.vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") == 0) return false;
		if (EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, info, null, p) != VK10.VK_SUCCESS) return false;
		
		createInfo.pNext(info.address());
		debugMessengerId = p[0];
		return true;
	}
	
	public void setDefaultPhysicalDeviceFilter(Predicate<PhysicalDevice> physicalDeviceFilter)
	{
		defaultPhysicalDeviceFilter = physicalDeviceFilter;
	}
	
	public void setPhysicalDeviceFilter(Predicate<PhysicalDevice> physicalDeviceFilter)
	{
		ThreadData threadData = getData();
		threadData.physicalDeviceFilter = physicalDeviceFilter;
		threadData.validPhysicalDevices = null;
	}
	
	public void pickPhysicalDevices(Comparator<PhysicalDevice> bestMatcher) throws PhysicalDeviceNotFoundException, VulkanException
	{
		ThreadData threadData = getData();
		if (threadData.physicalDevice == null)
		{
			threadData.physicalDevice = getValidPhysicalDevices().stream().sorted(bestMatcher).findFirst().orElse(null);
			if (threadData.physicalDevice == null) throw new PhysicalDeviceNotFoundException("No physical device has the required graphics properties");
		}
	}
	
	public void pickPhysicalDevices(int index) throws PhysicalDeviceNotFoundException, VulkanException
	{
		ThreadData threadData = getData();
		if (threadData.physicalDevice == null)
		{
			List<PhysicalDevice> validPhysicalDevices = getValidPhysicalDevices();
			if (index < 0 || index >= validPhysicalDevices.size()) throw new PhysicalDeviceNotFoundException("Unknown physical device");
			threadData.physicalDevice = validPhysicalDevices.get(index);
		}
	}
	
	public void pickPhysicalDevices(PhysicalDevice physicalDevice) throws PhysicalDeviceNotFoundException, VulkanException
	{
		ThreadData threadData = getData();
		if (threadData.physicalDevice == null)
		{
			List<PhysicalDevice> validPhysicalDevices = getValidPhysicalDevices();
			if (!validPhysicalDevices.contains(physicalDevice)) throw new PhysicalDeviceNotFoundException("Unknown physical device");
			threadData.physicalDevice = physicalDevice;
		}
	}
	
	public List<PhysicalDevice> getValidPhysicalDevices() throws VulkanException
	{
		ThreadData threadData = getData();
		if (threadData.validPhysicalDevices == null)
		{
			List<PhysicalDevice> l = new LinkedList<>();
			for (PhysicalDevice device : getAvailablePhysicalDevices()) if (threadData.physicalDeviceFilter.test(device)) l.add(device);
			threadData.validPhysicalDevices = List.copyOf(l);
		}
		return (@NonNull List<PhysicalDevice>) threadData.validPhysicalDevices;
	}
	
	/**
	 * 
	 * @return A list of all the available Physical Devices
	 * @throws VulkanException
	 */
	public List<PhysicalDevice> getAvailablePhysicalDevices() throws VulkanException
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
					
					List<PhysicalDevice> l = new ArrayList<>(p[0]);
					for (int i = 0; i < p[0]; i++)
					{
						l.add(new PhysicalDevice(new VkPhysicalDevice(physicalDevicesArray.get(i), instance)));
					}
					physicalDevices = List.copyOf(l);
				}
			}
		}
		return (@NonNull List<PhysicalDevice>) physicalDevices;
	}
	
	public void createDevice() throws VulkanException
	{
		createDevice(VkPhysicalDeviceFeatures.calloc(), null);
	}
	
	/**
	 * The deviceFeatures param is automatically freed at the end of the method
	 * after the creation of the device.
	 * 
	 * 
	 * @param deviceFeatures
	 * @throws VulkanException
	 */
	public Device createDevice(VkPhysicalDeviceFeatures deviceFeatures, Surface.@Nullable Builder surface, String... deviceExtensions) throws VulkanException
	{
		ThreadData threadData = getData();
		if (threadData.device == null)
		{
			PhysicalDevice physicalDevice = VulkanErrorUtils.nonNull(threadData.physicalDevice, "Physical device must be picked before calling getDevice using #pickPhysicalDevices");
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				QueueFamily graphicsQueue = physicalDevice.getGraphicsQueueFamily();
				QueueFamily computeQueue = (@NonNull QueueFamily) physicalDevice.getQueueFamily(VK10.VK_QUEUE_COMPUTE_BIT);
				QueueFamily graphicsComputeQueue = (@NonNull QueueFamily) physicalDevice.getQueueFamily(VK10.VK_QUEUE_GRAPHICS_BIT | VK10.VK_QUEUE_COMPUTE_BIT);
				QueueFamily presentationQueue = null;
				
				if (surface != null)
				{
					presentationQueue = VulkanErrorUtils.nonNull(physicalDevice.getQueueFamily(0, surface), "Missing valid presentation queue family");
				}
				VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(physicalDevice.indices(), stack);
				
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
				createInfo.ppEnabledLayerNames(KirryVulkan.getInstanceLayers(stack));
				createInfo.ppEnabledExtensionNames(deviceExtensionBuffer);
				
				PointerBuffer devicePointer = stack.pointers(VK10.VK_NULL_HANDLE);
				VulkanErrorUtils.assertNoError(VK10.vkCreateDevice(physicalDevice.getVk(), createInfo, null, devicePointer), "Failed to create logical device");
				threadData.device = new Device(new VkDevice(devicePointer.get(0), physicalDevice.getVk(), createInfo), physicalDevice, graphicsQueue, computeQueue, graphicsComputeQueue, presentationQueue);
				
				return threadData.device;
			}
			finally
			{
				deviceFeatures.free();
			}
		}
		return (@NonNull Device) threadData.device;
	}
	
	public Device getDevice()
	{
		if (closingDevice != null) return closingDevice;
		return (@NonNull Device) getData().device;
	}
	
	public VkInstance getInstance()
	{
		return instance;
	}
	
	public boolean isDebugUtilsExtensionEnabled()
	{
		return debugUtilsExtensionEnabled;
	}
	
	public PhysicalDevice getPhysicalDevice()
	{
		return (@NonNull PhysicalDevice) getData().physicalDevice;
	}
	
	public VulkanWindow getWindow()
	{
		ThreadData localData = getData();
		VulkanWindow window = (@NonNull VulkanWindow) localData.window;
		if (window.isLost())
		{
			Queue<VulkanWindow> newWindows = localData.newWindows;
			for (VulkanWindow newWindow = newWindows.poll(); newWindow != null; newWindow = newWindows.poll()) window = newWindow;
			localData.window = window;
		}
		return window;
	}
	
	public ObjectMapper getObjectMapper()
	{
		ObjectMapper mapper = this.mapper;
		if (mapper == null) mapper = new ObjectMapper();
		return mapper;
	}
	
	ThreadData getData()
	{
		return (@NonNull ThreadData) data.computeIfAbsent(Thread.currentThread(), _ -> new ThreadData());
	}
	
	Map<Thread, ThreadData> getAllData()
	{
		return data;
	}
	
	@Override
	public void close()
	{
		data.forEachValue(Long.MAX_VALUE, threadData -> {
			if (threadData.window != null) threadData.window.close();
			if (threadData.device != null)
			{
				closingDevice = threadData.device;
				closingDevice.close();
				closingDevice = null;
			}
		});
		if (debugMessengerId != 0) EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessengerId, null);
		if (physicalDevices != null) for (PhysicalDevice physicalDevice : physicalDevices) physicalDevice.close();
		VK10.vkDestroyInstance(instance, null);
	}
}
