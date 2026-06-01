package com.alcadrial.renderengine.render.memory;

import java.util.List;

import org.eclipse.collections.impl.factory.Lists;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.buffer.writer.DeferredBufferWriter;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public abstract class StackMemoryTransferState implements MemoryTransferState {
	
	protected VulkanBuffer src;
	protected CommandBuffer commandBuffer;
	private List<DeferredBufferWriter> writers;
	private int offset;
	private boolean isClosed;
	
	public StackMemoryTransferState(VulkanBuffer src, CommandBuffer commandBuffer)
	{
		this.src = src;
		this.commandBuffer = commandBuffer;
		writers = Lists.mutable.of();
		offset = 0;
		isClosed = false;
	}
	
	@Override
	public DeferredBufferWriter getWriter(VulkanBuffer dst) throws VulkanException
	{
		if (isClosed) throw new IllegalStateException("Closed");
		DeferredBufferWriter writer = new DeferredBufferWriter(src, offset, dst, 0, dst.getSize());
		writers.add(writer);
		offset += dst.getSize();
		return writer;
	}
	
	@Override
	public VulkanBuffer getLocal(VulkanBuffer dst) throws VulkanException
	{
		if (isClosed) throw new IllegalStateException("Closed");
		VulkanBuffer buffer = new VulkanBuffer(src, offset, dst.getSize(), src.getUsage() | VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		offset += dst.getSize();
		return buffer;
	}
	
	@Override
	public VulkanBuffer getView(VulkanBuffer dst) throws VulkanException
	{
		if (isClosed) throw new IllegalStateException("Closed");
		VulkanBuffer buffer = VulkanBuffer.view(src, offset, dst.getSize());
		offset += dst.getSize();
		return buffer;
	}
	
	protected abstract void free() throws VulkanException;
	
	@Override
	public void close() throws VulkanException
	{
		if (isClosed) return;
		isClosed = true;
		for (DeferredBufferWriter writer : writers) writer.transferAll(commandBuffer);
		commandBuffer.addCompletionAction(this::free);
	}
}
