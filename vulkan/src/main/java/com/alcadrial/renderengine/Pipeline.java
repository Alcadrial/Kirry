package com.alcadrial.renderengine;

import org.lwjgl.vulkan.VK10;

public class Pipeline implements AutoCloseable {
	
	private Device device;
	private long layout;
	private long pipeline;
	
	public Pipeline(Device device, long layout, long pipeline)
	{
		this.device = device;
		this.layout = layout;
		this.pipeline = pipeline;
	}
	
	public long getLayout()
	{
		return layout;
	}
	
	public long getPipeline()
	{
		return pipeline;
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroyPipeline(device.getDevice(), pipeline, null);
		VK10.vkDestroyPipelineLayout(device.getDevice(), layout, null);
	}
}
