package com.alcadrial.renderengine.vulkan;

import org.lwjgl.vulkan.VK10;

public class SwappingBuffer implements AutoCloseable {
	
	private final Device device;
	private final long imageAvailable;
	private final long renderFinished;
	private final long fence;
	
	public SwappingBuffer(Device device, long imageAvailable, long renderFinished, long fence)
	{
		this.device = device;
		this.imageAvailable = imageAvailable;
		this.renderFinished = renderFinished;
		this.fence = fence;
	}
	
	public long getImageAvailable()
	{
		return imageAvailable;
	}
	
	public long getRenderFinished()
	{
		return renderFinished;
	}
	
	public long getFence()
	{
		return fence;
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroySemaphore(device.getDevice(), imageAvailable, null);
		VK10.vkDestroySemaphore(device.getDevice(), renderFinished, null);
		VK10.vkDestroyFence(device.getDevice(), fence, null);
	}
}
