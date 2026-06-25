package com.alcadrial.kirry.render.memory;

import com.alcadrial.kirry.render.buffer.VulkanBuffer;

/**
 * Returned from a call to {@link MemoryManager#allocateCache(int, int)},
 * 
 * @author Alcadrial
 */
public interface MemoryCacheState {
	
	VulkanBuffer getBuffer();
	
	boolean isValid();
	void acquire();
	void release();
}
