package com.alcadrial.renderengine.render.util;

public abstract class VulkanResource<VK> implements AutoCloseable {
	
	protected VK vk;
	
	public VulkanResource()
	{}
	
	//public abstract void recreate(Object... objects);
	
	public VK getVk()
	{
		return vk;
	}
	
	@Override
	public abstract void close();
}
