package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.RenderEngine;

public abstract class VulkanResource<VK> implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
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
