package com.alcadrial.renderengine.render.pipeline;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class PipelineLayout extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public PipelineLayout(Device device, VkPipelineLayoutCreateInfo createInfo)
	{
		super(device);
		long[] p = new long[1];
		createInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
		VulkanErrorUtils.assertNoError(VK10.vkCreatePipelineLayout(device.getVk(), createInfo, null, p), "Failed to create pipeline layout");
		vk = p[0];
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroyPipelineLayout(device.getVk(), vk, null);
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
	}
}
