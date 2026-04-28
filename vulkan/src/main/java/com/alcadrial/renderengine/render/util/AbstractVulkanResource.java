package com.alcadrial.renderengine.render.util;

import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.VulkanObject;

public abstract class AbstractVulkanResource<VK> extends VulkanObject {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
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
	public int hashCode()
	{
		VK vk = this.vk;
		if (vk == null) return 0;
		return vk.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		VK vk = this.vk;
		if (vk == null)
		{
			if (obj == null) return true;
			return obj instanceof AbstractVulkanResource r && r.vk == null;
		}
		return obj instanceof AbstractVulkanResource r && vk.equals(r.vk);
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "(" + (vk instanceof Long l ? Debug.address(l) : vk) + ")";
	}
}
