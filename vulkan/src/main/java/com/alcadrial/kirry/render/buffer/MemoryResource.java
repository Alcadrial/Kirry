package com.alcadrial.kirry.render.buffer;

import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.util.RawDeviceResource;


public abstract class MemoryResource extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	protected Memory memory;
	
	public MemoryResource(Device device, Memory memory)
	{
		super(device);
		this.memory = memory;
	}
	
	public Memory getMemory()
	{
		return memory;
	}
}
