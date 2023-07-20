package com.alcadrial.renderengine.render;

import java.nio.FloatBuffer;
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
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class SwapChain implements AutoCloseable {
	
	private Surface surface;
	private long swapChain;
	private List<Long> images;
	private List<Long> imageViews;
	private List<FrameBuffer> frameBuffers;
	private VkRenderPassBeginInfo renderPassInfo;
	private PointerBuffer commandBuffers;
	private List<CommandBufferData> commands;
	private int activeCommandIndex;
	
	public SwapChain(Surface surface, long swapChain)
	{
		this.surface = surface;
		this.swapChain = swapChain;
		renderPassInfo = VkRenderPassBeginInfo.calloc();
		renderPassInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
		renderPassInfo.renderPass(surface.getWindow().getRenderPass());
		VkRect2D renderArea = VkRect2D.calloc();
		renderArea.offset(VkOffset2D.calloc().set(0, 0));
		renderArea.extent(surface.getExtent());
		renderPassInfo.renderArea(renderArea);
		VkClearValue.Buffer clearValues = VkClearValue.calloc(1);
		Color clearColor = surface.getWindow().getClearColor();
		FloatBuffer b = FloatBuffer.allocate(4);
		b.put(clearColor.r());
		b.put(clearColor.g());
		b.put(clearColor.b());
		b.put(clearColor.a());
		clearValues.color().float32(b);
		renderPassInfo.pClearValues(clearValues);
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
			
			int size = imageViews.size();
			commandBuffers = MemoryStack.stackMallocPointer(size);
			commands = new ArrayList<>(size);
			
			VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
			allocInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
			allocInfo.commandPool(device.getResetCommandPool());
			allocInfo.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY);
			allocInfo.commandBufferCount(size);
			
			VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(device.getDevice(), allocInfo, commandBuffers), "Failed to allocate command buffers");
			
			VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
			semaphoreInfo.sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
			
			VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
			fenceInfo.sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
			fenceInfo.flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT);
			
			long[][] p1 = new long[3][1];
			for (int i = 0; i < size; i++)
			{
				VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device.getDevice(), semaphoreInfo, null, p1[0]), "Failed to create synchronization objects for the frame " + i);
				VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device.getDevice(), semaphoreInfo, null, p1[1]), "Failed to create synchronization objects for the frame " + i);
				VulkanErrorUtils.assertNoError(VK10.vkCreateFence(device.getDevice(), fenceInfo, null, p1[2]), "Failed to create synchronization objects for the frame " + i);
				
				commands.add(new CommandBufferData(device, commandBuffers.get(i), p1[0][0], p1[1][0], p1[2][0]));
			}
		}
	}
	
	public void createFrameBuffers()
	{
		if (frameBuffers == null)
		{
			Device device = RenderEngine.getVulkan().getDevice();
			int size = imageViews.size();
			frameBuffers = new ArrayList<>(size);
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				LongBuffer attachments = stack.mallocLong(1);
				
				VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
				framebufferInfo.sType(VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
				framebufferInfo.renderPass(surface.getWindow().getRenderPass());
				framebufferInfo.width(surface.getExtent().width());
				framebufferInfo.height(surface.getExtent().height());
				framebufferInfo.layers(1);
				
				for (int i = 0; i < size; i++)
				{
					attachments.put(0, imageViews.get(i));
					framebufferInfo.pAttachments(attachments);
					FrameBuffer frameBuffer = new FrameBuffer(device, framebufferInfo);
					frameBuffers.add(frameBuffer);
				}
			}
		}
	}
	
	/**
	 * Not Thread-safe
	 */
	public void startRegisteringCommands()
	{
		createFrameBuffers();
		CommandBufferData activeState = commands.get(activeCommandIndex);
		if (!activeState.isRecording())
		{
			while (VK10.vkWaitForFences(RenderEngine.getVulkan().getDevice().getDevice(), activeState.getFence(), true, Integer.MAX_VALUE) != VK10.VK_SUCCESS);
			
			VkCommandBuffer commandBuffer = activeState.getCommandBuffer();
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
				beginInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
				VulkanErrorUtils.assertNoError(VK10.vkBeginCommandBuffer(commandBuffer, beginInfo), "Failed to begin recording command buffer");
			}
			renderPassInfo.framebuffer(frameBuffers.get(activeCommandIndex).getId());
			
			VK10.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			
			activeState.startRecording();
		}
	}
	
	/**
	 * Not Thread-safe
	 */
	public void endRegisteringCommands()
	{
		createFrameBuffers();
		CommandBufferData activeState = commands.get(activeCommandIndex);
		if (activeState.isRecording())
		{
			VkCommandBuffer commandBuffer = activeState.getCommandBuffer();
			VK10.vkCmdEndRenderPass(commandBuffer);
			VulkanErrorUtils.assertNoError(VK10.vkEndCommandBuffer(commandBuffer), "Failed to record command buffer");
			activeState.endRecording();
		}
	}
	
	public void setClearColor(Color color)
	{
		FloatBuffer buffer = renderPassInfo.pClearValues().color().float32();
		buffer.put(0, color.r());
		buffer.put(1, color.g());
		buffer.put(2, color.b());
		buffer.put(3, color.a());
	}
	
	public void reset(int width, int height)
	{
		
	}
	
	public void swapBuffer()
	{
		activeCommandIndex = (activeCommandIndex + 1) % 2;
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
		createFrameBuffers();
		return frameBuffers;
	}
	
	public int getActiveCommandIndex()
	{
		return activeCommandIndex;
	}
	
	public CommandBufferData getActiveCommandBufferData()
	{
		return commands.get(activeCommandIndex);
	}
	
	@Override
	public void close()
	{
		Device device = RenderEngine.getVulkan().getDevice();
		device.waitIdle();
		for (CommandBufferData swappingBuffer : commands) swappingBuffer.close();
		if (frameBuffers != null) for (FrameBuffer frameBuffer : frameBuffers) frameBuffer.close();
		for (long imageView : imageViews) VK10.vkDestroyImageView(device.getDevice(), imageView, null);
		KHRSwapchain.vkDestroySwapchainKHR(device.getDevice(), swapChain, null);
		renderPassInfo.pClearValues().forEach(v -> v.color().float32().clear());
		renderPassInfo.pClearValues().close();
		renderPassInfo.close();
	}
}
