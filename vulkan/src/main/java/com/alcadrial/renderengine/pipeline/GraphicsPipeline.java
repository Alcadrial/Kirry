package com.alcadrial.renderengine.pipeline;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;

import com.alcadrial.renderengine.Device;
import com.alcadrial.renderengine.util.VulkanErrorUtils;

public class GraphicsPipeline implements AutoCloseable {
	
	private Device device;
	private PipelineLayout layout;
	private long pipeline;
	
	public GraphicsPipeline(Device device, PipelineLayout layout, VkGraphicsPipelineCreateInfo.Buffer createInfo)
	{
		createInfo.layout(layout.getPipelineLayout());
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateGraphicsPipelines(device.getDevice(), 0, createInfo, null, p), "Failed to create graphics pipeline");
		this.device = device;
		this.layout = layout;
		pipeline = p[0];
	}
	
	public PipelineLayout getLayout()
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
		layout.close();
		VK10.vkDestroyPipeline(device.getDevice(), pipeline, null);
	}
}
