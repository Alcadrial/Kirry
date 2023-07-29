package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;

public class UniformBuffer extends VulkanBuffer {
	
	public UniformBuffer(long size)
	{
		this(RenderEngine.getVulkan().getDevice(), size);
	}
	
	public UniformBuffer(Device device, long size)
	{
		super(device, size, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
	}
}
