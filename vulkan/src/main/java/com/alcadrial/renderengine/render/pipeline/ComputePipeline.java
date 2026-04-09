package com.alcadrial.renderengine.render.pipeline;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class ComputePipeline extends Pipeline {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public ComputePipeline(Device device, PipelineLayout layout, VkComputePipelineCreateInfo createInfo) throws VulkanException
	{
		super(device, layout);
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateComputePipelines(device.getVk(), 0, VkComputePipelineCreateInfo.create(createInfo.address(), 1), null, p), "Failed to create compute pipeline");
		vk = p[0];
	}
	
	@Override
	public int bindPoint()
	{
		return VK10.VK_PIPELINE_BIND_POINT_COMPUTE;
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("Layout", layout);
	}
}
