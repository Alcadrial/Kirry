package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;

public class VertexBuffer extends VulkanDoubleBuffer {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private long offset;
	
	public VertexBuffer(int size)
	{
		this(RenderEngine.getVulkan().getDevice(), size * 4);
		offset = 0;
	}
	
	public VertexBuffer(Device device, int size)
	{
		super(device, size * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		offset = 0;
	}
	
	public VertexBuffer(float[] data)
	{
		this(data.length);
		asFloatBuffer().put(data);
	}
	
	public long getOffset()
	{
		return offset;
	}
	
	public void setOffset(long offset)
	{
		this.offset = offset;
	}
	
	@Override
	public void sendToPhysicalDevice()
	{
		super.sendToPhysicalDevice(0, size);
	}
	
	@Override
	public synchronized void sendToPhysicalDevice(long offset, long size)
	{
		super.sendToPhysicalDevice(offset * 4, size * 4);
	}
}
