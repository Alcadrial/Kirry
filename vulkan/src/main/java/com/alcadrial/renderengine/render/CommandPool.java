package com.alcadrial.renderengine.render;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.util.RawDeviceResource;

public class CommandPool extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public CommandPool(Device device, long vk)
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
	
	@Override
	public void debug()
	{
		Debug.print("vk: " + Debug.address(vk));
	}
}
