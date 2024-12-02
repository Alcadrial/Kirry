package com.alcadrial.renderengine.render;

import com.alcadrial.renderengine.render.util.VulkanDebug;

public abstract class VulkanObject implements AutoCloseable, VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public VulkanObject()
	{
		
	}
	
	@Override
	public void close()
	{
		
	}
}
