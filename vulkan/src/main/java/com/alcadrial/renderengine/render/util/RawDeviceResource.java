package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngineVulkan;

public abstract class RawDeviceResource extends AbstractDeviceResource<Long> {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
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
