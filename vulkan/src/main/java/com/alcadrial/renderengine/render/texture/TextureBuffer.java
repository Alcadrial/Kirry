package com.alcadrial.renderengine.render.texture;

import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;

public class TextureBuffer {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	private static int idGenerator = 0;
	
	public static TextureBuffer create(VulkanBuffer buffer, int width, int height, Object id)
	{
		return new TextureBuffer(buffer, width, height, id);
	}
	
	public static TextureBuffer createAnonymous(VulkanBuffer buffer, int width, int height)
	{
		return new TextureBuffer(buffer, width, height, idGenerator++);
	}

	private Object id;
	private VulkanBuffer buffer;
	private int width;
	private int height;
	
	private TextureBuffer(VulkanBuffer buffer, int width, int height, Object id)
	{
		this.buffer = buffer;
		this.width = width;
		this.height = height;
		this.id = id;
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
	
	public Object getId()
	{
		return id;
	}
}
