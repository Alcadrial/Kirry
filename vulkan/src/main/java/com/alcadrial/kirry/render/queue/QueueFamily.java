package com.alcadrial.kirry.render.queue;

import org.jspecify.annotations.Nullable;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import com.alcadrial.kirry.render.KirryVulkan;

public class QueueFamily {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	private @Nullable VkQueueFamilyProperties properties;
	private int index;
	
	public QueueFamily()
	{
		this(null, -1);
	}
	
	public QueueFamily(@Nullable VkQueueFamilyProperties properties, int index)
	{
		this.properties = properties;
		this.index = index;
	}
	
	public @Nullable VkQueueFamilyProperties getProperties()
	{
		return properties;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	void setProperties(@Nullable VkQueueFamilyProperties properties)
	{
		if (properties == null) index = -1;
		this.properties = properties;
	}
	
	void setIndex(int index)
	{
		this.index = index;
	}
}
