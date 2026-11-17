package com.alcadrial.kirry.render.memory;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.buffer.VulkanBuffer;

/**
 * Returned from a call to {@link MemoryManager#allocateCacheAll},
 * 
 * @author Alcadrial
 */
public interface MemoryCacheBulkState extends Iterable<MemoryCacheState> {
	
	@Nullable VulkanBuffer getBuffer();
	
	MemoryCacheState getState(int index);
	boolean isContinuous();
}
