package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.Device;

public class LongDeviceResource extends DeviceResource<Long> {
	
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
