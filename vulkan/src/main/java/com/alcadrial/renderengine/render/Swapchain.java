package com.alcadrial.renderengine.render;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import com.alcadrial.renderengine.render.util.LongDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

/**
 * Describes all the data and how is used during the entire rendering process
 * and the operation of it.
 * 
 * @author Alcadrial
 */
public class Swapchain extends LongDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public Swapchain(Device device, Surface surface, VkSwapchainCreateInfoKHR createInfo)
	{
		super(device);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(KHRSwapchain.vkCreateSwapchainKHR(device.getVk(), createInfo, null, p), "Failed to create swap chain");
			vk = p[0];
		}
	}
	
	@Override
	public void close()
	{
		device.waitIdle();
		KHRSwapchain.vkDestroySwapchainKHR(device.getVk(), vk, null);
		super.close();
	}
}
