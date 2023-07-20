package com.alcadrial.renderengine.render;

import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import com.alcadrial.renderengine.render.buffer.VulkanBufferData;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class Device implements AutoCloseable {
	
	private Window window;
	private VkDevice device;
	private PhysicalDevice physicalDevice;
	private Queue graphicsQueue;
	private Queue presentQueue;
	private long resetCommandPool;
	private long shortLivedCommandPool;
	private long vmaAllocator;
	
	public Device(Window window, VkDevice device, PhysicalDevice physicalDevice)
	{
		this.window = VulkanErrorUtils.nonNull(window);
		this.device = VulkanErrorUtils.nonNull(device);
		this.physicalDevice = VulkanErrorUtils.nonNull(physicalDevice);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] p = new long[1];
			
			VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
			poolInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
			poolInfo.queueFamilyIndex(physicalDevice.getQueueGraphicsFamilyIndex());
			poolInfo.flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateCommandPool(device, poolInfo, null, p), "Failed to create reset command pool");
			resetCommandPool = p[0];
			
			poolInfo.flags(VK10.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT);
			VulkanErrorUtils.assertNoError(VK10.vkCreateCommandPool(device, poolInfo, null, p), "Failed to create short lived command pool");
			shortLivedCommandPool = p[0];
		}
	}
	
	/**
	 * Vma
	 */
	public void initialize()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VmaVulkanFunctions functions = VmaVulkanFunctions.calloc(stack);
			
			functions.vkGetPhysicalDeviceProperties(VK10.vkGetInstanceProcAddr(RenderEngine.getVulkan().getInstance(), "vkGetPhysicalDeviceProperties"));
			functions.vkGetPhysicalDeviceMemoryProperties(VK10.vkGetInstanceProcAddr(RenderEngine.getVulkan().getInstance(), "vkGetPhysicalDeviceMemoryProperties"));
			functions.vkAllocateMemory(VK10.vkGetDeviceProcAddr(device, "vkAllocateMemory"));
			functions.vkFreeMemory(VK10.vkGetDeviceProcAddr(device, "vkFreeMemory"));
			functions.vkMapMemory(VK10.vkGetDeviceProcAddr(device, "vkMapMemory"));
			functions.vkUnmapMemory(VK10.vkGetDeviceProcAddr(device, "vkUnmapMemory"));
			functions.vkFlushMappedMemoryRanges(VK10.vkGetDeviceProcAddr(device, "vkFlushMappedMemoryRanges"));
			functions.vkInvalidateMappedMemoryRanges(VK10.vkGetDeviceProcAddr(device, "vkInvalidateMappedMemoryRanges"));
			functions.vkBindBufferMemory(VK10.vkGetDeviceProcAddr(device, "vkBindBufferMemory"));
			functions.vkBindImageMemory(VK10.vkGetDeviceProcAddr(device, "vkBindImageMemory"));
			functions.vkGetBufferMemoryRequirements(VK10.vkGetDeviceProcAddr(device, "vkGetBufferMemoryRequirements"));
			functions.vkGetImageMemoryRequirements(VK10.vkGetDeviceProcAddr(device, "vkGetImageMemoryRequirements"));
			functions.vkCreateBuffer(VK10.vkGetDeviceProcAddr(device, "vkCreateBuffer"));
			functions.vkDestroyBuffer(VK10.vkGetDeviceProcAddr(device, "vkDestroyBuffer"));
			functions.vkCreateImage(VK10.vkGetDeviceProcAddr(device, "vkCreateImage"));
			functions.vkDestroyImage(VK10.vkGetDeviceProcAddr(device, "vkDestroyImage"));
			functions.vkCmdCopyBuffer(VK10.vkGetDeviceProcAddr(device, "vkCmdCopyBuffer"));
			functions.vkGetInstanceProcAddr(VK10.vkGetInstanceProcAddr(RenderEngine.getVulkan().getInstance(), "vkGetInstanceProcAddr"));
			functions.vkGetDeviceProcAddr(VK10.vkGetDeviceProcAddr(device, "vkGetDeviceProcAddr"));
			
			VmaAllocatorCreateInfo allocatorInfo = VmaAllocatorCreateInfo.calloc(stack);
			allocatorInfo.flags(Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT);
			allocatorInfo.vulkanApiVersion(VK12.VK_API_VERSION_1_2);
			allocatorInfo.physicalDevice(physicalDevice.getDevice());
			allocatorInfo.device(device);
			allocatorInfo.instance(RenderEngine.getVulkan().getInstance());
			allocatorInfo.pVulkanFunctions(functions);
			
			PointerBuffer p = stack.callocPointer(1);
			Vma.vmaCreateAllocator(allocatorInfo, p);
			vmaAllocator = p.get();
		}
	}
	
	public VulkanBufferData createBuffer(MemoryStack stack, VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo)
	{
		LongBuffer p = stack.callocLong(1);
		PointerBuffer p0 = stack.callocPointer(1);
		VmaAllocationInfo allocationInfo = VmaAllocationInfo.calloc();
		VulkanErrorUtils.assertNoError(Vma.vmaCreateBuffer(vmaAllocator, bufferCreateInfo, allocationCreateInfo, p, p0, allocationInfo), "Failed to create buffer");
		return new VulkanBufferData(p.get(), p0.get(), allocationInfo);
	}
	
	public VulkanBufferData createBuffer(VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			return createBuffer(stack, bufferCreateInfo, allocationCreateInfo);
		}
	}
	
	public void waitIdle()
	{
		VK10.vkDeviceWaitIdle(device);
	}
	
	public VkDevice getDevice()
	{
		return device;
	}
	
	public PhysicalDevice getPhysicalDevice()
	{
		return physicalDevice;
	}
	
	public Queue getGraphicsQueue()
	{
		return graphicsQueue;
	}
	
	public void setGraphicsQueue(Queue queue)
	{
		VulkanErrorUtils.match(this, queue.getDevice(), "Cannot set a queue of another device");
		graphicsQueue = queue;
	}
	
	public Queue getPresentQueue()
	{
		return presentQueue;
	}
	
	public void setPresentQueue(Queue queue)
	{
		VulkanErrorUtils.match(this, queue.getDevice(), "Cannot set a queue of another device");
		presentQueue = queue;
	}
	
	public Window getWindow()
	{
		return window;
	}
	
	public long getResetCommandPool()
	{
		return resetCommandPool;
	}
	
	public long getShortLivedCommandPool()
	{
		return shortLivedCommandPool;
	}
	
	@Override
	public void close()
	{
		waitIdle();
		if (window != null) window.close();
		VK10.vkDestroyCommandPool(device, resetCommandPool, null);
		VK10.vkDestroyCommandPool(device, shortLivedCommandPool, null);
		VK10.vkDestroyDevice(device, null);
	}
}
