package com.alcadrial.renderengine.render.buffer;

import java.util.concurrent.Future;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.Debug;
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
	protected long gpuOffset;
	protected long gpuMemory;
	protected int gpuSize;
	private int gpuUsage;
	
	public VulkanDoubleBuffer(Device device, int size, int usage)
	{
		this(device, size, usage, false);
	}
	
	public VulkanDoubleBuffer(Device device, int size, int usage, boolean bidirectional)
	{
		this(device, size, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT | (bidirectional ? VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT : 0), usage | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | (bidirectional ? VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT : 0));
	}
	
	protected VulkanDoubleBuffer(Device device, int size, int localUsage, int gpuUsage)
	{
		super(device, size, localUsage, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
		this.gpuUsage = gpuUsage;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			gpuVk = device.createBuffer(stack, size, gpuUsage);
			gpuOffset = 0;
			gpuMemory = device.allocateBufferMemory(stack, gpuVk, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
			gpuSize = size;
		}
	}
	
	protected VulkanDoubleBuffer(VulkanDoubleBuffer buffer, long offset, int size)
	{
		super(buffer, offset, size);
		gpuUsage = buffer.gpuUsage;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			gpuVk = device.createBuffer(stack, size, gpuUsage);
			gpuOffset = offset;
			gpuMemory = buffer.gpuMemory;
			VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(device.getVk(), gpuVk, gpuMemory, offset), "Failed to bind buffer memory");
			gpuSize = size;
		}
	}
	
	public synchronized Future<?> sendToPhysicalDevice()
	{
		return sendToPhysicalDevice(0, size);
	}
	
	public synchronized Future<?> sendToPhysicalDevice(long offset, int size)
	{
		return sendToPhysicalDevice(offset, size, gpuVk);
	}
	
	public synchronized Future<?> sendToPhysicalDevice(long dstBuffer)
	{
		return sendToPhysicalDevice(0, size, dstBuffer);
	}
	
	public synchronized Future<?> sendToPhysicalDevice(long offset, int size, long dstBuffer)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			CommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			VK10.vkCmdCopyBuffer(commandBuffer.getVk(), vk, dstBuffer, VulkanUtils.bufferCopy(stack, offset, offset, size));
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
	
	public synchronized Future<?> queryFromPhysicalDevice(long offset, int size)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			CommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			VK10.vkCmdCopyBuffer(commandBuffer.getVk(), gpuVk, vk, VulkanUtils.bufferCopy(stack, offset, offset, size));
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
	
	public int getGpuSize()
	{
		return gpuSize;
	}
	
	public int getGpuUsage()
	{
		return gpuUsage;
	}
	
	@Override
	protected void closeCopy()
	{
		super.closeCopy();
		VK10.vkDestroyBuffer(device.getVk(), gpuVk, null);
		gpuVk = 0;
		gpuMemory = 0;
	}
	
	@Override
	protected void closeOriginal()
	{
		super.closeOriginal();
		VK10.vkDestroyBuffer(device.getVk(), gpuVk, null);
		VK10.vkFreeMemory(device.getVk(), gpuMemory, null);
		gpuVk = 0;
		gpuMemory = 0;
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk (cpu): " + Debug.address(vk));
		Debug.print("Memory (cpu): " + Debug.address(memory));
		Debug.print("Offset (cpu): " + offset);
		Debug.print("Size (cpu): " + size);
		Debug.print("Vk (gpu): " + Debug.address(gpuVk));
		Debug.print("Memory (gpu): " + Debug.address(gpuMemory));
		Debug.print("Offset (gpu): " + gpuOffset);
		Debug.print("Size (gpu): " + gpuSize);
	}
}
