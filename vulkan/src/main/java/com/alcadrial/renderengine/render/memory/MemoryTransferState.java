package com.alcadrial.renderengine.render.memory;

import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.buffer.writer.DeferredBufferWriter;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

/**
 * Returned from a call to {@link MemoryManager#allocateForTransfer},
 * 
 * @author Alcadrial
 */
public interface MemoryTransferState extends AutoCloseable {
	
	DeferredBufferWriter getWriter(VulkanBuffer dst) throws VulkanException;
	VulkanBuffer getLocal(int size) throws VulkanException;
	VulkanBuffer getLocal(VulkanBuffer dst) throws VulkanException;
	VulkanBuffer getView(int size) throws VulkanException;
	VulkanBuffer getView(VulkanBuffer dst) throws VulkanException;
	
	@Override
	void close() throws VulkanException;
}
