package com.alcadrial.renderengine.render.util;

import java.util.Objects;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;

public abstract class AbstractDeviceResource<VK> extends AbstractVulkanResource<VK> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected final Device device;
	protected String name;
	
	public AbstractDeviceResource(Device device)
	{
		this.device = Objects.requireNonNull(device);
		device.addToDestructionList(this);
	}
	
	public abstract void setName(String name);
	
	//public abstract void recreate(Device device, Object... objects);
	
	@Override
	public void close()
	{
		device.removeFromDestructionList(this);
		super.close();
		vk = null;
	}
	
	public Device getDevice()
	{
		return device;
	}
}
