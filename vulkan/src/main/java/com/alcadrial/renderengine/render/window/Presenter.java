package com.alcadrial.renderengine.render.window;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.CommandBufferData;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.RenderPass;
import com.alcadrial.renderengine.render.Surface;
import com.alcadrial.renderengine.render.Swapchain;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class Presenter implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public enum Result {
		SUCCESS,
		// FAILED,
		RECREATED_SWAPCHAIN,
		EMPTY_SURFACE;
	}
	
	private final int swappingBuffers;
	
	private Device device;
	private Surface surface;
	private Swapchain swapchain;
	private RenderPass renderPass;
	
	private VkRenderPassBeginInfo renderPassBeginInfo;
	
	private List<Long> images;
	private List<Long> imageViews;
	private List<FrameBuffer> frameBuffers;
	private List<CommandBufferData> commands;
	
	private Semaphore graphicsQueueSemaphor;
	
	private int frameIndex;
	
	private Color clearColor;
	private float clearDepth;
	private int clearStencil;
	
	public Presenter(Device device, Surface surface, @Nullable Texture depthTexture, int swappingBuffers)
	{
		this.device = device;
		this.surface = surface;
		this.swappingBuffers = swappingBuffers;
		clearColor = Color.BLACK;
		clearDepth = 1;
		clearStencil = 0;
		graphicsQueueSemaphor = new Semaphore(1);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PhysicalDevice physicalDevice = device.getPhysicalDevice();
			
			surface.init(physicalDevice, stack);
			
			VkAttachmentDescription.Buffer attachmentDescriptions = VkAttachmentDescription.calloc(depthTexture != null ? 2 : 1, stack);
			
			VkAttachmentDescription colorAttachment = attachmentDescriptions.get();
			colorAttachment.format(surface.getFormat().format());
			colorAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
			colorAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
			colorAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
			colorAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
			colorAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
			colorAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			colorAttachment.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
			
			VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack);
			colorAttachmentRef.attachment(0);
			colorAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
			
			VkSubpassDescription.Buffer subpassDescription = VkSubpassDescription.calloc(1, stack);
			subpassDescription.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
			subpassDescription.colorAttachmentCount(1);
			subpassDescription.pColorAttachments(colorAttachmentRef);
			
			int srcStageMask = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
			int srcAccessMask = 0;
			int dstStageMask = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
			int dstAccessMask = VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
			
			if (depthTexture != null)
			{
				VkAttachmentDescription depthAttachment = attachmentDescriptions.get();
				depthAttachment.format(depthTexture.getFormat());
				depthAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
				depthAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
				depthAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
				depthAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
				depthAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
				depthAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
				depthAttachment.finalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
				
				VkAttachmentReference depthAttachmentRef = VkAttachmentReference.calloc(stack);
				depthAttachmentRef.attachment(1);
				depthAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
				
				subpassDescription.pDepthStencilAttachment(depthAttachmentRef);
				
				srcStageMask |= VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
				dstStageMask |= VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
				dstAccessMask |= VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
			}
			
			VkSubpassDependency.Buffer subpassDependency = VkSubpassDependency.calloc(1, stack);
			subpassDependency.srcSubpass(VK10.VK_SUBPASS_EXTERNAL);
			subpassDependency.dstSubpass(0);
			subpassDependency.srcStageMask(srcStageMask);
			subpassDependency.srcAccessMask(srcAccessMask);
			subpassDependency.dstStageMask(dstStageMask);
			subpassDependency.dstAccessMask(dstAccessMask);
			
			VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc(stack);
			renderPassCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
			renderPassCreateInfo.pAttachments(attachmentDescriptions.rewind());
			renderPassCreateInfo.pSubpasses(subpassDescription);
			renderPassCreateInfo.pDependencies(subpassDependency);
			
			renderPass = new RenderPass(device, renderPassCreateInfo);
			
			VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getVk(), surface.getSurface(), capabilities), "Failed to create swap chain");
			
			VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack);
			swapchainCreateInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
			swapchainCreateInfo.minImageCount(swappingBuffers);
			swapchainCreateInfo.imageArrayLayers(1);
			swapchainCreateInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
			if (device.getGraphicsQueue().getFamilyIndex() == device.getPresentQueue().getFamilyIndex())
			{
				swapchainCreateInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			}
			else
			{
				swapchainCreateInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
				swapchainCreateInfo.pQueueFamilyIndices(stack.ints(device.getGraphicsQueue().getFamilyIndex(), device.getPresentQueue().getFamilyIndex()));
			}
			swapchainCreateInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			swapchainCreateInfo.clipped(true);
			swapchainCreateInfo.preTransform(capabilities.currentTransform());
			swapchainCreateInfo.surface(surface.getSurface());
			swapchainCreateInfo.imageFormat(surface.getFormat().format());
			swapchainCreateInfo.imageColorSpace(surface.getFormat().colorSpace());
			swapchainCreateInfo.imageExtent(surface.getExtent());
			swapchainCreateInfo.presentMode(surface.getMode());
			
			swapchain = new Swapchain(device, surface, swapchainCreateInfo);
			
			initialize(stack, depthTexture);
		}
	}
	
	private void initialize(MemoryStack stack, @Nullable Texture depthTexture)
	{
		int[] p = new int[1];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVk(), swapchain.getVk(), p, null));
		long[] p0 = new long[p[0]];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVk(), swapchain.getVk(), p, p0));
		
		images = new ArrayList<>(p[0]);
		
		for (long image : p0) images.add(image);
		
		imageViews = new ArrayList<>(images.size());
		frameBuffers = new ArrayList<>(images.size());
		
		p0 = new long[1];
		
		LongBuffer attachments = stack.mallocLong(depthTexture != null ? 2 : 1);
		if (depthTexture != null) attachments.put(1, depthTexture.getView().getVk());
		
		VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc(stack);
		framebufferCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
		framebufferCreateInfo.renderPass(renderPass.getVk());
		framebufferCreateInfo.width(surface.getExtent().width());
		framebufferCreateInfo.height(surface.getExtent().height());
		
		framebufferCreateInfo.layers(1);
		
		VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(stack);
		
		imageViewCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
		imageViewCreateInfo.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D);
		imageViewCreateInfo.format(surface.getFormat().format());
		
		imageViewCreateInfo.components().r(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
		imageViewCreateInfo.components().g(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
		imageViewCreateInfo.components().b(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
		imageViewCreateInfo.components().a(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
		
		imageViewCreateInfo.subresourceRange().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
		imageViewCreateInfo.subresourceRange().baseMipLevel(0);
		imageViewCreateInfo.subresourceRange().levelCount(1);
		imageViewCreateInfo.subresourceRange().baseArrayLayer(0);
		imageViewCreateInfo.subresourceRange().layerCount(1);
		
		for (long image : images)
		{
			imageViewCreateInfo.image(image);
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateImageView(device.getVk(), imageViewCreateInfo, null, p0), "Failed to create image views");
			imageViews.add(p0[0]);
			
			attachments.put(0, p0[0]);
			framebufferCreateInfo.pAttachments(attachments);
			FrameBuffer frameBuffer = new FrameBuffer(device, framebufferCreateInfo);
			frameBuffers.add(frameBuffer);
		}
		
		renderPassBeginInfo = VkRenderPassBeginInfo.calloc();
		renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
		renderPassBeginInfo.renderPass(renderPass.getVk());
		
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVk(), swapchain.getVk(), p, null));
		int size = p[0];
		commands = new ArrayList<>(size);
		
		List<VkCommandBuffer> commandBuffers = device.createMultiUseCommandBuffer(stack, 3);
		
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
	
	private Result recreateSwapchain(MemoryStack stack)
	{
		surface.init(device.getPhysicalDevice(), stack);
		if (surface.getExtent().width() == 0 || surface.getExtent().height() == 0) return Result.EMPTY_SURFACE;
		
		VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
		VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.getPhysicalDevice().getVk(), surface.getSurface(), capabilities), "Failed to recreate swap chain");
		
		VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack);
		swapchainCreateInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
		swapchainCreateInfo.minImageCount(swappingBuffers);
		swapchainCreateInfo.imageArrayLayers(1);
		swapchainCreateInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
		if (device.getGraphicsQueue().getFamilyIndex() == device.getPresentQueue().getFamilyIndex())
		{
			swapchainCreateInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
		}
		else
		{
			swapchainCreateInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
			swapchainCreateInfo.pQueueFamilyIndices(stack.ints(device.getGraphicsQueue().getFamilyIndex(), device.getPresentQueue().getFamilyIndex()));
		}
		swapchainCreateInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
		swapchainCreateInfo.clipped(true);
		swapchainCreateInfo.preTransform(capabilities.currentTransform());
		swapchainCreateInfo.surface(surface.getSurface());
		swapchainCreateInfo.imageFormat(surface.getFormat().format());
		swapchainCreateInfo.imageColorSpace(surface.getFormat().colorSpace());
		swapchainCreateInfo.imageExtent(surface.getExtent());
		swapchainCreateInfo.presentMode(surface.getMode());
		swapchainCreateInfo.oldSwapchain(swapchain.getVk());
		
		swapchain = new Swapchain(device, surface, swapchainCreateInfo);
		
		Device oldDevice = device;
		List<FrameBuffer> oldFrameBuffers = frameBuffers;
		List<Long> oldImageViews = imageViews;
		Long oldVk = swapchain.getVk();
		
		device.waitAsyncIdle(() -> {
			for (FrameBuffer frameBuffer : oldFrameBuffers) frameBuffer.close();
			oldFrameBuffers.clear();
			for (long imageView : oldImageViews) VK10.vkDestroyImageView(oldDevice.getVk(), imageView, null);
			oldImageViews.clear();
			KHRSwapchain.vkDestroySwapchainKHR(oldDevice.getVk(), oldVk, null);
		});
		
		return Result.RECREATED_SWAPCHAIN;
	}
	
	public void beginRenderPass(CommandBufferData commandBuffer)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			FrameBuffer frameBuffer = frameBuffers.get(frameIndex);
			renderPassBeginInfo.framebuffer(frameBuffer.getVk());
			
			VkRect2D renderArea = VkRect2D.calloc(stack);
			renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
			renderPassBeginInfo.renderArea(renderArea);
			
			VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
			clearValues.get().color().float32(stack.floats(clearColor.r(), clearColor.g(), clearColor.b(), clearColor.a()));
			clearValues.get().depthStencil().depth(clearDepth).stencil(clearStencil);
			renderPassBeginInfo.pClearValues(clearValues.rewind());
			
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
		}
	}
	
	public Result present()
	{
		return present((d, i) -> {}, () -> {});
	}
	
	public Result present(BiConsumer<CommandBufferData, Integer> onPresentAction, Runnable onCommandBufferEndAction)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer p = stack.mallocInt(1);
			
			CommandBufferData commandBuffer = commands.get(frameIndex);
			int result = KHRSwapchain.vkAcquireNextImageKHR(device.getVk(), swapchain.getVk(), Integer.MAX_VALUE, commandBuffer.getImageAvailableSemaphore(), 0, p);
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) return recreateSwapchain(stack);
			VulkanErrorUtils.assertNoError(result, "Error acquiring next image");
			
			device.waitForFences(commandBuffer.getFence());
			graphicsQueueSemaphor.acquireUninterruptibly();
			device.resetFence(commandBuffer.getFence());
			
			commandBuffer.reset();
			commandBuffer.startRecording();
			onPresentAction.accept(commandBuffer, frameIndex);
			commandBuffer.endRecording();
			
			LongBuffer p0 = stack.longs(commandBuffer.getImageAvailableSemaphore());
			LongBuffer p1 = stack.longs(commandBuffer.getRenderFinishedSemaphore());
			
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitSemaphores(p0);
			submitInfo.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			submitInfo.pCommandBuffers(VulkanUtils.toAddressBuffer(stack, commandBuffer));
			submitInfo.pSignalSemaphores(p1);
			
			device.getGraphicsQueue().submit(submitInfo, commandBuffer.getFence());
			device.waitAsyncForFences(() -> {
				onCommandBufferEndAction.run();
				graphicsQueueSemaphor.release();
			}, commandBuffer.getFence());
			
			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
			presentInfo.pWaitSemaphores(p1);
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(swapchain.getVk()));
			presentInfo.pImageIndices(p);
			
			result = KHRSwapchain.vkQueuePresentKHR(device.getPresentQueue().getVk(), presentInfo);
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) return recreateSwapchain(stack);
			VulkanErrorUtils.assertNoError(result, "Error presenting queue");
			
			frameIndex = (frameIndex + 1) % swappingBuffers;
			
			return Result.SUCCESS;
		}
	}
	
	public RenderPass getRenderPass()
	{
		return renderPass;
	}
	
	public void setClearColor(Color clearColor)
	{
		this.clearColor = clearColor;
	}
	
	public void setClearDepth(float clearDepth)
	{
		this.clearDepth = clearDepth;
	}
	
	public void setClearStencil(int clearStencil)
	{
		this.clearStencil = clearStencil;
	}
	
	@Override
	public void close()
	{
		renderPassBeginInfo.close();
		for (long imageView : imageViews) VK10.vkDestroyImageView(device.getVk(), imageView, null);
	}
}
