package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
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
			gpuVk = device.createBuffer(stack, size, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | usage);
			gpuMemory = device.allocateBufferMemory(stack, gpuVk, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
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
			VkCommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			VK10.vkCmdCopyBuffer(commandBuffer, vk, gpuVk, VulkanUtils.bufferCopy(stack, offset, offset, size));
			device.submitSingleUseCommandBuffer(stack, commandBuffer);
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
