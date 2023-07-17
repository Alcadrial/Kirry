package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.Pair;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class VulkanDoubleBuffer extends VulkanBuffer {
	
	protected long gpuId;
	protected long gpuMemory;
	
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
	
	public void sync(long commandPool)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			Device device = RenderEngine.getVulkan().getDevice();
			
			PointerBuffer p = stack.callocPointer(1);
			VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(RenderEngine.getVulkan().getDevice().getDevice(), VulkanUtils.commandBufferAllocateInfo(stack, commandPool, VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY, 1), p), "a");
			VkCommandBuffer commandBuffer = new VkCommandBuffer(p.get(), device.getDevice());
			
			VulkanErrorUtils.assertNoError(VK10.vkBeginCommandBuffer(commandBuffer, VulkanUtils.commandBufferBeginInfo(stack, VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "b");
			VK10.vkCmdCopyBuffer(commandBuffer, id, gpuId, VulkanUtils.bufferCopy(stack, 0, 0, size));
			VulkanErrorUtils.assertNoError(VK10.vkEndCommandBuffer(commandBuffer), "c");
			
			VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(device.getGraphicsQueue().getQueue(), VulkanUtils.submitInfo(stack, commandBuffer), 0), "d");
			VulkanErrorUtils.assertNoError(VK10.vkQueueWaitIdle(device.getGraphicsQueue().getQueue()), "e");
			
			VK10.vkFreeCommandBuffers(device.getDevice(), commandPool, commandBuffer);
		}
	}
	
	@Override
	public void close()
	{
		super.close();
		VkDevice device = RenderEngine.getVulkan().getDevice().getDevice();
		VK10.vkDestroyBuffer(device, gpuId, null);
		VK10.vkFreeMemory(device, gpuMemory, null);
	}
}
