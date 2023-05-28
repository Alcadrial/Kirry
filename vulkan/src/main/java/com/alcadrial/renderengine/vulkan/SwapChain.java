package com.alcadrial.renderengine.vulkan;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.renderengine.vulkan.util.VulkanErrorUtils;

public class SwapChain implements AutoCloseable {
	
	private Surface surface;
	private long swapChain;
	private List<Long> images;
	private List<Long> imageViews;
	private List<FrameBuffer> frameBuffers;
	private long commandPool;
	private List<VkCommandBuffer> commands;
	
	public SwapChain(Surface surface, long swapChain)
	{
		this.surface = surface;
		this.swapChain = swapChain;
		initialize();
	}
	
	private void initialize()
	{
		Device device = RenderEngine.getVulkan().getDevice();
		int[] p = new int[1];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapChain, p, null));
		long[] p0 = new long[p[0]];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapChain, p, p0));
		
		images = new ArrayList<>(p[0]);
		
		for (long image : p0) images.add(image);
		
		imageViews = new ArrayList<>(images.size());
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			p0 = new long[1];
			
			for (long image : images)
			{
				VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack);
				
				createInfo.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
				createInfo.image(image);
				createInfo.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D);
				createInfo.format(surface.getFormat().format());
				
				createInfo.components().r(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
				createInfo.components().g(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
				createInfo.components().b(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
				createInfo.components().a(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
				
				createInfo.subresourceRange().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
				createInfo.subresourceRange().baseMipLevel(0);
				createInfo.subresourceRange().levelCount(1);
				createInfo.subresourceRange().baseArrayLayer(0);
				createInfo.subresourceRange().layerCount(1);
				
				VulkanErrorUtils.assertNoError(VK10.vkCreateImageView(device.getDevice(), createInfo, null, p0), "Failed to create image views");
				imageViews.add(p0[0]);
			}
			
			VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
			poolInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
			poolInfo.queueFamilyIndex(device.getPhysicalDevice().getQueueGraphicsFamilyIndex());
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateCommandPool(device.getDevice(), poolInfo, null, p0), "Failed to create command pool");
			
			commandPool = p0[0];
		}
	}
	
	public void createFrameBuffers(Window window)
	{
		Device device = RenderEngine.getVulkan().getDevice();
		int size = imageViews.size();
		frameBuffers = new ArrayList<>(size);
		commands = new ArrayList<>(size);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			System.out.println("siz " + surface.getSize());
			System.out.println("ext " + surface.getExtent().width() + " " + surface.getExtent().height());
			LongBuffer attachments = stack.mallocLong(1);
			
			VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
			framebufferInfo.sType(VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
			framebufferInfo.renderPass(window.getRenderPass());
			framebufferInfo.width(surface.getExtent().width());
			framebufferInfo.height(surface.getExtent().height());
			framebufferInfo.layers(1);
			
			VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
			allocInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
			allocInfo.commandPool(commandPool);
			allocInfo.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY);
			allocInfo.commandBufferCount(size);
			
			PointerBuffer pointerBuffer = stack.mallocPointer(size);
			
			VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(device.getDevice(), allocInfo, pointerBuffer), "Failed to allocate command buffers");
			
			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
			beginInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
			
			VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
			renderPassInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
			renderPassInfo.renderPass(window.getRenderPass());
			VkRect2D renderArea = VkRect2D.calloc(stack);
			renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
			renderArea.extent(surface.getExtent());
			renderPassInfo.renderArea(renderArea);
			VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
			clearValues.color().float32(stack.floats(0, 0, 0, 0));
			renderPassInfo.pClearValues(clearValues);
			
			for (int i = 0; i < size; i++)
			{
				attachments.put(0, imageViews.get(i));
				framebufferInfo.pAttachments(attachments);
				FrameBuffer frameBuffer = new FrameBuffer(device, framebufferInfo);
				frameBuffers.add(frameBuffer);
				
				VkCommandBuffer commandBuffer = new VkCommandBuffer(pointerBuffer.get(i), device.getDevice());
				commands.add(commandBuffer);
				
				VulkanErrorUtils.assertNoError(VK10.vkBeginCommandBuffer(commandBuffer, beginInfo), "Failed to begin recording command buffer");
				
				renderPassInfo.framebuffer(frameBuffer.getId());
				
				VK10.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
				{
					VK10.vkCmdBindPipeline(commandBuffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, window.getPipeline().getPipeline());
					
					VK10.vkCmdDraw(commandBuffer, 3, 1, 0, 0);
				}
				VK10.vkCmdEndRenderPass(commandBuffer);
				
				VulkanErrorUtils.assertNoError(VK10.vkEndCommandBuffer(commandBuffer), "Failed to record command buffer");
				
			}
		}
	}
	
	public void reset(int width, int height)
	{
		
	}
	
	public Surface getSurface()
	{
		return surface;
	}
	
	public long getSwapchain()
	{
		return swapChain;
	}
	
	public int getImageFormat()
	{
		return surface.getFormat().format();
	}
	
	public List<Long> getImages()
	{
		return images;
	}
	
	public List<Long> getImageViews()
	{
		return imageViews;
	}
	
	public List<FrameBuffer> getFrameBuffers()
	{
		return frameBuffers;
	}
	
	public long getCommandPool()
	{
		return commandPool;
	}
	
	public List<VkCommandBuffer> getCommands()
	{
		return commands;
	}
	
	@Override
	public void close()
	{
		VkDevice device = RenderEngine.getVulkan().getDevice().getDevice();
		VK10.vkDestroyCommandPool(device, commandPool, null);
		for (FrameBuffer frameBuffer : frameBuffers) frameBuffer.close();
		for (long imageView : imageViews) VK10.vkDestroyImageView(device, imageView, null);
		KHRSwapchain.vkDestroySwapchainKHR(device, swapChain, null);
	}
}
