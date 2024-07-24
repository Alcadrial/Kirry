package com.alcadrial.renderengine.render.buffer;

import java.util.concurrent.Future;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class VulkanDoubleBuffer extends VulkanBuffer {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected VulkanBuffer gpuBuffer;
	
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
		gpuBuffer = new VulkanBuffer(device, size, gpuUsage);
	}
	
	protected VulkanDoubleBuffer(VulkanDoubleBuffer buffer, long offset, int size)
	{
		super(buffer, offset, size);
		gpuBuffer = new VulkanBuffer(buffer.gpuBuffer, offset, size);
	}
	
	protected VulkanDoubleBuffer(VulkanBuffer gpuBuffer)
	{
		super(gpuBuffer.getDevice(), gpuBuffer.size, gpuBuffer.getUsage());
		this.gpuBuffer = new VulkanBuffer(gpuBuffer, 0, gpuBuffer.size);
	}
	
	public synchronized Future<?> sendToPhysicalDevice()
	{
		return sendToPhysicalDevice(0, size);
	}
	
	public synchronized Future<?> sendToPhysicalDevice(long offset, int size)
	{
		return copyToBuffer(offset, size, gpuBuffer);
	}
	
	public synchronized Future<?> sendToPhysicalDevice(VulkanBuffer dstBuffer)
	{
		return copyToBuffer(0, size, dstBuffer);
	}
	
	public synchronized Future<?> queryFromPhysicalDevice()
	{
		return queryFromPhysicalDevice(0, size);
	}
	
	public synchronized Future<?> queryFromPhysicalDevice(long offset, int size)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			CommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			VK10.vkCmdCopyBuffer(commandBuffer.getVk(), gpuBuffer.getVk(), vk, VulkanUtils.bufferCopy(stack, offset, offset, size));
			Future<?> future = device.submitSingleUseCommandBuffer(stack, commandBuffer);
			return RenderEngine.getExecutor().submit(() -> {
				future.get();
				this.size = size;
				return null;
			});
		}
	}
	
	public VulkanBuffer getGpuBuffer()
	{
		return gpuBuffer;
	}
	
	@Override
	public void close()
	{
		super.close();
		gpuBuffer.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Host local {");
		Debug.indent();
		super.debug();
		Debug.deindent();
		Debug.print("}");
		Debug.print("Device local", gpuBuffer);
	}
}
