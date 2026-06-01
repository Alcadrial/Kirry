package com.alcadrial.renderengine.render.buffer.writer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkMappedMemoryRange;

import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class DirectBufferWriter extends AbstractBufferWriter<DirectBufferWriter> {
	
	public DirectBufferWriter(VulkanBuffer srcDstBuffer) throws VulkanException
	{
		this(srcDstBuffer, srcDstBuffer);
	}
	
	public DirectBufferWriter(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer) throws VulkanException
	{
		this(srcBuffer, dstBuffer, 0, dstBuffer.getSize());
	}
	
	public DirectBufferWriter(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int offset, int size) throws VulkanException
	{
		this(srcBuffer, offset, dstBuffer, offset, size);
	}
	
	public DirectBufferWriter(VulkanBuffer srcBuffer, int srcOffset, VulkanBuffer dstBuffer, int dstOffset, int size) throws VulkanException
	{
		super(srcBuffer, srcOffset, dstBuffer, dstOffset, size);
	}
	
	public void flush() throws VulkanException
	{
		if (!dstBuffer.getMemory().isHostCoherent())
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkMappedMemoryRange.Buffer ranges = VkMappedMemoryRange.malloc(1, stack)
					.sType(VK10.VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
					.pNext(0)
					.memory(dstBuffer.getMemory().getVk())
					.offset(dstOffset)
					.size(size);
				VK10.vkFlushMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges);
				VK10.vkInvalidateMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges);
			}
		}
	}
	
	public void flush(boolean invalidate) throws VulkanException
	{
		if (!dstBuffer.getMemory().isHostCoherent())
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkMappedMemoryRange.Buffer ranges = VkMappedMemoryRange.malloc(1, stack)
					.sType(VK10.VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
					.pNext(0)
					.memory(dstBuffer.getMemory().getVk())
					.offset(dstOffset)
					.size(size);
				VK10.vkFlushMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges);
				if (invalidate) VK10.vkInvalidateMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges);
			}
		}
	}
	
	public void flush(int offset, int size) throws VulkanException
	{
		if (!dstBuffer.getMemory().isHostCoherent())
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkMappedMemoryRange.Buffer ranges = VkMappedMemoryRange.malloc(1, stack)
					.sType(VK10.VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
					.pNext(0)
					.memory(dstBuffer.getMemory().getVk())
					.offset(dstOffset + offset)
					.size(size);
				VK10.vkFlushMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges);
				VK10.vkInvalidateMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges);
			}
		}
	}
	
	public void flush(int offset, int size, boolean invalidate) throws VulkanException
	{
		if (!dstBuffer.getMemory().isHostCoherent())
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkMappedMemoryRange.Buffer ranges = VkMappedMemoryRange.malloc(1, stack)
					.sType(VK10.VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
					.pNext(0)
					.memory(dstBuffer.getMemory().getVk())
					.offset(dstOffset + offset)
					.size(size);
				VK10.vkFlushMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges);
				if (invalidate) VK10.vkInvalidateMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges);
			}
		}
	}
}
