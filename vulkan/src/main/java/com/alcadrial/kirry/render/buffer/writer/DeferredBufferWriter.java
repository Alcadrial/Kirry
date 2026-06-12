package com.alcadrial.kirry.render.buffer.writer;

import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class DeferredBufferWriter extends AbstractBufferWriter<DeferredBufferWriter> {
	
	public DeferredBufferWriter(VulkanBuffer srcDstBuffer) throws VulkanException
	{
		this(srcDstBuffer, srcDstBuffer);
	}
	
	public DeferredBufferWriter(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer) throws VulkanException
	{
		this(srcBuffer, dstBuffer, 0, dstBuffer.getSize());
	}
	
	public DeferredBufferWriter(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int offset, int size) throws VulkanException
	{
		this(srcBuffer, offset, dstBuffer, offset, size);
	}
	
	public DeferredBufferWriter(VulkanBuffer srcBuffer, int srcOffset, VulkanBuffer dstBuffer, int dstOffset, int size) throws VulkanException
	{
		super(srcBuffer, srcOffset, dstBuffer, dstOffset, size);
	}
	
	public void transfer(CommandBuffer commandBuffer, int offset, int size)
	{
		commandBuffer.copyBuffer(srcBuffer, srcOffset + offset, dstBuffer, dstOffset + offset, size);
	}
	
	public void transferAll(CommandBuffer commandBuffer)
	{
		commandBuffer.copyBuffer(srcBuffer, srcOffset, dstBuffer, dstOffset, size);
	}
}
