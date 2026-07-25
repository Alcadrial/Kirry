package com.alcadrial.kirry.render.buffer.writer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkMappedMemoryRange;

import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class DirectBufferWriter extends AbstractBufferWriter<DirectBufferWriter> {
	
	public DirectBufferWriter(VulkanBuffer buffer) throws VulkanException
	{
		this(buffer, 0, buffer.getSize());
	}
	
	public DirectBufferWriter(VulkanBuffer buffer, int offset, int size) throws VulkanException
	{
		super(buffer, offset, buffer, offset, size);
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
