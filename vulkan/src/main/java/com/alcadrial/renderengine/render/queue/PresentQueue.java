package com.alcadrial.renderengine.render.queue;

import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkPresentInfoKHR;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngineVulkan;

/**
 * Not thread-safe
 * 
 * @author Alcadrial
 *
 */
public class PresentQueue extends Queue {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
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
