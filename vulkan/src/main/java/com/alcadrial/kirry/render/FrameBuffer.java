package com.alcadrial.kirry.render;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.util.RawDeviceResource;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.VulkanUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class FrameBuffer extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public static FrameBuffer of(RenderPass renderPass, Texture texture) throws VulkanException
	{
		return of(renderPass, texture.getWidth(), texture.getHeight(), texture);
	}
	
	public static FrameBuffer of(RenderPass renderPass, int width, int height, Texture... textures) throws VulkanException
	{
		if (textures.length == 0) throw new VulkanException("Cannot create a FrameBuffer of zero textures");
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			return new FrameBuffer(textures[0].getDevice(), renderPass, VkFramebufferCreateInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.renderPass(renderPass.getVk())
				.pAttachments(VulkanUtils.toBuffer(stack, t -> t.getView().getVk(), textures))
				.width(width)
				.height(height)
				.layers(1), textures);
		}
	}
	
	public static FrameBuffer ofColor(RenderPass renderPass, int width, int height, int format, int usage) throws VulkanException
	{
		Texture colorTexture = Texture.create2dTexture(renderPass.getDevice(), width, height, format, VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | usage, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
		return of(renderPass, width, height, colorTexture);
	}
	
	public static FrameBuffer ofColorDepth(RenderPass renderPass, int width, int height, int colorFormat, int colorUsage, int depthFormat, int depthUsage) throws VulkanException
	{
		Texture colorTexture = Texture.create2dTexture(renderPass.getDevice(), width, height, colorFormat, VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | colorUsage, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
		Texture depthTexture = Texture.create2dTexture(renderPass.getDevice(), width, height, depthFormat, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | depthUsage, VK10.VK_IMAGE_ASPECT_DEPTH_BIT);
		return of(renderPass, width, height, colorTexture, depthTexture);
	}
	
	public static FrameBuffer ofColorDepth(RenderPass renderPass, int width, int height, int colorFormat, int colorUsage, int depthFormat, int depthUsage, boolean useStencil) throws VulkanException
	{
		Texture colorTexture = Texture.create2dTexture(renderPass.getDevice(), width, height, colorFormat, VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | colorUsage, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
		Texture depthTexture = Texture.create2dTexture(renderPass.getDevice(), width, height, depthFormat, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | depthUsage, VK10.VK_IMAGE_ASPECT_DEPTH_BIT | (Texture.hasStencil(depthFormat) && useStencil ? VK10.VK_IMAGE_ASPECT_STENCIL_BIT : 0));
		return of(renderPass, width, height, colorTexture, depthTexture);
	}
	
	private final RenderPass renderPass;
	private final Texture[] textures;
	private final int width;
	private final int height;
	private final int layers;
	
	public FrameBuffer(Device device, RenderPass renderPass, VkFramebufferCreateInfo info, Texture... textures) throws VulkanException
	{
		super(device);
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateFramebuffer(device.getVk(), info, null, p), "Failed to create framebuffer");
		vk = p[0];
		this.renderPass = renderPass;
		this.textures = textures;
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
	
	public Texture[] getTextures()
	{
		return textures;
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
