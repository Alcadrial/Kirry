package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;

public class LongDeviceResource extends DeviceResource<Long> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public LongDeviceResource(Device device)
	{
		super(device);
	}
	
	@Override
	public void close()
	{
		device.removeFromDestructionList(this);
		vk = 0L;
	}
}
