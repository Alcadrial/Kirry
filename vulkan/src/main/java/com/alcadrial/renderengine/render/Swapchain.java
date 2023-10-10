package com.alcadrial.renderengine.render;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.util.LongDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

/**
 * Describes all the data and how is used during the entire rendering process
 * and the operation of it.
 * 
 * @author Alcadrial
 */
public class Swapchain extends LongDeviceResource {
	
	private RenderPass renderPass;
	private List<Long> images;
	private List<Long> imageViews;
	private List<FrameBuffer> frameBuffers;
	private VkRenderPassBeginInfo renderPassBeginInfo;
	private PointerBuffer commandBuffers;
	private List<CommandBufferData> commands;
	private int activeCommandIndex;
	private Vector2i maxSize;
	
	public Swapchain(Device device, Surface surface, RenderPass renderPass, VkSwapchainCreateInfoKHR createInfo)
	{
		super(device);
		this.renderPass = renderPass;
		maxSize = Vector2i.of(0, 0);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.getPhysicalDevice().getVk(), surface.getSurface(), capabilities), "Failed to create swap chain");
			createInfo.preTransform(capabilities.currentTransform());
			createInfo.surface(surface.getSurface());
			createInfo.imageFormat(surface.getFormat().format());
			createInfo.imageColorSpace(surface.getFormat().colorSpace());
			createInfo.imageExtent(surface.getExtent());
			createInfo.presentMode(surface.getMode());
			if (surface.getSwapchain() != null) createInfo.oldSwapchain(surface.getSwapchain().getVk());
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(KHRSwapchain.vkCreateSwapchainKHR(device.getVk(), createInfo, null, p), "Failed to create swap chain");
			vk = p[0];
		}
		
		renderPassBeginInfo = VkRenderPassBeginInfo.calloc();
		renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
		renderPassBeginInfo.renderPass(renderPass.getVk());
		initialize(surface);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			int size = imageViews.size();
			commandBuffers = MemoryStack.stackMallocPointer(size);
			commands = new ArrayList<>(size);
			
			VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
			allocInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
			allocInfo.commandPool(device.getResetCommandPool());
			allocInfo.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY);
			allocInfo.commandBufferCount(size);
			
			VulkanErrorUtils.assertNoError(VK10.vkAllocateCommandBuffers(device.getVk(), allocInfo, commandBuffers), "Failed to allocate command buffers");
			
			VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
			semaphoreInfo.sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
			
			VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
			fenceInfo.sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
			fenceInfo.flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT);
			
			long[][] p1 = new long[3][1];
			for (int i = 0; i < size; i++)
			{
				VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device.getVk(), semaphoreInfo, null, p1[0]), "Failed to create synchronization objects for the frame " + i);
				VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device.getVk(), semaphoreInfo, null, p1[1]), "Failed to create synchronization objects for the frame " + i);
				VulkanErrorUtils.assertNoError(VK10.vkCreateFence(device.getVk(), fenceInfo, null, p1[2]), "Failed to create synchronization objects for the frame " + i);
				
				commands.add(new CommandBufferData(device, commandBuffers.get(i), p1[0][0], p1[1][0], p1[2][0]));
			}
		}
	}
	
	private void initialize(Surface surface)
	{
		int[] p = new int[1];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVk(), vk, p, null));
		long[] p0 = new long[p[0]];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVk(), vk, p, p0));
		
		images = new ArrayList<>(p[0]);
		
		for (long image : p0) images.add(image);
		
		imageViews = new ArrayList<>(images.size());
		frameBuffers = new ArrayList<>(images.size());
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			p0 = new long[1];
			
			LongBuffer attachments = stack.mallocLong(1);
			
			VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
			framebufferInfo.sType(VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
			framebufferInfo.renderPass(renderPass.getVk());
			framebufferInfo.width(surface.getExtent().width());
			framebufferInfo.height(surface.getExtent().height());
			maxSize.setAll(framebufferInfo.width(), framebufferInfo.height());
			framebufferInfo.layers(1);
			
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
				
				VulkanErrorUtils.assertNoError(VK10.vkCreateImageView(device.getVk(), createInfo, null, p0), "Failed to create image views");
				imageViews.add(p0[0]);
				
				attachments.put(0, p0[0]);
				framebufferInfo.pAttachments(attachments);
				FrameBuffer frameBuffer = new FrameBuffer(device, framebufferInfo);
				frameBuffers.add(frameBuffer);
			}
		}
	}
	
	/**
	 * Not Thread-safe
	 * 
	 * @param size 
	 * @param clearColor 
	 */
	public CommandBufferData startRegisteringCommands(@Nullable Vector2i offset, @Nullable Vector2i size, Color clearColor)
	{
		CommandBufferData commandBuffer = getActiveCommandBufferData();
		if (!commandBuffer.isRecording())
		{
			while (VK10.vkWaitForFences(device.getVk(), commandBuffer.getFence(), true, Integer.MAX_VALUE) != VK10.VK_SUCCESS);
			
			commandBuffer.startRecording();
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				FrameBuffer frameBuffer = frameBuffers.get(activeCommandIndex);
				renderPassBeginInfo.framebuffer(frameBuffer.getVk());
				
				VkRect2D renderArea = VkRect2D.calloc(stack);
				if (offset != null) renderArea.offset(VkOffset2D.calloc(stack).set(offset.x(), offset.y()));
				else renderArea.offset(VkOffset2D.calloc(stack));
				if (size != null) renderArea.extent(VkExtent2D.calloc(stack).set(size.x(), size.y()));
				else renderArea.extent(VkExtent2D.calloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
				renderPassBeginInfo.renderArea(renderArea);
				
				VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
				clearValues.color().float32(stack.floats(clearColor.r(), clearColor.g(), clearColor.b(), clearColor.a()));
				renderPassBeginInfo.pClearValues(clearValues);
				
				VK10.vkCmdBeginRenderPass(commandBuffer.getVk(), renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			}
		}
		return commandBuffer;
	}
	
	/**
	 * Not Thread-safe
	 */
	public void endRegisteringCommands()
	{
		CommandBufferData activeState = getActiveCommandBufferData();
		if (activeState.isRecording())
		{
			VK10.vkCmdEndRenderPass(activeState.getVk());
			activeState.endRecording();
		}
	}
	
	public void recreate(Device device, Surface surface, VkSwapchainCreateInfoKHR createInfo)
	{
		Device oldDevice = this.device;
		List<FrameBuffer> oldFrameBuffers = frameBuffers;
		List<Long> oldImageViews = imageViews;
		Long oldVk = vk;
		device.waitAsyncIdle(() -> {
			if (oldFrameBuffers != null) for (FrameBuffer frameBuffer : oldFrameBuffers) frameBuffer.close();
			for (long imageView : oldImageViews) VK10.vkDestroyImageView(oldDevice.getVk(), imageView, null);
			KHRSwapchain.vkDestroySwapchainKHR(oldDevice.getVk(), oldVk, null);
		});
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.getPhysicalDevice().getVk(), surface.getSurface(), capabilities), "Failed to create swap chain");
			createInfo.preTransform(capabilities.currentTransform());
			createInfo.surface(surface.getSurface());
			createInfo.imageFormat(surface.getFormat().format());
			createInfo.imageColorSpace(surface.getFormat().colorSpace());
			createInfo.imageExtent(surface.getExtent());
			createInfo.presentMode(surface.getMode());
			createInfo.oldSwapchain(VK10.VK_NULL_HANDLE);
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(KHRSwapchain.vkCreateSwapchainKHR(device.getVk(), createInfo, null, p), "Failed to create swap chain");
			vk = p[0];
			
			initialize(surface);
		}
	}
	
	public void swapBuffer()
	{
		activeCommandIndex = (activeCommandIndex + 1) % 2;
	}
	
	public RenderPass getRenderPass()
	{
		return renderPass;
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
		device.waitIdle();
		for (CommandBufferData swappingBuffer : commands) swappingBuffer.close();
		if (frameBuffers != null) for (FrameBuffer frameBuffer : frameBuffers) frameBuffer.close();
		for (long imageView : imageViews) VK10.vkDestroyImageView(device.getVk(), imageView, null);
		KHRSwapchain.vkDestroySwapchainKHR(device.getVk(), vk, null);
		renderPassBeginInfo.close();
		super.close();
	}
}
