package com.alcadrial.renderengine.render.buffer;

import com.alcadrial.renderengine.render.RenderEngine;

public class VulkanBufferRange {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private VulkanBuffer buffer;
	private int offset;
	private int size;
	
	public VulkanBufferRange(VulkanBuffer buffer)
	{
		this(buffer, 0, (int) buffer.getSize());
	}
	
	public VulkanBufferRange(VulkanBuffer buffer, int offset)
	{
		this(buffer, offset, (int) (buffer.getSize() - offset));
	}
	
	public VulkanBufferRange(VulkanBuffer buffer, int offset, int size)
	{
		this.buffer = buffer;
		this.offset = offset;
		this.size = size;
	}
	
	public VulkanBuffer getBuffer()
	{
		return buffer;
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	public int getSize()
	{
		return size;
	}
}
