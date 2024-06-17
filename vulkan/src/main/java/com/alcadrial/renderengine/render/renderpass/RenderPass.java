package com.alcadrial.renderengine.render.renderpass;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

/**
 * An Object that knows the src and dest images in 1 pipeline execution (enough for now)
 * <p>
 * It's indipendent from the pipeline
 * 
 * @author Alcadrial
 */
public class RenderPass extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private Device device;
	
	public RenderPass(Device device, VkRenderPassCreateInfo renderPassInfo)
	{
		super(device);
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateRenderPass(device.getVk(), renderPassInfo, null, p), "Failed to create render pass");
		vk = p[0];
		this.device = device;
	}
	
	/**
	 * Must be called after all submitted commands that refer to renderPass have completed execution
	 */
	public void recreate(Device device, VkRenderPassCreateInfo renderPassInfo)
	{
		close();
		this.device = device;
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateRenderPass(device.getVk(), renderPassInfo, null, p), "Failed to recreate render pass");
		vk = p[0];
	}
	
	/**
	 * Must be called after all submitted commands that refer to renderPass have completed execution
	 */
	@Override
	public void close()
	{
		VK10.vkDestroyRenderPass(device.getVk(), getVk(), null);
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("Device: " + Debug.address(device.getVk()));
	}
}
