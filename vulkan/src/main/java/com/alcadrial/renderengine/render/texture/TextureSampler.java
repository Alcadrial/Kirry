package com.alcadrial.renderengine.render.texture;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.LongDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class TextureSampler extends LongDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public TextureSampler(Device device)
	{
		super(device);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
			samplerInfo.sType(VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
			samplerInfo.magFilter(VK10.VK_FILTER_NEAREST);
			samplerInfo.minFilter(VK10.VK_FILTER_NEAREST);
			samplerInfo.addressModeU(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT);
			samplerInfo.addressModeV(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT);
			samplerInfo.addressModeW(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT);
			samplerInfo.anisotropyEnable(device.getPhysicalDevice().getFeatures().samplerAnisotropy());
			samplerInfo.maxAnisotropy(Math.min(16, device.getPhysicalDevice().getProperties().limits().maxSamplerAnisotropy()));
			samplerInfo.borderColor(VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK);
			samplerInfo.unnormalizedCoordinates(false);
			samplerInfo.compareEnable(false);
			samplerInfo.compareOp(VK10.VK_COMPARE_OP_ALWAYS);
			samplerInfo.mipmapMode(VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR);
			
			long[] p = new long[1];
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateSampler(device.getVk(), samplerInfo, null, p), "Failed to create texture sampler");
			
			vk = p[0];
		}
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroySampler(device.getVk(), vk, null);
		super.close();
	}
}
