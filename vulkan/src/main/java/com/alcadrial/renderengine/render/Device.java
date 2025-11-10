package com.alcadrial.renderengine.render;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugUtilsObjectNameInfoEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.buffer.Memory;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandPool;
import com.alcadrial.renderengine.render.pipeline.Pipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.queue.PresentQueue;
import com.alcadrial.renderengine.render.queue.Queue;
import com.alcadrial.renderengine.render.queue.QueueFamily;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.texture.TextureView;
import com.alcadrial.renderengine.render.util.AbstractDeviceResource;
import com.alcadrial.renderengine.render.util.AbstractVulkanResource;
import com.alcadrial.renderengine.render.util.OverflowException;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanFuture;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanConsumer;

public class Device extends AbstractVulkanResource<VkDevice> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private Deque<AbstractDeviceResource<?>> destructionList;
	private PhysicalDevice physicalDevice;
	private Queue graphicsQueue;
	private Queue computeQueue;
	private Queue graphicsComputeQueue;
	private PresentQueue presentQueue;
	private CommandPool multiUseCommandPool;
	private CommandPool singleUseCommandPool;
	private Set<Queue> queues;
	
	private Map<Object, String> debugNames;
	
	public Device(VkDevice device, PhysicalDevice physicalDevice, QueueFamily graphicsQueueFamily, QueueFamily computeQueueFamily, QueueFamily graphicsComputeQueueFamily, @Nullable QueueFamily presentQueueFamily) throws VulkanException
	{
		vk = VulkanErrorUtils.nonNull(device);
		this.physicalDevice = VulkanErrorUtils.nonNull(physicalDevice);
		destructionList = new LinkedList<>();
		
		if (RenderEngine.isDebug() && RenderEngine.getVulkan().isDebugUtilsExtensionEnabled()) debugNames = new HashMap<>(1024);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] p = new long[1];
			
			VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
			poolInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
			poolInfo.queueFamilyIndex(physicalDevice.getGraphicsQueueFamily().getIndex());
			poolInfo.flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateCommandPool(device, poolInfo, null, p), "Failed to create reset command pool");
			multiUseCommandPool = new CommandPool(this, p[0]);
			
			poolInfo.flags(VK10.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT);
			VulkanErrorUtils.assertNoError(VK10.vkCreateCommandPool(device, poolInfo, null, p), "Failed to create short lived command pool");
			singleUseCommandPool = new CommandPool(this, p[0]);
			if (RenderEngine.isDebug() && RenderEngine.getVulkan().isDebugUtilsExtensionEnabled())
			{
				multiUseCommandPool.setName("MultiUseCommandPool(" + device + ")");
				singleUseCommandPool.setName("SingleUseCommandPool(" + device + ")");
			}
		}
		queues = new HashSet<>();
		graphicsQueue = Queue.createQueue(this, graphicsQueueFamily.getIndex());
		queues.add(graphicsQueue);
		computeQueue = Queue.createQueue(this, computeQueueFamily.getIndex());
		queues.add(computeQueue);
		graphicsComputeQueue = Queue.createQueue(this, graphicsComputeQueueFamily.getIndex());
		queues.add(graphicsComputeQueue);
		if (presentQueueFamily != null) {
			presentQueue = Queue.createPresentQueue(this, presentQueueFamily.getIndex());
			queues.add(presentQueue);
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
	
	public PhysicalDevice getPhysicalDevice()
	{
		return physicalDevice;
	}
	
	public Queue getGraphicsQueue()
	{
		return graphicsQueue;
	}
	
	public Queue getComputeQueue()
	{
		return computeQueue;
	}
	
	public Queue getGraphicsComputeQueue()
	{
		return graphicsComputeQueue;
	}
	
	public PresentQueue getPresentQueue()
	{
		return presentQueue;
	}
	
	public void useResetCommandPool(VulkanConsumer<CommandPool> action) throws VulkanException
	{
		synchronized (multiUseCommandPool)
		{
			action.accept(multiUseCommandPool);
		}
	}
	
	public void useShortLivedCommandPool(VulkanConsumer<CommandPool> action) throws VulkanException
	{
		synchronized (singleUseCommandPool)
		{
			action.accept(singleUseCommandPool);
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
			
			for (int i = 0; i < count; i++)
			{
				CommandBuffer commandBuffer = new CommandBuffer(this, multiUseCommandPool, new VkCommandBuffer(p.get(), vk));
				Debug.runOnDebug(() -> commandBuffer.setName("MultiUse CommandBuffer " + Debug.getIncrementalId(this)));
				commandBuffers.add(commandBuffer);
			}
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
			CommandBuffer commandBuffer = new CommandBuffer(this, singleUseCommandPool, new VkCommandBuffer(p.get(), vk));
			Debug.runOnDebug(() -> commandBuffer.setName("SingleUse CommandBuffer " + Debug.getIncrementalId(this)));
			
			useShortLivedCommandPool(pool -> {
				commandBuffer.beginRecording();
				configure.accept(commandBuffer);
				commandBuffer.endRecording();
			});
			long fence = createFence(stack, false);
			queue.submit(VulkanUtils.submitInfo(stack, commandBuffer.getVk()), fence);
			destroyFences(fence);
			useShortLivedCommandPool(pool -> commandBuffer.close());
		}
	}
	
	public VulkanFuture<?> configureAndSubmitSingleUseCommandBuffer(Queue queue, VulkanConsumer<CommandBuffer> configure) throws VulkanException
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
			CommandBuffer commandBuffer = new CommandBuffer(this, singleUseCommandPool, new VkCommandBuffer(p.get(), vk));
			Debug.runOnDebug(() -> commandBuffer.setName("SingleUse CommandBuffer " + Debug.getIncrementalId(this)));
			
			useShortLivedCommandPool(pool -> {
				commandBuffer.beginRecording();
				configure.accept(commandBuffer);
				commandBuffer.endRecording();
			});
			return RenderEngine.composeAfter(queue.submitAsync(VulkanUtils.submitInfo(stack, commandBuffer.getVk()), createFence(stack, false)), fences -> {
				destroyFences(fences);
				synchronized (singleUseCommandPool)
				{
					commandBuffer.close();
				}
			});
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
			CommandBuffer commandBuffer = new CommandBuffer(this, singleUseCommandPool, new VkCommandBuffer(p.get(), vk));
			Debug.runOnDebug(() -> commandBuffer.setName("SingleUse CommandBuffer " + Debug.getIncrementalId(this)));
			
			commandBuffer.beginRecording();
			return commandBuffer;
		}
	}
	
	public VulkanFuture<?> submitSingleUseCommandBuffer(MemoryStack stack, CommandBuffer commandBuffer) throws VulkanException
	{
		return submitSingleUseCommandBuffer(stack, commandBuffer, graphicsQueue);
	}
	
	public VulkanFuture<?> submitSingleUseCommandBuffer(MemoryStack stack, CommandBuffer commandBuffer, Queue queue) throws VulkanException
	{
		if (commandBuffer.getDevice() != this) throw new IllegalArgumentException("Cannot submit a command buffer of another device");
		return RenderEngine.composeAfter(queue.submitAsync(VulkanUtils.submitInfo(stack, commandBuffer.getVk()), createFence(stack, false)), fences -> {
			destroyFences(fences);
			synchronized (commandBuffer.getPool())
			{
				commandBuffer.close();
			}
		});
	}
	
	public VulkanFuture<?> submitMultiUseCommandBuffer(MemoryStack stack, CommandBuffer commandBuffer) throws VulkanException
	{
		return submitMultiUseCommandBuffer(stack, commandBuffer, graphicsQueue);
	}
	
	public VulkanFuture<?> submitMultiUseCommandBuffer(MemoryStack stack, CommandBuffer commandBuffer, Queue queue) throws VulkanException
	{
		if (commandBuffer.getDevice() != this) throw new IllegalArgumentException("Cannot submit a command buffer of another device");
		return RenderEngine.composeAfter(queue.submitAsync(VulkanUtils.submitInfo(stack, commandBuffer.getVk()), createFence(stack, false)), fences -> {
			destroyFences(fences);
		});
	}
	
	public VulkanFuture<?> submitAndResetMultiUseCommandBuffer(MemoryStack stack, CommandBuffer commandBuffer) throws VulkanException
	{
		return submitAndResetMultiUseCommandBuffer(stack, commandBuffer, graphicsQueue);
	}
	
	public VulkanFuture<?> submitAndResetMultiUseCommandBuffer(MemoryStack stack, CommandBuffer commandBuffer, Queue queue) throws VulkanException
	{
		if (commandBuffer.getDevice() != this) throw new IllegalArgumentException("Cannot submit a command buffer of another device");
		return RenderEngine.composeAfter(queue.submitAsync(VulkanUtils.submitInfo(stack, commandBuffer.getVk()), createFence(stack, false)), fences -> {
			destroyFences(fences);
			commandBuffer.reset();
		});
	}
	
	public Device waitIdle()
	{
		return waitIdle0(queues.iterator());
	}
	
	private Device waitIdle0(Iterator<Queue> iterator)
	{
		if (!iterator.hasNext()) {
			VK10.vkDeviceWaitIdle(vk);
			return this;
		}
		synchronized (iterator.next())
		{
			return waitIdle0(iterator);
		}
	}
	
	public VulkanFuture<Device> waitIdleAsync()
	{
		return RenderEngine.submitAsync(this::waitIdle);
	}
	
	// FENCES
	
	public long[] waitForFences(long... fences)
	{
		while (VK10.vkWaitForFences(vk, fences, true, 0xFFFFFFFF) == VK10.VK_TIMEOUT);
		return fences;
	}
	
	public VulkanFuture<long[]> waitForFencesAsync(long... fences)
	{
		return RenderEngine.submitAsync(() -> {
			while (VK10.vkWaitForFences(vk, fences, true, 0xFFFFFFFF) == VK10.VK_TIMEOUT);
			return fences;
		});
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
	
	public VkMemoryRequirements getBufferRequirements(MemoryStack stack, VulkanBuffer buffer)
	{
		VkMemoryRequirements memoryRequirements = VkMemoryRequirements.malloc(stack);
		VK10.vkGetBufferMemoryRequirements(vk, buffer.getVk(), memoryRequirements);
		return memoryRequirements;
	}
	
	public VkMemoryRequirements getImageRequirements(MemoryStack stack, Texture texture)
	{
		VkMemoryRequirements memoryRequirements = VkMemoryRequirements.malloc(stack);
		VK10.vkGetImageMemoryRequirements(vk, texture.getVk(), memoryRequirements);
		return memoryRequirements;
	}
	
	public Memory allocateMemory(MemoryStack stack, VulkanBuffer buffer, int requiredProperties, int optimalProperties) throws VulkanException
	{
		Memory memory = allocateMemory(stack, getBufferRequirements(stack, buffer), requiredProperties, optimalProperties);
		VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(vk, buffer.getVk(), memory.getVk(), 0), "Failed to bind buffer memory");
		return memory;
	}
	
	public Memory allocateMemory(MemoryStack stack, Texture texture, int requiredProperties, int optimalProperties) throws VulkanException
	{
		Memory memory = allocateMemory(stack, getImageRequirements(stack, texture), requiredProperties, optimalProperties);
		VulkanErrorUtils.assertNoError(VK10.vkBindImageMemory(vk, texture.getVk(), memory.getVk(), 0), "Failed to bind image memory");
		return memory;
	}
	
	private Memory allocateMemory(MemoryStack stack, VkMemoryRequirements memoryRequirements, int requiredProperties, int optimalProperties) throws VulkanException
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
			if (index == memoryProperties.memoryTypeCount()) throw new OverflowException("Failed to find suitable memory type");
		}
		
		LongBuffer p = stack.mallocLong(1);
		VulkanErrorUtils.assertNoError(VK10.vkAllocateMemory(vk, VulkanUtils.memoryAllocateInfo(stack, memoryRequirements.size(), index), null, p), () -> "Failed to allocate image memory (" + memoryRequirements.size() + " bytes)");
		return new Memory(this, p.get(), (int) memoryRequirements.size());
	}
	
	@Override
	public void close()
	{
		if (vk != null)
		{
			waitIdle();
			int threshold = Properties.DEVICE_DESTRUCTION_LIST_THRESHOLD.getValue();
			if (destructionList.size() > threshold)
			{
				int limit = Math.max(Math.min(destructionList.size(), 100), threshold);
				Debug.debug("Warning: too much unclosed resources (" + destructionList.size() + "), logging the first " + limit);
				
				Iterator<AbstractDeviceResource<?>> iterator = destructionList.iterator();
				for (int i = 0; i < limit; i++) {
					AbstractDeviceResource<?> resource = iterator.next();
					Debug.print(i + " " + resource.getClass(), resource);
				}
			}
			while (destructionList.peek() != null) destructionList.pop().close();
			VK10.vkDestroyDevice(vk, null);
			super.close();
			vk = null;
		}
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
		Debug.print("Single-use command pool: ", singleUseCommandPool);
		Debug.print("Multi-use command pool: ", multiUseCommandPool);
	}
	
	// DEBUG
	
	public String getObjectDebugName(Object object)
	{
		if (RenderEngine.isDebug() && RenderEngine.getVulkan().isDebugUtilsExtensionEnabled())
		{
			return debugNames.get(object);
		}
		return null;
	}
	
	public void setObjectDebugName(Vulkan object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_INSTANCE, object.getInstance().address(), name);
	}
	
	public void setObjectDebugName(Surface object, String name)
	{
		setObjectDebugName(KHRSurface.VK_OBJECT_TYPE_SURFACE_KHR, object.getVk(), name);
	}
	
	public void setObjectDebugName(Swapchain object, String name)
	{
		setObjectDebugName(KHRSwapchain.VK_OBJECT_TYPE_SWAPCHAIN_KHR, object.getVk(), name);
	}
	
	public void setObjectDebugName(PhysicalDevice object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_PHYSICAL_DEVICE, object.getVk().address(), name);
	}
	
	public void setObjectDebugName(Device object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_DEVICE, object.getVk().address(), name);
	}
	
	public void setObjectDebugName(PipelineLayout object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_PIPELINE_LAYOUT, object.getVk(), name);
	}
	
	public void setObjectDebugName(Pipeline object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_PIPELINE, object.getVk(), name);
	}
	
	public void setObjectDebugName(RenderPass object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_RENDER_PASS, object.getVk(), name);
	}
	
	public void setObjectDebugName(TextureSampler object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_SAMPLER, object.getVk(), name);
	}
	
	public void setObjectDebugName(TextureView object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_IMAGE_VIEW, object.getVk(), name);
	}
	
	public void setObjectDebugName(Texture object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_IMAGE, object.getVk(), name);
	}
	
	public void setObjectDebugName(FrameBuffer object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_FRAMEBUFFER, object.getVk(), name);
	}
	
	public void setObjectDebugName(Queue object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_QUEUE, object.getVk().address(), name);
	}
	
	public void setObjectDebugName(Memory object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_DEVICE_MEMORY, object.getVk(), name);
	}
	
	public void setObjectDebugName(VulkanBuffer object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_BUFFER, object.getVk(), name);
	}
	
	public void setObjectDebugName(CommandBuffer object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_COMMAND_BUFFER, object.getVk().address(), name);
	}
	
	public void setObjectDebugName(CommandPool object, String name)
	{
		setObjectDebugName(VK10.VK_OBJECT_TYPE_COMMAND_POOL, object.getVk(), name);
	}
	
	public void setObjectDebugName(int objectType, long object, String name)
	{
		if (RenderEngine.isDebug() && RenderEngine.getVulkan().isDebugUtilsExtensionEnabled())
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkDebugUtilsObjectNameInfoEXT info = VkDebugUtilsObjectNameInfoEXT.malloc(stack);
				info.set(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_OBJECT_NAME_INFO_EXT, 0, objectType, object, stack.UTF8(name));
				EXTDebugUtils.vkSetDebugUtilsObjectNameEXT(vk, info);
				debugNames.put(object, name);
			}
		}
	}
}
