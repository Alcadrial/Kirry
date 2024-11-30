package com.alcadrial.renderengine.render;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alcadrial.renderengine.render.util.VulkanDebug;

public abstract class VulkanObject implements AutoCloseable, VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private static final Set<VulkanObject> VULKAN_INSTANCE_SET = Collections.newSetFromMap(new ConcurrentHashMap<>());
	static final Set<VulkanObject> VULKAN_INSTANCE_IMMUTABLE_SET = Collections.unmodifiableSet(VULKAN_INSTANCE_SET);
	
	public VulkanObject()
	{
		VULKAN_INSTANCE_SET.add(this);
	}
	
	@Override
	public void close()
	{
		VULKAN_INSTANCE_SET.remove(this);
	}
}
