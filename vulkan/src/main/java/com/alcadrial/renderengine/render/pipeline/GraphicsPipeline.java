package com.alcadrial.renderengine.render.pipeline;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class GraphicsPipeline extends Pipeline {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public GraphicsPipeline(Device device, PipelineLayout layout, VkGraphicsPipelineCreateInfo.Buffer createInfo) throws VulkanException
	{
		super(device, layout);

		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateGraphicsPipelines(device.getVk(), 0, createInfo, null, p), "Failed to create graphics pipeline");
		vk = p[0];
	}
	
	@Override
	public int bindPoint()
	{
		return VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("Layout", layout);
	}
}
