package com.alcadrial.renderengine.render;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import com.alcadrial.renderengine.render.util.LongDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class FrameBuffer extends LongDeviceResource {
	
	private final int type;
	private final long renderPass;
	private final int width;
	private final int height;
	private final int layers;
	
	public FrameBuffer(Device device, VkFramebufferCreateInfo info)
	{
		super(device);
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateFramebuffer(device.getVk(), info, null, p), "Failed to create framebuffer");
		vk = p[0];
		type = info.sType();
		renderPass = info.renderPass();
		width = info.width();
		height = info.height();
		layers = info.layers();
	}
	
	public int getType()
	{
		return type;
	}
	
	public long getRenderPass()
	{
		return renderPass;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getLayers()
	{
		return layers;
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroyFramebuffer(device.getVk(), vk, null);
		super.close();
	}
}
