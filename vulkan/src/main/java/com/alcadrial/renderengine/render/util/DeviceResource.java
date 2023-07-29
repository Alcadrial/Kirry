package com.alcadrial.renderengine.render.util;

import java.util.Objects;

import com.alcadrial.renderengine.render.Device;

public abstract class DeviceResource<VK> extends VulkanResource<VK> {
	
	protected Device device;
	
	public DeviceResource(Device device)
	{
		this.device = Objects.requireNonNull(device);
		device.addToDestructionList(this);
	}
	
	//public abstract void recreate(Device device, Object... objects);
	
	@Override
	public void close()
	{
		device.removeFromDestructionList(this);
		vk = null;
	}
	
	public Device getDevice()
	{
		return device;
	}
}
