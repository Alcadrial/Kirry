package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.util.vma.VmaAllocationInfo;

public class VulkanBufferData implements AutoCloseable {
	
	private long id;
	private long allocation;
	private VmaAllocationInfo allocationInfo;
	
	public VulkanBufferData(long id, long allocation, VmaAllocationInfo allocationInfo)
	{
		this.id = id;
		this.allocation = allocation;
		this.allocationInfo = allocationInfo;
	}
	
	public long getId()
	{
		return id;
	}
	
	public long getAllocation()
	{
		return allocation;
	}
	
	public VmaAllocationInfo getAllocationInfo()
	{
		return allocationInfo;
	}
	
	@Override
	public void close()
	{
		allocationInfo.close();
	}
}
