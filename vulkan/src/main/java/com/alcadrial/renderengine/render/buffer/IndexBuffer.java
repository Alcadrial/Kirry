package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;

public class IndexBuffer extends VulkanDoubleBuffer {
	
	public IndexBuffer(int[] data)
	{
		this(RenderEngine.getVulkan().getDevice(), data.length);
		asIntBuffer().put(data);
	}
	
	public IndexBuffer(Device device, int[] data)
	{
		this(device, data.length);
		asIntBuffer().put(data);
	}
	
	public IndexBuffer(int size)
	{
		this(RenderEngine.getVulkan().getDevice(), size * 4);
	}
	
	public IndexBuffer(Device device, int size)
	{
		super(device, size * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
	}
	
	public int getIndexType()
	{
		return VK10.VK_INDEX_TYPE_UINT32;
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
