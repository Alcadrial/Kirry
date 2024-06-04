package com.alcadrial.renderengine.render;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
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
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import com.alcadrial.renderengine.core.util.WorkInProgress;
import com.alcadrial.renderengine.render.buffer.VulkanBufferData;
import com.alcadrial.renderengine.render.util.AbstractDeviceResource;
import com.alcadrial.renderengine.render.util.AbstractVulkanResource;
import com.alcadrial.renderengine.render.util.OutOfSpaceException;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class Device extends AbstractVulkanResource<VkDevice> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private Deque<AbstractDeviceResource<?>> destructionList;
	private PhysicalDevice physicalDevice;
	private Queue graphicsQueue;
	private Queue presentQueue;
	private CommandPool resetCommandPool;
	private CommandPool shortLivedCommandPool;
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
			poolInfo.queueFamilyIndex(physicalDevice.getGraphicsQueueFamily().getIndex());
			poolInfo.flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateCommandPool(device, poolInfo, null, p), "Failed to create reset command pool");
			resetCommandPool = new CommandPool(this, p[0]);
			
			poolInfo.flags(VK10.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT);
			VulkanErrorUtils.assertNoError(VK10.vkCreateCommandPool(device, poolInfo, null, p), "Failed to create short lived command pool");
			shortLivedCommandPool = new CommandPool(this, p[0]);
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
	
	public void addToDestructionList(AbstractDeviceResource<?> resource)
	{
		if (resource.getDevice() == this) destructionList.add(resource);
	}
	
	public void removeFromDestructionList(AbstractDeviceResource<?> resource)
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
	 */
	public synchronized void waitForFences(long... fences)
	{
		while (VK10.vkWaitForFences(vk, fences, true, 0xFFFFFFFF) == VK10.VK_TIMEOUT);
	}
	
	/**
	 * 
	 * @param action
	 * @param fences
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
	
	public void useResetCommandPool(Consumer<Long> action)
	{
		synchronized (resetCommandPool)
		{
			action.accept(resetCommandPool.getVk());
		}
	}
	
	public void useShortLivedCommandPool(Consumer<Long> action)
	{
		synchronized (shortLivedCommandPool)
		{
			action.accept(shortLivedCommandPool.getVk());
		}
	}
	
	public VkCommandBuffer createMultiUseCommandBuffer(MemoryStack stack)
	{
		return createMultiUseCommandBuffer(stack, 1).get(0);
	}
	
	public List<VkCommandBuffer> createMultiUseCommandBuffer(MemoryStack stack, int count)
	{
		PointerBuffer p = stack.callocPointer(count);
		useResetCommandPool(pool -> VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(vk, VulkanUtils.commandBufferAllocateInfo(stack, pool, VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY, count), p), "Failed to create multi use command buffer"));
		List<VkCommandBuffer> commandBuffers = new ArrayList<>(count);
		
		for (int i = 0; i < count; i++)
		{
			commandBuffers.add(new VkCommandBuffer(p.get(), vk));
			VulkanErrorUtils.assertNoError(VK10.vkBeginCommandBuffer(commandBuffers.get(i), VulkanUtils.commandBufferBeginInfo(stack, 0)), "Failed to create multi use command buffer");
		}
		return commandBuffers;
	}
	
	public VkCommandBuffer createSingleUseCommandBuffer(MemoryStack stack)
	{
		PointerBuffer p = stack.callocPointer(1);
		useShortLivedCommandPool(pool -> VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(vk, VulkanUtils.commandBufferAllocateInfo(stack, pool, VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY, 1), p), "Failed to create single use command buffer"));
		VkCommandBuffer commandBuffer = new VkCommandBuffer(p.get(), vk);
		VulkanErrorUtils.assertNoError(VK10.vkBeginCommandBuffer(commandBuffer, VulkanUtils.commandBufferBeginInfo(stack, VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "Failed to create single use command buffer");
		return commandBuffer;
	}
	
	public Future<?> submitSingleUseCommandBuffer(MemoryStack stack, VkCommandBuffer commandBuffer)
	{
		return submitSingleUseCommandBuffer(stack, commandBuffer, graphicsQueue);
	}
	
	public Future<?> submitSingleUseCommandBuffer(MemoryStack stack, VkCommandBuffer commandBuffer, Queue queue)
	{
		if (commandBuffer.getDevice() != vk) throw new IllegalArgumentException("Cannot submit a command buffer of another device");
		VulkanErrorUtils.assertNoError(VK10.vkEndCommandBuffer(commandBuffer), "Failed to submit command buffer");
		long fence = createFence(stack, false);
		queue.submit(VulkanUtils.submitInfo(stack, commandBuffer), fence);
		return RenderEngine.getExecutor().submit(() -> {
			waitForFences(fence);
			useShortLivedCommandPool(pool -> VK10.vkFreeCommandBuffers(vk, pool, commandBuffer));
			destroyFence(fence);
		});
	}
	
	public long createFence(MemoryStack stack, boolean signaled)
	{
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateFence(vk, VkFenceCreateInfo.calloc(stack).set(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO, 0, signaled ? VK10.VK_FENCE_CREATE_SIGNALED_BIT : 0), null, p), "Failed to create fence");
		return p[0];
	}
	
	public void resetFence(long... fence)
	{
		VulkanErrorUtils.assertNoError(VK10.vkResetFences(vk, fence), "Failed to reset fences");
	}
	
	public void destroyFence(long fence)
	{
		VK10.vkDestroyFence(vk, fence, null);
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
		long memory = allocateMemory(stack, memRequirements, properties);
		VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(vk, vkBuffer, memory, 0), "Failed to bind buffer memory");
		return memory;
	}
	
	public long allocateImageMemory(MemoryStack stack, long vkBuffer, int properties)
	{
		VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
		VK10.vkGetImageMemoryRequirements(vk, vkBuffer, memRequirements);
		long memory = allocateMemory(stack, memRequirements, properties);
		VulkanErrorUtils.assertNoError(VK10.vkBindImageMemory(vk, vkBuffer, memory, 0), "Failed to bind image memory");
		return memory;
	}
	
	private long allocateMemory(MemoryStack stack, VkMemoryRequirements memRequirements, int properties)
	{
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
		
		if (index == memProperties.memoryTypeCount()) throw new OutOfSpaceException("Failed to find suitable memory type");
		
		LongBuffer p = stack.mallocLong(1);
		VulkanErrorUtils.assertNoError(VK10.vkAllocateMemory(vk, VulkanUtils.memoryAllocateInfo(stack, memRequirements.size(), index), null, p), "Failed to allocate image memory");
		return p.get();
	}
	
	@Override
	public void close()
	{
		waitIdle();
		while (destructionList.peek() != null) destructionList.poll().close();
		VK10.vkDestroyDevice(vk, null);
	}
	
	@Override
	public void debug()
	{
		Debug.print("vk: " + Debug.address(vk));
		if (presentQueue == graphicsQueue)
		{
			Debug.print("Queue (Graphics|Present)", graphicsQueue);
		}
		else
		{
			Debug.print("Queue (Graphics)", graphicsQueue);
			Debug.print("Queue (Present)", presentQueue);
		}
		Debug.print("Single-use command pool: ", shortLivedCommandPool);
		Debug.print("Multi-use command pool: ", resetCommandPool);
	}
}
