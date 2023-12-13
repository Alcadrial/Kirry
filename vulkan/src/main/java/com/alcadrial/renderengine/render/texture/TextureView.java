package com.alcadrial.renderengine.render.texture;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.LongDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class TextureView extends LongDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	TextureView(Texture texture)
	{
		super(texture.getDevice());
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack);
			createInfo.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
			createInfo.image(texture.getVk());
			createInfo.viewType(texture.getImageType());
			createInfo.format(texture.getFormat());
			
			VkComponentMapping components = createInfo.components();
			components.r(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
			components.g(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
			components.b(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
			components.a(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
			
			VkImageSubresourceRange subresourceRange = createInfo.subresourceRange();
			subresourceRange.aspectMask(texture.getAspectMask());
			subresourceRange.baseMipLevel(0);
			subresourceRange.levelCount(1);
			subresourceRange.baseArrayLayer(0);
			subresourceRange.layerCount(1);
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateImageView(device.getVk(), createInfo, null, p), "Failed to create image view");
			vk = p[0];
		}
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroyImageView(device.getVk(), vk, null);
		super.close();
	}
}
