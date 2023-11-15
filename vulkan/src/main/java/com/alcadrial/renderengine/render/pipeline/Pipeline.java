package com.alcadrial.renderengine.render.pipeline;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.LongDeviceResource;

/**
 * A sequence of operation that must be executed in order.
 * 
 * @author Alcadrial
 */
public abstract class Pipeline extends LongDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected PipelineLayout layout;
	
	public Pipeline(Device device, PipelineLayout layout)
	{
		super(device);
		this.layout = layout;
	}
	
	public abstract int bindPoint();
	
	public PipelineLayout getLayout()
	{
		return layout;
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroyPipeline(device.getVk(), vk, null);
		super.close();
	}
}
