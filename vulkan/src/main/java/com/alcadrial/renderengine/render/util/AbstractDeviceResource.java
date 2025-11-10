package com.alcadrial.renderengine.render.util;

import java.util.Objects;

import com.alcadrial.renderengine.render.Debug;
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
	
	protected AbstractDeviceResource(Device device, int unusued)
	{
		this.device = Objects.requireNonNull(device);
	}
	
	public abstract void setName(String name);
	
	//public abstract void recreate(Device device, Object... objects);

	protected void closeInternal()
	{
		super.close();
		vk = null;
	}
	
	@Override
	public void close()
	{
		device.removeFromDestructionList(this);
		super.close();
		Debug.debugClose(() -> "Closed " + this);
		vk = null;
	}
	
	public Device getDevice()
	{
		return device;
	}
	
	@Override
	public String toString()
	{
		return Objects.toIdentityString(this) + "(" + (name != null ? name + ", " : "") + (vk instanceof Long l ? Debug.address(l) : vk) + ")";
	}
}
