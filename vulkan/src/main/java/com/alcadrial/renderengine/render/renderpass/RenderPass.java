package com.alcadrial.renderengine.render.renderpass;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

/**
 * A description of the objects used by the pipeline
 * <p>
 * It's independent from the pipeline
 * 
 * @author Alcadrial
 */
public class RenderPass extends RawDeviceResource {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	private Device device;
	
	public RenderPass(Device device, VkRenderPassCreateInfo renderPassInfo) throws VulkanException
	{
		super(device);
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateRenderPass(device.getVk(), renderPassInfo, null, p), "Failed to create render pass");
		vk = p[0];
		this.device = device;
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	/**
	 * Must be called after all submitted commands that refer to renderPass have completed execution
	 */
	@Override
	public void close()
	{
		VK10.vkDestroyRenderPass(device.getVk(), vk, null);
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("Device: " + Debug.address(device.getVk()));
	}
}
