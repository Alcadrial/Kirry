package com.alcadrial.kirry.render.pipeline;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.util.RawDeviceResource;

/**
 * A sequence of operation that must be executed in order.
 * 
 * @author Alcadrial
 */
public abstract class Pipeline extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	protected PipelineLayout layout;
	
	public Pipeline(Device device, PipelineLayout layout)
	{
		super(device);
		this.layout = layout;
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	public abstract int bindPoint();
	
	public PipelineLayout getLayout()
	{
		return layout;
	}
	
	@Override
	public void close()
	{
		if (vk != null) VK10.vkDestroyPipeline(device.getVk(), vk, null);
		super.close();
	}
}
