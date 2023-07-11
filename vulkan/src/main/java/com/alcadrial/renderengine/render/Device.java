package com.alcadrial.renderengine.render;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSubmitInfo;

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
	
	public void draw()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			SwapChain swapChain = window.getSwapChain();
			swapChain.endRegisteringCommands();
			CommandBufferData buffer = swapChain.getActiveCommandBufferData();
			
			IntBuffer p = stack.callocInt(1);
			
			if (KHRSwapchain.vkAcquireNextImageKHR(device, swapChain.getSwapchain(), Integer.MAX_VALUE, buffer.getImageAvailableSemaphore(), 0, p) == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				
				return;
			}
			
			LongBuffer p0 = stack.callocLong(1);
			p0.put(0, buffer.getImageAvailableSemaphore());
			LongBuffer p1 = stack.callocLong(1);
			p1.put(0, buffer.getRenderFinishedSemaphore());
			
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitSemaphores(p0);
			submitInfo.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			submitInfo.pSignalSemaphores(p1);
			submitInfo.pCommandBuffers(stack.pointers(buffer.getCommandBuffer()));
			
			VK10.vkResetFences(device, buffer.getFence());
			
			VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(graphicsQueue.getQueue(), submitInfo, buffer.getFence()), "Failed to submit draw command buffer");// FAIL
			
			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
			presentInfo.pWaitSemaphores(p1);
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(swapChain.getSwapchain()));
			presentInfo.pImageIndices(p);
			
			KHRSwapchain.vkQueuePresentKHR(presentQueue.getQueue(), presentInfo);
			
			swapChain.swapBuffer();
			
			swapChain.startRegisteringCommands();
		}
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
