package com.alcadrial.renderengine.render;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;

public class CommandBufferData implements AutoCloseable {
	
	private final Device device;
	private final VkCommandBuffer commandBuffer;
	private final long imageAvailable;
	private final long renderFinished;
	private final long fence;
	private boolean isRecording;
	
	public CommandBufferData(Device device, long commandBuffer, long imageAvailable, long renderFinished, long fence)
	{
		this.device = device;
		this.commandBuffer = new VkCommandBuffer(commandBuffer, device.getDevice());
		this.imageAvailable = imageAvailable;
		this.renderFinished = renderFinished;
		this.fence = fence;
		isRecording = false;
	}
	
	public boolean startRecording()
	{
		if (isRecording) return false;
		isRecording = true;
		return true;
	}
	
	public boolean endRecording()
	{
		if (!isRecording) return false;
		isRecording = false;
		return true;
	}
	
	public VkCommandBuffer getCommandBuffer()
	{
		return commandBuffer;
	}
	
	public long getImageAvailableSemaphore()
	{
		return imageAvailable;
	}
	
	public long getRenderFinishedSemaphore()
	{
		return renderFinished;
	}
	
	public long getFence()
	{
		return fence;
	}
	
	public boolean isRecording()
	{
		return isRecording;
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroySemaphore(device.getDevice(), imageAvailable, null);
		VK10.vkDestroySemaphore(device.getDevice(), renderFinished, null);
		VK10.vkDestroyFence(device.getDevice(), fence, null);
	}
}
