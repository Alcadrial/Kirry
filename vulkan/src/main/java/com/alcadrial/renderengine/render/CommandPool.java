package com.alcadrial.renderengine.render;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.util.LongDeviceResource;

public class CommandPool extends LongDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public CommandPool(Device device, Long vk)
	{
		super(device);
		this.vk = vk;
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroyCommandPool(device.getVk(), vk, null);
		super.close();
	}
}
