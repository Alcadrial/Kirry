package com.alcadrial.kirry.render.util;

import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;

public abstract class RawDeviceResource extends AbstractDeviceResource<Long> {
	
	static
	{
		KirryVulkan.assertLoaded();
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
