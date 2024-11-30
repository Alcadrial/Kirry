package com.alcadrial.renderengine.render;

import java.util.concurrent.ConcurrentHashMap;

import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public abstract class VulkanObject implements AutoCloseable, VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public VulkanObject()
	{
		
	}
	
	@Override
	public void close() throws VulkanException
	{
		
	}
}
