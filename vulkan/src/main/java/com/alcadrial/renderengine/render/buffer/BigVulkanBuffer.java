package com.alcadrial.renderengine.render.buffer;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.OutOfSpaceException;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public abstract class BigVulkanBuffer extends RawDeviceResource implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected VulkanBuffer remoteBigBuffer;
	protected VulkanBuffer localBigBuffer;
	
	protected BigVulkanBuffer(Device device, int size, int flags) throws VulkanException
	{
		super(device);
		remoteBigBuffer = new VulkanBuffer(device, size, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
		vk = remoteBigBuffer.getVk();
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(remoteBigBuffer, name);
		this.name = name;
	}
	
	/**
	 * 
	 * @param size the capacity of the buffer in bytes
	 * @param usage the vulkan usage flags
	 * @return the newly allocated buffer
	 * @throws OutOfSpaceException 
	 * @throws VulkanException 
	 */
	public abstract VulkanBuffer allocate(int size, int usage) throws OutOfSpaceException, VulkanException;
	
	public synchronized PhysicalDeviceTranferState startLocalBuffering() throws VulkanException
	{
		if (localBigBuffer == null)
		{
			localBigBuffer = new VulkanBuffer(remoteBigBuffer.getDevice(), remoteBigBuffer.getSize(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT);
			return new PhysicalDeviceTranferState();
		}
		throw new IllegalStateException("Local buffering already started");
	}
	
	public void startLocalBuffering(Consumer<PhysicalDeviceTranferState> action) throws VulkanException
	{
		try (PhysicalDeviceTranferState tranferState = startLocalBuffering())
		{
			action.accept(tranferState);
		}
	}
	
	public boolean isLocalBufferingActive()
	{
		return localBigBuffer != null;
	}
	
	public abstract int getMaxAllocableSize();
	public abstract void sync();
	public abstract void clear();
	
	@Override
	public void debug()
	{
		
	}
	
	@Override
	public void close()
	{
		clear();
		if (isLocalBufferingActive()) localBigBuffer.close();
		remoteBigBuffer.close();
		super.close();
	}
	
	public class PhysicalDeviceTranferState implements AutoCloseable {
		
		private CommandBuffer commandBuffer;
		private List<BufferWriter> writers;
		
		private PhysicalDeviceTranferState()
		{
			writers = new LinkedList<>();
		}
		
		public BufferWriter beginTransfer(VulkanBuffer remoteBuffer) throws VulkanException
		{
			return beginTransfer(remoteBuffer, 0, remoteBuffer.getSize());
		}
		
		/**
		 * 
		 * @param remoteBuffer the destination buffer
		 * @param offset the starting position of the buffer in bytes
		 * @param size the capacity of the buffer in bytes
		 * @return the newly allocated buffer
		 */
		public BufferWriter beginTransfer(VulkanBuffer remoteBuffer, int offset, int size) throws VulkanException
		{
			BufferWriter writer = new BufferWriter(allocate(remoteBuffer.size, remoteBuffer.getUsage()), 0, remoteBuffer, offset, size, commandBuffer);
			writers.add(writer);
			return writer;
		}
		
		public void setCommandBuffer(CommandBuffer commandBuffer)
		{
			this.commandBuffer = commandBuffer;
			for (BufferWriter writer : writers) writer.setCommandBuffer(commandBuffer);
		}
		
		public CommandBuffer getCommandBuffer()
		{
			return commandBuffer;
		}
		
		public void flush() throws VulkanException
		{
			for (BufferWriter writer : writers) writer.flush();
		}
		
		@Override
		public void close() throws VulkanException
		{
			synchronized (BigVulkanBuffer.this)
			{
				for (BufferWriter writer : writers) writer.close();
				writers.clear();
				clear();
				localBigBuffer.close();
				localBigBuffer = null;
			}
		}
	}
	
	protected class Slot<B extends VulkanBuffer> implements Comparable<Slot<B>> {
		
		protected B buffer;
		
		public Slot(B buffer)
		{
			this.buffer = buffer;
		}
		
		public long getOffset()
		{
			return buffer.getOffset();
		}
		
		public B getBuffer()
		{
			return buffer;
		}
		
		public void sync()
		{
			
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof Slot s && buffer.getOffset() == s.buffer.getOffset();
		}
		
		@Override
		public int compareTo(Slot<B> o)
		{
			return Long.compare(buffer.getOffset(), o.buffer.getOffset());
		}
	}
}