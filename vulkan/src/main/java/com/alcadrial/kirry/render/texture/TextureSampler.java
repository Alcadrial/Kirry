package com.alcadrial.kirry.render.texture;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.util.RawDeviceResource;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class TextureSampler extends RawDeviceResource {
	
	public static TextureSampler ofNearest(Device device) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			return new TextureSampler(device, VkSamplerCreateInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.magFilter(VK10.VK_FILTER_NEAREST)
				.minFilter(VK10.VK_FILTER_NEAREST)
				.mipmapMode(VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST)
				.addressModeU(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
				.addressModeV(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
				.addressModeW(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
				.mipLodBias(0)
				.anisotropyEnable(false)
				.maxAnisotropy(0)
				.compareEnable(false)
				.compareOp(VK10.VK_COMPARE_OP_ALWAYS)
				.minLod(0)
				.maxLod(0)
				.borderColor(VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK)
				.unnormalizedCoordinates(false)
			);
		}
	}
	public static TextureSampler ofLinear(Device device) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			return new TextureSampler(device, VkSamplerCreateInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.magFilter(VK10.VK_FILTER_LINEAR)
				.minFilter(VK10.VK_FILTER_LINEAR)
				.mipmapMode(VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR)
				.addressModeU(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
				.addressModeV(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
				.addressModeW(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
				.mipLodBias(0)
				.anisotropyEnable(true)
				.maxAnisotropy(Math.min(16, device.getPhysicalDevice().getProperties().limits().maxSamplerAnisotropy()))
				.compareEnable(false)
				.compareOp(VK10.VK_COMPARE_OP_ALWAYS)
				.minLod(0)
				.maxLod(VK10.VK_LOD_CLAMP_NONE)
				.borderColor(VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK)
				.unnormalizedCoordinates(false)
			);
		}
	}
	
	public TextureSampler(Device device, VkSamplerCreateInfo createInfo) throws VulkanException
	{
		super(device);
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateSampler(device.getVk(), createInfo, null, p), "Failed to create texture sampler");
		vk = p[0];
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
		VK10.vkDestroySampler(device.getVk(), vk, null);
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
	}
}
