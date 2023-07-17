package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.vulkan.VK10;

public class UniformBuffer extends VulkanBuffer {
	
	public UniformBuffer(long size)
	{
		super(size, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
	}
}
