package com.alcadrial.kirry.render.queue;

import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkPresentInfoKHR;

import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;

/**
 * Not thread-safe
 * 
 * @author Alcadrial
 *
 */
public class PresentQueue extends Queue {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	protected PresentQueue(Device device, int familyIndex)
	{
		super(device, familyIndex);
	}
	
	public int present(VkPresentInfoKHR presentInfo)
	{
		return KHRSwapchain.vkQueuePresentKHR(vk, presentInfo);
	}
}
