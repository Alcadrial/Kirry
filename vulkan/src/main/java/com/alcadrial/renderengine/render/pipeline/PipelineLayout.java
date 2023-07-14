package com.alcadrial.renderengine.render.pipeline;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class PipelineLayout implements AutoCloseable {
	
	private Device device;
	private long pipelineLayout;
	
	public PipelineLayout(Device device, VkPipelineLayoutCreateInfo createInfo)
	{
		this.device = device;
		long[] p = new long[1];
		createInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
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
