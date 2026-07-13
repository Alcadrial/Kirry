package com.alcadrial.kirry.render;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.util.RawDeviceResource;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class FrameBuffer extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public static FrameBuffer of(Texture texture, RenderPass renderPass) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			return new FrameBuffer(texture.getDevice(), renderPass, VkFramebufferCreateInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.renderPass(renderPass.getVk())
				.pAttachments(stack.longs(texture.getView().getVk()))
				.width(texture.getWidth())
				.height(texture.getHeight())
				.layers(1));
		}
	}
	
	public static FrameBuffer ofDepth(Texture texture, Texture depthStencilTexture, RenderPass renderPass) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			return new FrameBuffer(texture.getDevice(), renderPass, VkFramebufferCreateInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.renderPass(renderPass.getVk())
				.pAttachments(stack.longs(texture.getView().getVk(), depthStencilTexture.getView().getVk()))
				.width(texture.getWidth())
				.height(texture.getHeight())
				.layers(1));
		}
	}
	
	private final RenderPass renderPass;
	private final int width;
	private final int height;
	private final int layers;
	
	public FrameBuffer(Device device, RenderPass renderPass, VkFramebufferCreateInfo info) throws VulkanException
	{
		super(device);
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateFramebuffer(device.getVk(), info, null, p), "Failed to create framebuffer");
		vk = p[0];
		this.renderPass = renderPass;
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
	
	public RenderPass getRenderPass()
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
	
	@Override
	public void debug()
	{
		Debug.print("Vk", vk);
		Debug.print("Size", width + "x" + height);
		Debug.print("Layers", layers);
		Debug.print("RenderPass: ", renderPass);
	}
}
