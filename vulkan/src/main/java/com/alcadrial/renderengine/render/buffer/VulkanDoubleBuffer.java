package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;

import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class VulkanDoubleBuffer extends VulkanBuffer {

	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected long gpuVk;
	protected long gpuMemory;
	protected long gpuSize;
	
	public VulkanDoubleBuffer(Device device, long size, int usage)
	{
		super(device, size, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			Pair<Long, Long> data = build(stack, size, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | usage, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
			gpuVk = data.l();
			gpuMemory = data.r();
		}
	}
	
	public void sendToPhysicalDevice()
	{
		sendToPhysicalDevice(0, size);
	}
	
	public synchronized void sendToPhysicalDevice(long offset, long size)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.callocPointer(1);
			VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(device.getVk(), VulkanUtils.commandBufferAllocateInfo(stack, device.getShortLivedCommandPool(), VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY, 1), p), "Failed to send data to physical device");
			VkCommandBuffer commandBuffer = new VkCommandBuffer(p.get(), device.getVk());
			
			VulkanErrorUtils.assertNoError(VK10.vkBeginCommandBuffer(commandBuffer, VulkanUtils.commandBufferBeginInfo(stack, VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "Failed to send data to physical device");
			VK10.vkCmdCopyBuffer(commandBuffer, vk, gpuVk, VulkanUtils.bufferCopy(stack, offset, offset, size));
			VulkanErrorUtils.assertNoError(VK10.vkEndCommandBuffer(commandBuffer), "Failed to send data to physical device");
			
			VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(device.getGraphicsQueue().getVk(), VulkanUtils.submitInfo(stack, commandBuffer), 0), "Failed to send data to physical device");
			VulkanErrorUtils.assertNoError(VK10.vkQueueWaitIdle(device.getGraphicsQueue().getVk()), "Failed to send data to physical device");
			
			VK10.vkFreeCommandBuffers(device.getVk(), device.getShortLivedCommandPool(), commandBuffer);
			
			gpuSize = size;
		}
	}
	
	public long getGpuVk()
	{
		return gpuVk;
	}
	
	public long getGpuSize()
	{
		return gpuSize;
	}
	
	@Override
	public void close()
	{
		synchronized (closingMonitor)
		{
			super.close();
			VK10.vkDestroyBuffer(device.getVk(), gpuVk, null);
			VK10.vkFreeMemory(device.getVk(), gpuMemory, null);
			gpuVk = 0;
			gpuMemory = 0;
		}
	}
}
