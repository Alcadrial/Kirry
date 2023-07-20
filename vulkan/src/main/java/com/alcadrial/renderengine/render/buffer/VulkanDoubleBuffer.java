package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;

import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.Pair;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class VulkanDoubleBuffer extends VulkanBuffer {
	
	protected long gpuId;
	protected long gpuMemory;
	protected long gpuSize;
	
	public VulkanDoubleBuffer(long size, int usage)
	{
		super(size, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			Pair<Long, Long> data = build(stack, size, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | usage, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
			gpuId = data.getLeft();
			gpuMemory = data.getRigth();
		}
	}
	
	public void sendToGpu()
	{
		sendToGpu(0, size);
	}
	
	public void sendToGpu(long offset, long size)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.callocPointer(1);
			VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(RenderEngine.getVulkan().getDevice().getDevice(), VulkanUtils.commandBufferAllocateInfo(stack, device.getShortLivedCommandPool(), VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY, 1), p), "Failed to send data to gpu");
			VkCommandBuffer commandBuffer = new VkCommandBuffer(p.get(), device.getDevice());
			
			VulkanErrorUtils.assertNoError(VK10.vkBeginCommandBuffer(commandBuffer, VulkanUtils.commandBufferBeginInfo(stack, VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "Failed to send data to gpu");
			VK10.vkCmdCopyBuffer(commandBuffer, id, gpuId, VulkanUtils.bufferCopy(stack, offset, offset, size));
			VulkanErrorUtils.assertNoError(VK10.vkEndCommandBuffer(commandBuffer), "Failed to send data to gpu");
			
			VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(device.getGraphicsQueue().getQueue(), VulkanUtils.submitInfo(stack, commandBuffer), 0), "Failed to send data to gpu");
			VulkanErrorUtils.assertNoError(VK10.vkQueueWaitIdle(device.getGraphicsQueue().getQueue()), "Failed to send data to gpu");
			
			VK10.vkFreeCommandBuffers(device.getDevice(), device.getShortLivedCommandPool(), commandBuffer);
			
			gpuSize = size;
		}
	}
	
	public long getGpuId()
	{
		return gpuId;
	}
	
	public long getGpuSize()
	{
		return gpuSize;
	}
	
	@Override
	public void close()
	{
		super.close();
		VK10.vkDestroyBuffer(device.getDevice(), gpuId, null);
		VK10.vkFreeMemory(device.getDevice(), gpuMemory, null);
	}
}
