package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.RenderEngine;

public abstract class AbstractVulkanResource<VK> implements VulkanResource<VK> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected VK vk;
	
	public AbstractVulkanResource()
	{}
	
	// public abstract void recreate(Object... objects);
	
	@Override
	public VK getVk()
	{
		return vk;
	}
}
