package com.alcadrial.renderengine.render;

import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class QueueFamily {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private VkQueueFamilyProperties properties;
	private int index;
	
	public QueueFamily()
	{
		this(null, -1);
	}
	
	public QueueFamily(VkQueueFamilyProperties properties, int index)
	{
		this.properties = properties;
		this.index = index;
	}
	
	public VkQueueFamilyProperties getProperties()
	{
		return properties;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	void setProperties(VkQueueFamilyProperties properties)
	{
		if (properties == null) index = -1;
		this.properties = properties;
	}
	
	void setIndex(int index)
	{
		this.index = index;
	}
}
