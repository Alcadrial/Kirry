package com.alcadrial.renderengine.render;

import java.nio.LongBuffer;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.function.Consumer;

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
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import com.alcadrial.renderengine.core.util.WorkInProgress;
import com.alcadrial.renderengine.render.buffer.VulkanBufferData;
import com.alcadrial.renderengine.render.util.AbstractVulkanResource;
import com.alcadrial.renderengine.render.util.DeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class Device extends AbstractVulkanResource<VkDevice> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private Deque<DeviceResource<?>> destructionList;
	private PhysicalDevice physicalDevice;
	private Queue graphicsQueue;
	private Queue presentQueue;
	private long resetCommandPool;
	private long shortLivedCommandPool;
	private long vmaAllocator;
	
	public Device(VkDevice device, PhysicalDevice physicalDevice)
	{
		vk = VulkanErrorUtils.nonNull(device);
		this.physicalDevice = VulkanErrorUtils.nonNull(physicalDevice);
		destructionList = new LinkedList<>();
		
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
	@WorkInProgress
	public void initializeVma()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VmaVulkanFunctions functions = VmaVulkanFunctions.calloc(stack);
			
			functions.vkGetPhysicalDeviceProperties(VK10.vkGetInstanceProcAddr(RenderEngine.getVulkan().getInstance(), "vkGetPhysicalDeviceProperties"));
			functions.vkGetPhysicalDeviceMemoryProperties(VK10.vkGetInstanceProcAddr(RenderEngine.getVulkan().getInstance(), "vkGetPhysicalDeviceMemoryProperties"));
			functions.vkAllocateMemory(VK10.vkGetDeviceProcAddr(vk, "vkAllocateMemory"));
			functions.vkFreeMemory(VK10.vkGetDeviceProcAddr(vk, "vkFreeMemory"));
			functions.vkMapMemory(VK10.vkGetDeviceProcAddr(vk, "vkMapMemory"));
			functions.vkUnmapMemory(VK10.vkGetDeviceProcAddr(vk, "vkUnmapMemory"));
			functions.vkFlushMappedMemoryRanges(VK10.vkGetDeviceProcAddr(vk, "vkFlushMappedMemoryRanges"));
			functions.vkInvalidateMappedMemoryRanges(VK10.vkGetDeviceProcAddr(vk, "vkInvalidateMappedMemoryRanges"));
			functions.vkBindBufferMemory(VK10.vkGetDeviceProcAddr(vk, "vkBindBufferMemory"));
			functions.vkBindImageMemory(VK10.vkGetDeviceProcAddr(vk, "vkBindImageMemory"));
			functions.vkGetBufferMemoryRequirements(VK10.vkGetDeviceProcAddr(vk, "vkGetBufferMemoryRequirements"));
			functions.vkGetImageMemoryRequirements(VK10.vkGetDeviceProcAddr(vk, "vkGetImageMemoryRequirements"));
			functions.vkCreateBuffer(VK10.vkGetDeviceProcAddr(vk, "vkCreateBuffer"));
			functions.vkDestroyBuffer(VK10.vkGetDeviceProcAddr(vk, "vkDestroyBuffer"));
			functions.vkCreateImage(VK10.vkGetDeviceProcAddr(vk, "vkCreateImage"));
			functions.vkDestroyImage(VK10.vkGetDeviceProcAddr(vk, "vkDestroyImage"));
			functions.vkCmdCopyBuffer(VK10.vkGetDeviceProcAddr(vk, "vkCmdCopyBuffer"));
			functions.vkGetInstanceProcAddr(VK10.vkGetInstanceProcAddr(RenderEngine.getVulkan().getInstance(), "vkGetInstanceProcAddr"));
			functions.vkGetDeviceProcAddr(VK10.vkGetDeviceProcAddr(vk, "vkGetDeviceProcAddr"));
			
			VmaAllocatorCreateInfo allocatorInfo = VmaAllocatorCreateInfo.calloc(stack);
			allocatorInfo.flags(Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT);
			allocatorInfo.vulkanApiVersion(VK12.VK_API_VERSION_1_2);
			allocatorInfo.physicalDevice(physicalDevice.getVk());
			allocatorInfo.device(vk);
			allocatorInfo.instance(RenderEngine.getVulkan().getInstance());
			allocatorInfo.pVulkanFunctions(functions);
			
			PointerBuffer p = stack.callocPointer(1);
			Vma.vmaCreateAllocator(allocatorInfo, p);
			vmaAllocator = p.get();
		}
	}
	
	@WorkInProgress
	public VulkanBufferData createBuffer(MemoryStack stack, VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo)
	{
		LongBuffer p = stack.callocLong(1);
		PointerBuffer p0 = stack.callocPointer(1);
		VmaAllocationInfo allocationInfo = VmaAllocationInfo.calloc();
		VulkanErrorUtils.assertNoError(Vma.vmaCreateBuffer(vmaAllocator, bufferCreateInfo, allocationCreateInfo, p, p0, allocationInfo), "Failed to create buffer");
		return new VulkanBufferData(p.get(), p0.get(), allocationInfo);
	}
	
	@WorkInProgress
	public VulkanBufferData createBuffer(VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			return createBuffer(stack, bufferCreateInfo, allocationCreateInfo);
		}
	}
	
	public void addToDestructionList(DeviceResource<?> resource)
	{
		if (resource.getDevice() == this) destructionList.add(resource);
	}
	
	public void removeFromDestructionList(DeviceResource<?> resource)
	{
		destructionList.remove(resource);
	}
	
	public synchronized void waitIdle()
	{
		VK10.vkDeviceWaitIdle(vk);
	}
	
	public Future<?> waitAsyncIdle(Runnable action)
	{
		return RenderEngine.getExecutor().submit(() -> {
			waitIdle();
			action.run();
		});
	}
	
	public Future<?> waitAsyncIdle(Consumer<Device> action)
	{
		return RenderEngine.getExecutor().submit(() -> {
			waitIdle();
			action.accept(this);
		});
	}
	
	/**
	 * 
	 * @param fences
	 * @renderengine.vulkan 1.2
	 */
	public synchronized void waitForFences(long... fences)
	{
		while (VK12.vkWaitForFences(vk, fences, true, 0xFFFFFFFF) == VK10.VK_TIMEOUT);
	}
	
	/**
	 * 
	 * @param action
	 * @param fences
	 * @renderengine.vulkan 1.2
	 */
	public synchronized Future<?> waitAsyncForFences(Runnable action, long... fences)
	{
		return RenderEngine.getExecutor().submit(() -> {
			waitForFences(fences);
			action.run();
		});
	}
	
	/**
	 * 
	 * @param action
	 * @param fences
	 * @renderengine.vulkan 1.2
	 */
	public synchronized Future<?> waitAsyncForFences(Consumer<Device> action, long... fences)
	{
		return RenderEngine.getExecutor().submit(() -> {
			waitForFences(fences);
			action.accept(this);
		});
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
	
	public long getResetCommandPool()
	{
		return resetCommandPool;
	}
	
	public long getShortLivedCommandPool()
	{
		return shortLivedCommandPool;
	}
	
	public VkCommandBuffer createSingleUseCommandBuffer(MemoryStack stack)
	{
		PointerBuffer p = stack.callocPointer(1);
		VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(vk, VulkanUtils.commandBufferAllocateInfo(stack, shortLivedCommandPool, VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY, 1), p), "Failed to create single use command buffer");
		VkCommandBuffer commandBuffer = new VkCommandBuffer(p.get(), vk);
		VulkanErrorUtils.assertNoError(VK10.vkBeginCommandBuffer(commandBuffer, VulkanUtils.commandBufferBeginInfo(stack, VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "Failed to create single use command buffer");
		return commandBuffer;
	}
	
	public void submitSingleUseCommandBuffer(MemoryStack stack, VkCommandBuffer commandBuffer)
	{
		VulkanErrorUtils.assertStatement(commandBuffer.getDevice() == vk, "Cannot submit a command buffer of another device");
		VulkanErrorUtils.assertNoError(VK10.vkEndCommandBuffer(commandBuffer), "Failed to submit command buffer");
		VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(graphicsQueue.getVk(), VulkanUtils.submitInfo(stack, commandBuffer), 0), "Failed to submit command buffer");
		VulkanErrorUtils.assertNoError(VK10.vkQueueWaitIdle(graphicsQueue.getVk()), "Failed to submit command buffer");
		
		VK10.vkFreeCommandBuffers(vk, shortLivedCommandPool, commandBuffer);
	}
	
	public long createBuffer(MemoryStack stack, long size, int usage)
	{
		VkBufferCreateInfo bufferInfo = VulkanUtils.bufferInfo(stack, size, usage);
		bufferInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateBuffer(vk, bufferInfo, null, p), "Failed to create buffer");
		return p[0];
	}
	
	public long allocateBufferMemory(MemoryStack stack, long vkBuffer, int properties)
	{
		VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
		VK10.vkGetBufferMemoryRequirements(vk, vkBuffer, memRequirements);
		
		int index;
		VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.malloc(stack);
		VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice.getVk(), memProperties);
		
		for (index = 0; index < memProperties.memoryTypeCount(); index++)
		{
			if ((memRequirements.memoryTypeBits() & (1 << index)) != 0 && (memProperties.memoryTypes(index).propertyFlags() & properties) == properties)
			{
				break;
			}
		}
		
		VulkanErrorUtils.assertStatement(index < memProperties.memoryTypeCount(), "Failed to find suitable memory type");
		
		LongBuffer p = stack.mallocLong(1);
		VulkanErrorUtils.assertNoError(VK10.vkAllocateMemory(vk, VulkanUtils.memoryAllocateInfo(stack, memRequirements.size(), index), null, p), "Failed to allocate buffer memory");
		
		VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(vk, vkBuffer, p.get(0), 0), "Failed to bind buffer memory");
		return p.get(0);
	}
	
	public long allocateImageMemory(MemoryStack stack, long vkBuffer, int properties)
	{
		VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
		VK10.vkGetImageMemoryRequirements(vk, vkBuffer, memRequirements);
		
		int index;
		VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.malloc(stack);
		VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice.getVk(), memProperties);
		
		for (index = 0; index < memProperties.memoryTypeCount(); index++)
		{
			if ((memRequirements.memoryTypeBits() & (1 << index)) != 0 && (memProperties.memoryTypes(index).propertyFlags() & properties) == properties)
			{
				break;
			}
		}
		
		VulkanErrorUtils.assertStatement(index < memProperties.memoryTypeCount(), "Failed to find suitable memory type");
		
		LongBuffer p = stack.mallocLong(1);
		VulkanErrorUtils.assertNoError(VK10.vkAllocateMemory(vk, VulkanUtils.memoryAllocateInfo(stack, memRequirements.size(), index), null, p), "Failed to allocate image memory");
		
		VulkanErrorUtils.assertNoError(VK10.vkBindImageMemory(vk, vkBuffer, p.get(0), 0), "Failed to bind image memory");
		return p.get(0);
	}
	
	@Override
	public void close()
	{
		waitIdle();
		while (destructionList.peek() != null) destructionList.poll().close();
		VK10.vkDestroyCommandPool(vk, resetCommandPool, null);
		VK10.vkDestroyCommandPool(vk, shortLivedCommandPool, null);
		VK10.vkDestroyDevice(vk, null);
	}
}
