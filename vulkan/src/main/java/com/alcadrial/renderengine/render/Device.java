package com.alcadrial.renderengine.render;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;

import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class Device implements AutoCloseable {
	
	private Window window;
	private VkDevice device;
	private PhysicalDevice physicalDevice;
	private Queue graphicsQueue;
	private Queue presentQueue;
	
	public Device(Window window, VkDevice device, PhysicalDevice physicalDevice)
	{
		this.window = VulkanErrorUtils.nonNull(window);
		this.device = VulkanErrorUtils.nonNull(device);
		this.physicalDevice = VulkanErrorUtils.nonNull(physicalDevice);
	}
	
	public void waitIdle()
	{
		VK10.vkDeviceWaitIdle(device);
	}
	
	public VkDevice getDevice()
	{
		return device;
	}
	
	public PhysicalDevice getPhysicalDevice()
	{
		return physicalDevice;
	}
	
	public Queue getGraphicsQueue()
	{
		return graphicsQueue;
	}
	
	public void setGraphicsQueue(Queue queue)
	{
		VulkanErrorUtils.match(this, queue.getDevice(), "Cannot set a queue of another device");
		graphicsQueue = queue;
	}
	
	public Queue getPresentQueue()
	{
		return presentQueue;
	}
	
	public void setPresentQueue(Queue queue)
	{
		VulkanErrorUtils.match(this, queue.getDevice(), "Cannot set a queue of another device");
		presentQueue = queue;
	}
	
	public Window getWindow()
	{
		return window;
	}
	
	@Override
	public void close()
	{
		if (window != null) window.close();
		VK10.vkDestroyDevice(device, null);
	}
}
