package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.VulkanObject;

public abstract class AbstractVulkanResource<VK> extends VulkanObject {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected VK vk;
	
	public AbstractVulkanResource()
	{}
	
	// public abstract void recreate(Object... objects);
	
	public VK getVk()
	{
		return vk;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "(" + (vk instanceof Long l ? Debug.address(l) : vk) + ")";
	}
}
