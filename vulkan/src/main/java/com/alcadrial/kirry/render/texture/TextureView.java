package com.alcadrial.kirry.render.texture;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
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
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateImageView(device.getVk(), VkImageViewCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.image(texture.getVk())
				.viewType(texture.getImageType())
				.format(texture.getFormat())
				.components(c -> c
					.r(VK10.VK_COMPONENT_SWIZZLE_IDENTITY)
					.g(VK10.VK_COMPONENT_SWIZZLE_IDENTITY)
					.b(VK10.VK_COMPONENT_SWIZZLE_IDENTITY)
					.a(VK10.VK_COMPONENT_SWIZZLE_IDENTITY)
				)
				.subresourceRange(r -> r
					.aspectMask(texture.getAspectMask())
					.baseMipLevel(0)
					.levelCount(texture.getMipLevels())
					.baseArrayLayer(0)
					.layerCount(1)
				), null, p), "Failed to create image view");
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
