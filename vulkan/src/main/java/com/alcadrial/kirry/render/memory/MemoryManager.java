package com.alcadrial.kirry.render.memory;

import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public interface MemoryManager {
	
	static MemoryManager getDefault(Device device)
	{
		return new DefaultMemoryManager(device, 65536, 65536, 65536, 65536);
	}
	
	VulkanBuffer allocate(int size, int usage) throws VulkanException;
	VulkanBuffer subAllocate(VulkanBuffer parent, int offset, int size, int usage) throws VulkanException;
	
	MemoryTransferState allocateForTransfer(CommandBuffer commandBuffer, int size) throws VulkanException;
	
	default MemoryTransferState allocateForTransfer(CommandBuffer commandBuffer, VulkanBuffer... dests) throws VulkanException
	{
		int size = 0;
		for (VulkanBuffer vulkanBuffer : dests) size += vulkanBuffer.getSize();
		return allocateForTransfer(commandBuffer, size);
	}
	
	VulkanBuffer allocateCache(int size, int usage) throws VulkanException;
	VulkanBuffer allocateSingleUse(int size, int usage) throws VulkanException;

	void close();
	void setName(String name);
}
