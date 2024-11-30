package com.alcadrial.renderengine.render;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugMarker;
import org.lwjgl.vulkan.EXTDebugReport;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugMarkerObjectNameInfoEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.Pipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.texture.TextureView;
import com.alcadrial.renderengine.render.util.AbstractDeviceResource;
import com.alcadrial.renderengine.render.util.AbstractVulkanResource;
import com.alcadrial.renderengine.render.util.OutOfSpaceException;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanConsumer;

public class Device extends AbstractVulkanResource<VkDevice> {
	
	private static final int DESTRUCTION_LIST_THRESHOLD = 200;
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private boolean debugReportExtensionEnabled;
	private Deque<AbstractDeviceResource<?>> destructionList;
	private PhysicalDevice physicalDevice;
	private Queue graphicsQueue;
	private Queue computeQueue;
	private Queue graphicsComputeQueue;
	private Queue presentQueue;
	private CommandPool resetCommandPool;
	private CommandPool shortLivedCommandPool;
	
	public Device(VkDevice device, PhysicalDevice physicalDevice, boolean debugReportExtensionEnabled) throws VulkanException
	{
		vk = VulkanErrorUtils.nonNull(device);
		this.debugReportExtensionEnabled = debugReportExtensionEnabled;
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
	
	public void addToDestructionList(AbstractDeviceResource<?> resource)
	{
		if (resource.getDevice() == this) destructionList.push(resource);
	}
	
	public void removeFromDestructionList(AbstractDeviceResource<?> resource)
	{
		destructionList.remove(resource);
	}
	
	private void sync(Queue queue, Runnable r)
	{
		if (queue == null)
		{
			r.run();
			return;
		}
		synchronized (queue)
		{
			r.run();
		}
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
	
	public Queue getComputeQueue()
	{
		return computeQueue;
	}
	
	public void setComputeQueue(Queue queue)
	{
		VulkanErrorUtils.match(this, queue.getDevice(), "Cannot set a queue of another device");
		computeQueue = queue;
	}
	
	public Queue getGraphicsComputeQueue()
	{
		return graphicsComputeQueue;
	}
	
	public void setGraphicsComputeQueue(Queue queue)
	{
		VulkanErrorUtils.match(this, queue.getDevice(), "Cannot set a queue of another device");
		graphicsComputeQueue = queue;
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
	
	public void useResetCommandPool(VulkanConsumer<CommandPool> action) throws VulkanException
	{
		synchronized (resetCommandPool)
		{
			action.accept(resetCommandPool);
		}
	}
	
	public void useShortLivedCommandPool(VulkanConsumer<CommandPool> action) throws VulkanException
	{
		synchronized (shortLivedCommandPool)
		{
			action.accept(shortLivedCommandPool);
		}
	}
	
	// COMMAND BUFFER
	
	public CommandBuffer createMultiUseCommandBuffer() throws VulkanException
	{
		return createMultiUseCommandBuffer(1).get(0);
	}
	
	public List<CommandBuffer> createMultiUseCommandBuffer(int count) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.callocPointer(count);
			useResetCommandPool(pool -> {
				VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
					.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
					.commandPool(pool.getVk())
					.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
					.commandBufferCount(count);
				VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(vk, commandBufferAllocateInfo, p), "Failed to create multi use command buffer");
			});
			List<CommandBuffer> commandBuffers = new ArrayList<>(count);
			
			for (int i = 0; i < count; i++) commandBuffers.add(new CommandBuffer(this, resetCommandPool, new VkCommandBuffer(p.get(), vk)));
			return commandBuffers;
		}
	}
	
