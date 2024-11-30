package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;

public abstract class RawDeviceResource extends AbstractDeviceResource<Long> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public RawDeviceResource(Device device)
	{
		super(device);
	}
	
	@Override
	public void close()
	{
		super.close();
		vk = 0L;
	}
}
