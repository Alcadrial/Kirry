package com.alcadrial.renderengine.render;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class FrameBuffer extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private final int type;
	private final long renderPass;
	private final int width;
	private final int height;
	private final int layers;
	
	public FrameBuffer(Device device, VkFramebufferCreateInfo info) throws VulkanException
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
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	public int getType()
	{
		return type;
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
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("Type: " + type);
		Debug.print("Size: " + width + "x" + height);
		Debug.print("Layers: " + layers);
		Debug.print("RenderPass: " + Debug.address(renderPass));
	}
}
