package com.alcadrial.kirry.render.commandbuffer;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.util.RawDeviceResource;

public class CommandPool extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public CommandPool(Device device, long vk)
	{
		super(device);
		this.vk = vk;
		Debug.debugCreate(() -> "Created " + this);
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
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