	public void configureSingleUseCommandBuffer(Queue queue, VulkanConsumer<CommandBuffer> configure) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.callocPointer(1);
			useShortLivedCommandPool(pool -> {
				VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
					.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
					.commandPool(pool.getVk())
					.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
					.commandBufferCount(1);
				VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(vk, commandBufferAllocateInfo, p), "Failed to create single use command buffer");
			});
			CommandBuffer commandBuffer = new CommandBuffer(this, shortLivedCommandPool, new VkCommandBuffer(p.get(), vk));
			VulkanErrorUtils.assertStatement(commandBuffer.beginRecording(), "Failed to create single use command buffer");
			useShortLivedCommandPool(pool -> configure.accept(commandBuffer));
		}
	}
	
	public CompletableFuture<?> configureAndSubmitSingleUseCommandBuffer(Queue queue, VulkanConsumer<CommandBuffer> configure) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.callocPointer(1);
			useShortLivedCommandPool(pool -> {
				VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
					.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
					.commandPool(pool.getVk())
					.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
					.commandBufferCount(1);
				VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(vk, commandBufferAllocateInfo, p), "Failed to create single use command buffer");
			});
			CommandBuffer commandBuffer = new CommandBuffer(this, shortLivedCommandPool, new VkCommandBuffer(p.get(), vk));
			VulkanErrorUtils.assertStatement(commandBuffer.beginRecording(), "Failed to create single use command buffer");
			useShortLivedCommandPool(pool -> configure.accept(commandBuffer));
			return submitSingleUseCommandBuffer(stack, commandBuffer, queue);
		}
	}
	
	public CommandBuffer createSingleUseCommandBuffer() throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.callocPointer(1);
			useShortLivedCommandPool(pool -> {
				VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
					.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
					.commandPool(pool.getVk())
					.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
					.commandBufferCount(1);
				VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(vk, commandBufferAllocateInfo, p), "Failed to create single use command buffer");
			});
			CommandBuffer commandBuffer = new CommandBuffer(this, shortLivedCommandPool, new VkCommandBuffer(p.get(), vk));
			VulkanErrorUtils.assertStatement(commandBuffer.beginRecording(), "Failed to create single use command buffer");
			return commandBuffer;
		}
	}
	
	public CompletableFuture<?> submitSingleUseCommandBuffer(MemoryStack stack, CommandBuffer commandBuffer) throws VulkanException
	{
		return submitSingleUseCommandBuffer(stack, commandBuffer, graphicsQueue);
	}
	
	public CompletableFuture<?> submitSingleUseCommandBuffer(MemoryStack stack, CommandBuffer commandBuffer, Queue queue) throws VulkanException
	{
		if (commandBuffer.getDevice() != this) throw new IllegalArgumentException("Cannot submit a command buffer of another device");
		VulkanErrorUtils.assertStatement(commandBuffer.endRecording(), "Command buffer is not recording");
		long fence = createFence(stack, false);
		queue.submit(VulkanUtils.submitInfo(stack, commandBuffer.getVk()), fence);
		return waitForFences(fence).thenAccept(fences -> {
			destroyFences(fence);
			commandBuffer.close();
		});
	}
	
	public CompletableFuture<?> submitMultiUseCommandBuffer(CommandBuffer commandBuffer, Queue queue) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			if (commandBuffer.getDevice() != this) throw new IllegalArgumentException("Cannot submit a command buffer of another device");
			VulkanErrorUtils.assertStatement(commandBuffer.endRecording(), "Command buffer is not recording");
			long fence = createFence(stack, false);
			queue.submit(VulkanUtils.submitInfo(stack, commandBuffer.getVk()), fence);
			return waitForFences(fence).thenAccept(this::destroyFences);
		}
	}
	
	public CompletableFuture<Device> waitIdle()
	{
		return CompletableFuture.supplyAsync(() -> {
			sync(graphicsQueue, () -> {
				Queue q = null;
				if (computeQueue != graphicsQueue) q = computeQueue;
				sync(q, () -> {
					Queue q0 = null;
					if (presentQueue != graphicsQueue && presentQueue != computeQueue) q0 = computeQueue;
					sync(q0, () -> VK10.vkDeviceWaitIdle(vk));
				});
			});
			return this;
		}, RenderEngine.getExecutor());
	}
	
	// FENCES
	
	/**
	 *
	 * @param fences
	 */
	public CompletableFuture<long[]> waitForFences(long... fences)
	{
		return CompletableFuture.supplyAsync(() -> {
			while (VK10.vkWaitForFences(vk, fences, true, 0xFFFFFFFF) == VK10.VK_TIMEOUT);
			return fences;
		}, RenderEngine.getExecutor());
	}
	
	public long createFence(MemoryStack stack, boolean signaled) throws VulkanException
	{
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateFence(vk, VkFenceCreateInfo.calloc(stack).set(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO, 0, signaled ? VK10.VK_FENCE_CREATE_SIGNALED_BIT : 0), null, p), "Failed to create fence");
		return p[0];
	}
	
	public void resetFences(long... fences) throws VulkanException
	{
		VulkanErrorUtils.assertNoError(VK10.vkResetFences(vk, fences), "Failed to reset fences");
	}
	
	public void destroyFences(long... fences)
	{
		for (long fence : fences) VK10.vkDestroyFence(vk, fence, null);
	}
	
	public long createBuffer(MemoryStack stack, long size, int usage) throws VulkanException
	{
		VkBufferCreateInfo bufferInfo = VulkanUtils.bufferInfo(stack, size, usage);
		bufferInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateBuffer(vk, bufferInfo, null, p), "Failed to create buffer");
		return p[0];
	}
	
	// MEMORY
	
	public VkMemoryRequirements getMemoryRequirements(VulkanBuffer buffer)
	{
		VkMemoryRequirements memoryRequirements = VkMemoryRequirements.malloc();
		VK10.vkGetBufferMemoryRequirements(vk, buffer.getVk(), memoryRequirements);
		return memoryRequirements;
	}
	
	public VkMemoryRequirements getBufferRequirements(MemoryStack stack, VulkanBuffer buffer)
	{
		VkMemoryRequirements memoryRequirements = VkMemoryRequirements.malloc(stack);
		VK10.vkGetBufferMemoryRequirements(vk, buffer.getVk(), memoryRequirements);
		return memoryRequirements;
	}
	
	public VkMemoryRequirements getMemoryRequirements(Texture texture)
	{
		VkMemoryRequirements memoryRequirements = VkMemoryRequirements.malloc();
		VK10.vkGetImageMemoryRequirements(vk, texture.getVk(), memoryRequirements);
		return memoryRequirements;
	}
	
	public VkMemoryRequirements getMemoryRequirements(MemoryStack stack, Texture texture)
	{
		VkMemoryRequirements memoryRequirements = VkMemoryRequirements.malloc(stack);
		VK10.vkGetImageMemoryRequirements(vk, texture.getVk(), memoryRequirements);
		return memoryRequirements;
	}
	
	public long allocateMemory(MemoryStack stack, VulkanBuffer buffer, int requiredProperties, int optimalProperties) throws VulkanException
	{
		long memory = allocateMemory(stack, getBufferRequirements(stack, buffer), requiredProperties, optimalProperties);
		VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(vk, buffer.getVk(), memory, 0), "Failed to bind buffer memory");
		return memory;
	}
	
	public long allocateMemory(MemoryStack stack, Texture texture, int requiredProperties, int optimalProperties) throws VulkanException
	{
		long memory = allocateMemory(stack, getMemoryRequirements(stack, texture), requiredProperties, optimalProperties);
		VulkanErrorUtils.assertNoError(VK10.vkBindImageMemory(vk, texture.getVk(), memory, 0), "Failed to bind image memory");
		return memory;
	}
	
	private long allocateMemory(MemoryStack stack, VkMemoryRequirements memoryRequirements, int requiredProperties, int optimalProperties) throws VulkanException
	{
		if (memoryRequirements.size() == 0) throw new IllegalArgumentException("Cannot allocate 0 bytes memory");
		VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.malloc(stack);
		VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice.getVk(), memoryProperties);
		
		int minimal = requiredProperties;
		int optimal = minimal | optimalProperties;
		
		int index;
		for (index = 0; index < memoryProperties.memoryTypeCount(); index++)
		{
			if ((memoryRequirements.memoryTypeBits() & (1 << index)) != 0 && (memoryProperties.memoryTypes(index).propertyFlags() & optimal) == optimal)
			{
				break;
			}
		}
		
		if (index == memoryProperties.memoryTypeCount())
		{
			for (index = 0; index < memoryProperties.memoryTypeCount(); index++)
			{
				if ((memoryRequirements.memoryTypeBits() & (1 << index)) != 0 && (memoryProperties.memoryTypes(index).propertyFlags() & minimal) == minimal)
				{
					break;
				}
			}
			if (index == memoryProperties.memoryTypeCount()) throw new OutOfSpaceException("Failed to find suitable memory type");
		}
		
		LongBuffer p = stack.mallocLong(1);
		VulkanErrorUtils.assertNoError(VK10.vkAllocateMemory(vk, VulkanUtils.memoryAllocateInfo(stack, memoryRequirements.size(), index), null, p), () -> "Failed to allocate image memory (" + memoryRequirements.size() + " bytes)");
		return p.get();
	}
	
	@Override
	public void close()
	{
		waitIdle().thenRun(() -> {
			if (destructionList.size() > DESTRUCTION_LIST_THRESHOLD)
			{
				Debug.debug("Warning: too much unclosed resources (" + destructionList.size() + "), logging the first " + DESTRUCTION_LIST_THRESHOLD);
				
				AbstractDeviceResource<?>[] resources = destructionList.stream().limit(DESTRUCTION_LIST_THRESHOLD).toArray(AbstractDeviceResource[]::new);
				
				for (int i = 0; i < resources.length; i++) Debug.print(i + " " + resources[i].getClass(), resources[i]);
			}
			while (destructionList.peek() != null) destructionList.pop().close();
			VK10.vkDestroyDevice(vk, null);
		}).join();
		super.close();
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
	
	// DEBUG
	
	public void setObjectDebugName(Vulkan object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_INSTANCE_EXT, object.getInstance().address(), name);
	}
	
	public void setObjectDebugName(Surface object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_SURFACE_KHR_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(Swapchain object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_SWAPCHAIN_KHR_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(PhysicalDevice object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_PHYSICAL_DEVICE_EXT, object.getVk().address(), name);
	}
	
	public void setObjectDebugName(Device object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_DEVICE_EXT, object.getVk().address(), name);
	}
	
	public void setObjectDebugName(PipelineLayout object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_PIPELINE_LAYOUT_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(Pipeline object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_PIPELINE_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(RenderPass object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_RENDER_PASS_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(TextureSampler object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_SAMPLER_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(TextureView object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_IMAGE_VIEW_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(Texture object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_IMAGE_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(FrameBuffer object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_FRAMEBUFFER_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(Queue object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_QUEUE_EXT, object.getVk().address(), name);
	}
	
	public void setObjectDebugName(VulkanBuffer object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_BUFFER_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(CommandBuffer object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_COMMAND_BUFFER_EXT, object.getVk().address(), name);
	}
	
	public void setObjectDebugName(CommandPool object, String name)
	{
		setObjectDebugName(EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_COMMAND_POOL_EXT, object.getVk(), name);
	}
	
	public void setObjectDebugName(int objectType, long object, String name)
	{
		if (RenderEngine.isDebug() && debugReportExtensionEnabled)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkDebugMarkerObjectNameInfoEXT info = VkDebugMarkerObjectNameInfoEXT.malloc(stack);
				info.set(EXTDebugMarker.VK_STRUCTURE_TYPE_DEBUG_MARKER_OBJECT_NAME_INFO_EXT, 0, objectType, object, stack.UTF8(name));
				EXTDebugMarker.vkDebugMarkerSetObjectNameEXT(vk, info);
			}
		}
	}
}
