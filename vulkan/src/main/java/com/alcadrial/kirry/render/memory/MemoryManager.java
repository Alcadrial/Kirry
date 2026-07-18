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
	VulkanBuffer allocateHost(int size, int usage) throws VulkanException;
	VulkanBuffer subAllocate(VulkanBuffer parent, int offset, int size, int usage) throws VulkanException;
	
	MemoryTransferState allocateForTransfer(CommandBuffer commandBuffer, int size) throws VulkanException;
	
	default MemoryTransferState allocateForTransfer(CommandBuffer commandBuffer, VulkanBuffer... dests) throws VulkanException
	{
		int size = 0;
		for (VulkanBuffer vulkanBuffer : dests) size += vulkanBuffer.getSize();
		return allocateForTransfer(commandBuffer, size);
	}
	
	/**
	 * Allocate a new buffer for cache purposes, the returned {@link MemoryCacheState} is already acquired 1 time.
	 * <p>
	 * The cache memory guarantee that all allocated buffer that are not released to 0 will never become invalid,
	 * when an allocation is released to 0 its content may invalid at any time.
	 * 
	 * @param size
	 * @param usage
	 * @throws VulkanException
	 */
	MemoryCacheState allocateCache(int size, int usage) throws VulkanException;
	
	/**
	 * Allocate many buffers for cache purposes, the returned {@link MemoryCacheBulkState} has
	 * its {@link MemoryCacheState MemoryCacheStates} already acquired 1 time.
	 * <p>
	 * If an error occurs, any allocation will be automatically freed as if none where actually done.
	 * <p>
	 * See {@link #allocateCache(int, int)} for guarantees.
	 * 
	 * @param sizes size of each allocation
	 * @param usage usage of all allocations
	 * @param bulkUsage usage of the returned bulk buffer
	 * @throws VulkanException
	 * @see #allocateCache(int, int)
	 */
	MemoryCacheBulkState allocateCacheAll(int[] sizes, int usage, int bulkUsage) throws VulkanException;
	VulkanBuffer allocateSingleUse(int size, int usage) throws VulkanException;
	
	void close();
	void setName(String name);
	
}
