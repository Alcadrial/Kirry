package com.alcadrial.renderengine.render.buffer;

import java.util.function.Consumer;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.OutOfSpaceException;

public abstract class BigVulkanBuffer implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected VulkanBuffer remoteBigBuffer;
	protected VulkanBuffer localBigBuffer;
	
	protected BigVulkanBuffer(Device device, int size, int flags)
	{
		remoteBigBuffer = new VulkanBuffer(device, size, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
	}
	
	public abstract VulkanBuffer allocate(int size, int usage) throws OutOfSpaceException;
	
	public synchronized PhysicalDeviceTranferState startLocalBuffering()
	{
		if (localBigBuffer == null)
		{
			localBigBuffer = new VulkanBuffer(remoteBigBuffer.getDevice(), (int) remoteBigBuffer.getSize(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
			return new PhysicalDeviceTranferState();
		}
		throw new IllegalStateException("Local buffering already started");
	}
	
	public void startLocalBuffering(Consumer<PhysicalDeviceTranferState> action)
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
	public void close()
	{
		clear();
		if (isLocalBufferingActive()) localBigBuffer.close();
		remoteBigBuffer.close();
	}
	
	public class PhysicalDeviceTranferState implements AutoCloseable {
		
		public VulkanBuffer mapLocal(VulkanBuffer remoteBuffer)
		{
			return allocate(remoteBuffer.size, remoteBuffer.getUsage());
		}
		
		@Override
		public void close()
		{
			synchronized (BigVulkanBuffer.this)
			{
				localBigBuffer.close();
				localBigBuffer = null;
			}
		}
	}
	
	protected class SyncSlot extends Slot<VulkanDoubleBuffer> {
		
		private boolean sync;
		
		public SyncSlot(VulkanDoubleBuffer buffer)
		{
			super(buffer);
			sync = false;
		}
		
		@Override
		public void sync()
		{
			if (!sync)
			{
				sync = true;
				buffer.sendToPhysicalDevice();
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