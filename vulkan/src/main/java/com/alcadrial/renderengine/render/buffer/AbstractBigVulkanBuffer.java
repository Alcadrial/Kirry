package com.alcadrial.renderengine.render.buffer;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.OverflowException;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanFuture;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public abstract class AbstractBigVulkanBuffer<D extends AbstractBigVulkanBuffer.Data> extends RawDeviceResource implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected static class Data implements AutoCloseable {
		
		protected VulkanBuffer buffer;
		
		public Data(VulkanBuffer buffer)
		{
			this.buffer = buffer;
		}
		
		@Override
		public void close()
		{
			buffer.close();
		}
		
		@Override
		public String toString()
		{
			return buffer.toString();
		}
	}
	
	protected D remoteData;
	protected @Nullable D localData;
	
	protected AbstractBigVulkanBuffer(Device device, int size, int flags) throws VulkanException
	{
		super(device);
		VulkanBuffer remoteBigBuffer = new VulkanBuffer(device, size, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
		Debug.debugCreate(() -> "Created BigBuffer Remote " + remoteBigBuffer);
		vk = remoteBigBuffer.getVk();
		remoteData = allocateData(remoteBigBuffer);
	}
	
	protected abstract D allocateData(VulkanBuffer buffer);
	
	@Override
	public void setName(String name)
	{
		remoteData.buffer.setName(name);
		this.name = name;
	}
	
	/**
	 * 
	 * @param size the capacity of the buffer in bytes
	 * @param usage the vulkan usage flags
	 * @return the newly allocated buffer
	 * @throws OverflowException 
	 * @throws VulkanException 
	 */
	public abstract VulkanBuffer allocate(int size, int usage) throws OverflowException, VulkanException;
	
	public synchronized PhysicalDeviceTransferState startLocalBuffering() throws VulkanException
	{
		if (localData == null)
		{
			VulkanBuffer buffer = new VulkanBuffer(remoteData.buffer.getDevice(), remoteData.buffer.getSize(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT);
			buffer.getMemory().mapMemory();
			localData = allocateData(buffer);
			Debug.debugCreate(() -> "Created BigBuffer Local " + localData + " of " + remoteData);
			return new PhysicalDeviceTransferState(this, buffer);
		}
		throw new IllegalStateException("Local buffering already started");
	}
	
	public void startLocalBuffering(Consumer<PhysicalDeviceTransferState> action) throws VulkanException
	{
		try (PhysicalDeviceTransferState tranferState = startLocalBuffering())
		{
			action.accept(tranferState);
		}
	}
	
	public boolean isLocalBufferingActive()
	{
		return localData != null;
	}
	
	protected D getActiveData()
	{
		D data = localData;
		if (data == null) data = remoteData;
		return data;
	}
	
	public VulkanBuffer getActiveBuffer()
	{
		return getActiveData().buffer;
	}
	
	public VulkanBuffer getRemoteBuffer()
	{
		return remoteData.buffer;
	}
	
	public @Nullable VulkanBuffer getLocalBuffer()
	{
		D data = localData;
		if (data == null) return null;
		return data.buffer;
	}
	
	public abstract int getMaxAllocableSize();
	public abstract void sync();
	public abstract void clear();
	
	@Override
	public void debug()
	{}
	
	@Override
	public void close()
	{
		D data = localData;
		if (data != null)
		{
			clear();
			data.close();
			localData = null;
			Debug.debugClose(() -> "Destroyed BigBuffer Local of " + localData);
		}
		clear();
		if (remoteData != null) remoteData.close();
		super.close();
	}
	
	public static class PhysicalDeviceTransferState implements AutoCloseable {

		private AbstractBigVulkanBuffer<?> bigVulkanBuffer;
		private VulkanBuffer localBuffer;
		private List<BufferWriter> writers;
		private @Nullable CommandBuffer commandBuffer;
		
		private PhysicalDeviceTransferState(AbstractBigVulkanBuffer<?> bigVulkanBuffer, VulkanBuffer buffer)
		{
			this.bigVulkanBuffer = bigVulkanBuffer;
			localBuffer = buffer;
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
			BufferWriter writer = new BufferWriter(bigVulkanBuffer.allocate(size, remoteBuffer.getUsage()), 0, remoteBuffer, offset, size, commandBuffer);
			writers.add(writer);
			return writer;
		}
		
		public void setCommandBuffer(CommandBuffer commandBuffer)
		{
			this.commandBuffer = commandBuffer;
			for (BufferWriter writer : writers) writer.setCommandBuffer(commandBuffer);
		}
		
		public @Nullable CommandBuffer getCommandBuffer()
		{
			return commandBuffer;
		}
		
		public void flush() throws VulkanException
		{
			for (BufferWriter writer : writers) writer.transfer();
		}
		
		public VulkanFuture<?> waitIdle()
		{
			return RenderEngine.submitAsync(() -> {
				for (BufferWriter writer : writers) writer.getLoad().join();
			});
		}
		
		@Override
		public void close() throws VulkanException
		{
			synchronized (bigVulkanBuffer)
			{
				for (BufferWriter writer : writers)
				{
					writer.close();
					boolean interrupted = false;
					while (true)
					{
						try
						{
							writer.getLoad().await();
							break;
						}
						catch (InterruptedException e)
						{
							interrupted = true;
						}
					}
					if (interrupted) Thread.currentThread().interrupt();
				}
				writers.clear();
				bigVulkanBuffer.clear();
				localBuffer.getMemory().unmapMemory();
				localBuffer.close();
				bigVulkanBuffer.localData = null;
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
		{}
		
		@Override
		public boolean equals(@Nullable Object obj)
		{
			return obj instanceof Slot s && buffer.getOffset() == s.buffer.getOffset();
		}
		
		@Override
		public int compareTo(@Nullable Slot<B> o)
		{
			if (o == null) return -1;
			return Long.compare(buffer.getOffset(), o.buffer.getOffset());
		}
	}
}