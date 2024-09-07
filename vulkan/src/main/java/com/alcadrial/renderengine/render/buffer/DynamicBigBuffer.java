package com.alcadrial.renderengine.render.buffer;

import java.util.TreeSet;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;

public class DynamicBigBuffer extends BigVulkanBuffer {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected int maxAllocableBufferSize;
	protected TreeSet<Slot<?>> slots;
	
	public DynamicBigBuffer(Device device, int size)
	{
		super(device, size, 0);
		maxAllocableBufferSize = size;
		slots = new TreeSet<>();
	}
	
	@Override
	public VulkanBuffer allocate(int size, int usage)
	{
		throw new UnsupportedOperationException("NYI");
	}
	
	public VulkanBuffer reallocate(VulkanBuffer buffer, int size)
	{
		throw new UnsupportedOperationException("NYI");
	}
	
	public void free(VulkanBuffer buffer)
	{
		throw new UnsupportedOperationException("NYI");
	}
	
	@Override
	public int getMaxAllocableSize()
	{
		throw new UnsupportedOperationException("NYI");
	}
	
	@Override
	public void sync()
	{
		throw new UnsupportedOperationException("NYI");
	}
	
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("NYI");
	}
}