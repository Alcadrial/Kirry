package com.alcadrial.kirry.render.texture;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.util.RawDeviceResource;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class TextureView extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	TextureView(Texture texture) throws VulkanException
	{
		super(texture.getDevice());
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.malloc(stack);
			createInfo.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
			createInfo.pNext(0);
			createInfo.flags(0);
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
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroyImageView(device.getVk(), vk, null);
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
	}
}
