package com.alcadrial.kirry.render;

import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import com.alcadrial.kirry.render.util.RawDeviceResource;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

/**
 * Describes all the data and how is used during the entire presentation process
 * and the operation of it.
 * 
 * @author Alcadrial
 */
public class Swapchain extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public Swapchain(Device device, VkSwapchainCreateInfoKHR createInfo) throws VulkanException
	{
		super(device);
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkCreateSwapchainKHR(device.getVk(), createInfo, null, p), "Failed to create swapchain");
		vk = p[0];
		Debug.runOnDebug(() -> device.setObjectDebugName(this, "Swapchain " + Debug.getIncrementalId(getClass())));
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
		device.waitIdle();
		KHRSwapchain.vkDestroySwapchainKHR(device.getVk(), vk, null);
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
	}
}
