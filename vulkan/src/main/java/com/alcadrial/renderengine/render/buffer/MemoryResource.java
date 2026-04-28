package com.alcadrial.renderengine.render.buffer;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.util.RawDeviceResource;


public abstract class MemoryResource extends RawDeviceResource {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
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

	public int getSize()
	{
		return memory.getSize();
	}
}
