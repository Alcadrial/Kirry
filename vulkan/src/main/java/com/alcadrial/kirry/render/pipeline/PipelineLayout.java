package com.alcadrial.kirry.render.pipeline;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.util.RawDeviceResource;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class PipelineLayout extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public PipelineLayout(Device device, VkPipelineLayoutCreateInfo createInfo) throws VulkanException
	{
		super(device);
		long[] p = new long[1];
		createInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
		VulkanErrorUtils.assertNoError(VK10.vkCreatePipelineLayout(device.getVk(), createInfo, null, p), "Failed to create pipeline layout");
		vk = p[0];
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
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
