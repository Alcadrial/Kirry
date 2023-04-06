package com.alcadrial.renderengine;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import com.alcadrial.renderengine.util.VulkanErrorUtils;

public class FrameBuffer implements AutoCloseable {
	
	private final Device device;
	private final long id;
	private final int type;
	private final long renderPass;
	private final int width;
	private final int height;
	private final int layers;
	
	public FrameBuffer(Device device, VkFramebufferCreateInfo info)
	{
		this.device = device;
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateFramebuffer(device.getDevice(), info, null, p), "Failed to create framebuffer");
		id = p[0];
		type = info.sType();
		renderPass = info.renderPass();
		width = info.width();
		height = info.height();
		layers = info.layers();
	}
	
	public long getId()
	{
		return id;
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
		VK10.vkDestroyFramebuffer(device.getDevice(), id, null);
	}
}
