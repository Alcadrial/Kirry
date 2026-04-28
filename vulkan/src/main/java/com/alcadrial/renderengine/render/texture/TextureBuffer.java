package com.alcadrial.renderengine.render.texture;

import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;

public class TextureBuffer {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	private VulkanBuffer buffer;
	private int width;
	private int height;
	
	public TextureBuffer(VulkanBuffer buffer, int width, int height)
	{
		this.buffer = buffer;
		this.width = width;
		this.height = height;
	}
	
	public VulkanBuffer getBuffer()
	{
		return buffer;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
}
