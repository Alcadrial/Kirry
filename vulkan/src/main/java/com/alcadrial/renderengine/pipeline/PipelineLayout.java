package com.alcadrial.renderengine.pipeline;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import com.alcadrial.renderengine.Device;
import com.alcadrial.renderengine.util.VulkanErrorUtils;

public class PipelineLayout implements AutoCloseable {
	
	private Device device;
	private long pipelineLayout;
	
	public PipelineLayout(Device device, VkPipelineLayoutCreateInfo createInfo)
	{
		this.device = device;
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreatePipelineLayout(device.getDevice(), createInfo, null, p), "Failed to create pipeline layout");
		pipelineLayout = p[0];
	}
	
	public long getPipelineLayout()
	{
		return pipelineLayout;
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroyPipelineLayout(device.getDevice(), pipelineLayout, null);
	}
}
