package com.alcadrial.renderengine.render.buffer;

import java.util.concurrent.Future;

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
		this(device, size, usage, false);
	}
	
	public VulkanDoubleBuffer(Device device, long size, int usage, boolean bidirectional)
	{
		this(device, size, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT | (bidirectional ? VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT : 0), usage | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | (bidirectional ? VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT : 0));
	}
	
	protected VulkanDoubleBuffer(Device device, long size, int localUsage, int gpuUsage)
	{
		super(device, size, localUsage, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			gpuVk = device.createBuffer(stack, size, gpuUsage);
			gpuMemory = device.allocateBufferMemory(stack, gpuVk, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
			gpuSize = size;
		}
	}
	
	public Future<?> sendToPhysicalDevice()
	{
		return sendToPhysicalDevice(0, size);
	}
	
	public synchronized Future<?> sendToPhysicalDevice(long offset, long size)
	{
		return sendToPhysicalDevice(offset, size, gpuVk);
	}
	
	public synchronized Future<?> sendToPhysicalDevice(long dstBuffer)
	{
		return sendToPhysicalDevice(0, size, dstBuffer);
	}
	
	public synchronized Future<?> sendToPhysicalDevice(long offset, long size, long dstBuffer)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			VK10.vkCmdCopyBuffer(commandBuffer, vk, dstBuffer, VulkanUtils.bufferCopy(stack, offset, offset, size));
			Future<?> future = device.submitSingleUseCommandBuffer(stack, commandBuffer);
			return RenderEngine.getExecutor().submit(() -> {
				future.get();
				gpuSize = size;
				return null;
			});
		}
	}
	
	public synchronized Future<?> queryFromPhysicalDevice()
	{
		return queryFromPhysicalDevice(0, gpuSize);
	}
	
	public synchronized Future<?> queryFromPhysicalDevice(long offset, long size)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			VK10.vkCmdCopyBuffer(commandBuffer, gpuVk, vk, VulkanUtils.bufferCopy(stack, offset, offset, size));
			Future<?> future = device.submitSingleUseCommandBuffer(stack, commandBuffer);
			return RenderEngine.getExecutor().submit(() -> {
				future.get();
				this.size = size;
				return null;
			});
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
