package com.alcadrial.renderengine.render;

import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

/**
 * Describes all the data and how is used during the entire rendering process
 * and the operation of it.
 * 
 * @author Alcadrial
 */
public class Swapchain extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public Swapchain(Device device, Surface surface, VkSwapchainCreateInfoKHR createInfo)
	{
		super(device);
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkCreateSwapchainKHR(device.getVk(), createInfo, null, p), "Failed to create swap chain");
		vk = p[0];
		Debug.runOnDebug(() -> device.setObjectDebugName(this, "Swapchain " + Debug.getIncrementalId()));
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
		device.waitIdle().thenRun(() -> {
			KHRSwapchain.vkDestroySwapchainKHR(device.getVk(), vk, null);
			super.close();
		});
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
	}
}
