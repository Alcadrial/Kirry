package com.alcadrial.renderengine.render;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import com.alcadrial.renderengine.render.util.VulkanDebug;

public abstract class VulkanObject implements AutoCloseable, VulkanDebug {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	private static final MutableList<VulkanObject> VULKAN_INSTANCE_LIST = Lists.mutable.<VulkanObject>empty().asSynchronized();
	static final MutableList<VulkanObject> VULKAN_INSTANCE_IMMUTABLE_LIST = VULKAN_INSTANCE_LIST.asUnmodifiable();
	
	public VulkanObject()
	{
		VULKAN_INSTANCE_LIST.add(this);
	}
	
	@Override
	public void close()
	{
		if (!VULKAN_INSTANCE_LIST.remove(this) && Debug.isEnabledDeallocationFail()) throw new IllegalStateException("Already closed resource " + this);
	}
}
