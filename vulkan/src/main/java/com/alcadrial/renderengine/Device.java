package com.alcadrial.renderengine;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;

import com.alcadrial.renderengine.util.VulkanErrorUtils;

public class Device implements AutoCloseable {
	
	private Window window;
	private VkDevice device;
	private PhysicalDevice physicalDevice;
	private Queue graphicsQueue;
	private Queue presentQueue;
	private List<SwappingBuffer> swappingBuffers;
	private int currentFrame;
	private Map<Integer, SwappingBuffer> imagesInFlight;
	
	public Device(Window window, VkDevice device, PhysicalDevice physicalDevice)
	{
		this.window = VulkanErrorUtils.nonNull(window);
		this.device = VulkanErrorUtils.nonNull(device);
		this.physicalDevice = VulkanErrorUtils.nonNull(physicalDevice);
	}
	
	public void createSwappingBuffers(int buffers)
	{
		if (buffers < 1) throw new IllegalArgumentException("Required at least 1 buffer");
		
		swappingBuffers = new ArrayList<>(buffers);
		imagesInFlight = new HashMap<>(window.getSwapChain().getImages().size());
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
			semaphoreInfo.sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
			
			VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
			fenceInfo.sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
			fenceInfo.flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT);
			
			long[][] p = new long[3][1];
			
			for (int i = 0; i < buffers; i++)
			{
				VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device, semaphoreInfo, null, p[0]), "Failed to create swapping buffers for the frame " + i);
				VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device, semaphoreInfo, null, p[1]), "Failed to create swapping buffers for the frame " + i);
				VulkanErrorUtils.assertNoError(VK10.vkCreateFence(device, fenceInfo, null, p[2]), "Failed to create swapping buffers for the frame " + i);
				
				swappingBuffers.add(new SwappingBuffer(this, p[0][0], p[1][0], p[2][0]));
			}
		}
		currentFrame = 0;
	}
	
	public void draw()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			SwappingBuffer buffer = swappingBuffers.get(currentFrame);
			
			VK10.vkWaitForFences(device, buffer.getFence(), true, -1);
			
			IntBuffer p = stack.callocInt(1);
			
			if (KHRSwapchain.vkAcquireNextImageKHR(device, window.getSwapChain().getSwapchain(), -1, buffer.getImageAvailable(), 0, p) == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				
				return;
			}
			int imageIndex = p.get(0);
			
			if (imagesInFlight.containsKey(imageIndex))
			{
				VK10.vkWaitForFences(device, imagesInFlight.get(imageIndex).getFence(), true, -1);
			}
			
			imagesInFlight.put(imageIndex, buffer);
			
			LongBuffer p0 = stack.callocLong(1);
			p0.put(0, buffer.getImageAvailable());
			LongBuffer p1 = stack.callocLong(1);
			p1.put(0, buffer.getRenderFinished());
			
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
			
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitSemaphores(p0);
			submitInfo.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			
			submitInfo.pSignalSemaphores(p1);
			
			submitInfo.pCommandBuffers(stack.pointers(window.getSwapChain().getCommands().get(imageIndex)));
			
			VK10.vkResetFences(device, buffer.getFence());
			
			VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(graphicsQueue.getQueue(), submitInfo, buffer.getFence()), "Failed to submit draw command buffer");// FAIL
			
			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
			
			presentInfo.pWaitSemaphores(p1);
			
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(window.getSwapChain().getSwapchain()));
			
			presentInfo.pImageIndices(p);
			
			KHRSwapchain.vkQueuePresentKHR(presentQueue.getQueue(), presentInfo);
			
			currentFrame = (currentFrame + 1) % swappingBuffers.size();
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
		if (swappingBuffers != null)
		{
			for (SwappingBuffer swappingBuffer : swappingBuffers)
			{
				VK10.vkWaitForFences(device, swappingBuffer.getFence(), true, -1);
				swappingBuffer.close();
			}
		}
		if (window != null) window.close();
		VK10.vkDestroyDevice(device, null);
	}
}
