package com.alcadrial.renderengine.render.window;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Surface;
import com.alcadrial.renderengine.render.Swapchain;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class Presenter implements AutoCloseable, VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public class FrameData implements AutoCloseable {
		
		private long image;
		private long imageView;
		private FrameBuffer frameBuffer;
		
		private long imageAvailable;
		private long renderFinished;
		private long fence;
		private final IntBuffer imageIndex;
		
		public FrameData(long image)
		{
			this.image = image;
			imageIndex = MemoryUtil.memAllocInt(1);
		}
		
		public long getImage()
		{
			return image;
		}
		
		public void setImage(long image)
		{
			this.image = image;
		}
		
		public long getImageView()
		{
			return imageView;
		}
		
		public void setImageView(long imageView)
		{
			this.imageView = imageView;
		}
		
		public FrameBuffer getFrameBuffer()
		{
			return frameBuffer;
		}
		
		public void setFrameBuffer(FrameBuffer frameBuffer)
		{
			this.frameBuffer = frameBuffer;
		}
		
		public long getImageAvailable()
		{
			return imageAvailable;
		}
		
		public void setImageAvailable(long imageAvailable)
		{
			this.imageAvailable = imageAvailable;
		}
		
		public long getRenderFinished()
		{
			return renderFinished;
		}
		
		public void setRenderFinished(long renderFinished)
		{
			this.renderFinished = renderFinished;
		}
		
		public long getFence()
		{
			return fence;
		}
		
		public void setFence(long fence)
		{
			this.fence = fence;
		}
		
		public IntBuffer getImageIndex()
		{
			return imageIndex;
		}
		
		@Override
		public void close()
		{
			VkDevice vk = device.getVk();
			VK10.vkDestroyImageView(vk, imageView, null);
			VK10.vkDestroySemaphore(vk, imageAvailable, null);
			VK10.vkDestroySemaphore(vk, renderFinished, null);
			VK10.vkDestroyFence(vk, fence, null);
			MemoryUtil.memFree(imageIndex);
			frameBuffer.close();
		}
	}
	
	public enum Result {
		SUCCESS,
		FAILED,
		SWAPCHAIN_OUT_OF_DATE,
		EMPTY_SURFACE;
	}
	
	private final int swappingBuffers;
	
	private Device device;
	private Surface surface;
	private Swapchain swapchain;
	private RenderPass renderPass;
	
	private boolean isSwapchainValid;
	private ReentrantLock swapchainRecreationLock;
	
	private FrameData[] frames;
	
	private Semaphore graphicsQueueSemaphor;
	
	private int frameIndex;
	private String name;
	
	public Presenter(Device device, VulkanWindow window, RenderPass renderPass, int swappingBuffers) throws VulkanException
	{
		this.device = device;
		surface = window.getSurface();
		this.renderPass = renderPass;
		this.swappingBuffers = swappingBuffers;
		graphicsQueueSemaphor = new Semaphore(1);
		swapchainRecreationLock = new ReentrantLock();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.getPhysicalDevice().getVk(), surface.getVk(), capabilities), "Failed to create swap chain");
			
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
			swapchainCreateInfo.surface(surface.getVk());
			swapchainCreateInfo.imageFormat(surface.getFormat().format());
			swapchainCreateInfo.imageColorSpace(surface.getFormat().colorSpace());
			swapchainCreateInfo.imageExtent(surface.getExtent());
			swapchainCreateInfo.presentMode(surface.getMode());
			
			swapchain = new Swapchain(device, surface, swapchainCreateInfo);
		}
		window.addSurfaceUpdateActions(s -> {
			swapchainRecreationLock.lock();
			isSwapchainValid = false;
			swapchainRecreationLock.unlock();
		});
	}
	
	public void setName(String name)
	{
		this.name = name;
		swapchain.setName("Swapchain (" + name + ")");
	}
	
	public void initialize(@Nullable Texture depthTexture) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			if (frames != null) recreateSwapchain(stack, depthTexture);
			
			int[] p = new int[1];
			VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVk(), swapchain.getVk(), p, null));
			long[] p0 = new long[p[0]];
			VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVk(), swapchain.getVk(), p, p0));
			
			frames = new FrameData[p[0]];
			
			for (int i = 0; i < p0.length; i++) frames[i] = new FrameData(p0[i]);
			
			p0 = new long[1];
			
			LongBuffer attachments = stack.mallocLong(depthTexture != null ? 2 : 1);
			if (depthTexture != null) attachments.put(1, depthTexture.getView().getVk());
			
			VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.malloc(stack);
			framebufferCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
			framebufferCreateInfo.pNext(0);
			framebufferCreateInfo.flags(0);
			framebufferCreateInfo.renderPass(renderPass.getVk());
			framebufferCreateInfo.width(surface.getExtent().width());
			framebufferCreateInfo.height(surface.getExtent().height());
			
			framebufferCreateInfo.layers(1);
			
			VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.malloc(stack);
			
			imageViewCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
			imageViewCreateInfo.pNext(0);
			imageViewCreateInfo.flags(0);
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
			
			for (int i = 0; i < frames.length; i++)
			{
				FrameData frame = frames[i];
				imageViewCreateInfo.image(frame.getImage());
				
				VulkanErrorUtils.assertNoError(VK10.vkCreateImageView(device.getVk(), imageViewCreateInfo, null, p0), "Failed to create image views");
				frame.setImageView(p0[0]);
				if (name != null) device.setObjectDebugName(VK10.VK_OBJECT_TYPE_IMAGE_VIEW, p0[0], "Frame" + i + " ImageView (" + name + ")");
				
				attachments.put(0, p0[0]);
				framebufferCreateInfo.pAttachments(attachments);
				FrameBuffer frameBuffer = new FrameBuffer(device, framebufferCreateInfo);
				if (name != null) frameBuffer.setName("Frame" + i + " FrameBuffer (" + name + ")");
				frame.setFrameBuffer(frameBuffer);
			}
			
			VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
			semaphoreInfo.sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
			
			VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
			fenceInfo.sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
			fenceInfo.flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT);
			
			long[][] p1 = new long[3][1];
			for (int i = 0; i < p[0]; i++)
			{
				VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device.getVk(), semaphoreInfo, null, p1[0]), "Failed to create synchronization objects for the frame " + i);
				VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device.getVk(), semaphoreInfo, null, p1[1]), "Failed to create synchronization objects for the frame " + i);
				VulkanErrorUtils.assertNoError(VK10.vkCreateFence(device.getVk(), fenceInfo, null, p1[2]), "Failed to create synchronization objects for the frame " + i);
				
				FrameData frame = frames[i];
				frame.setImageAvailable(p1[0][0]);
				frame.setRenderFinished(p1[1][0]);
				frame.setFence(p1[2][0]);
			}
			isSwapchainValid = true;
		}
	}
	
	private boolean recreateSwapchain(MemoryStack stack, @Nullable Texture depthTexture) throws VulkanException
	{
		if (isSwapchainValid) return false;
		
		VkExtent2D extent = surface.getExtent();
		if (extent.width() == 0 || extent.height() == 0) return false;
		
		Device oldDevice = device;
		FrameData[] oldFrames = frames;
		Swapchain oldSwapchain = swapchain;
		frames = null;
		
		VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
		VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.getPhysicalDevice().getVk(), surface.getVk(), capabilities), "Failed to recreate swap chain");
		
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
		swapchainCreateInfo.surface(surface.getVk());
		swapchainCreateInfo.imageFormat(surface.getFormat().format());
		swapchainCreateInfo.imageColorSpace(surface.getFormat().colorSpace());
		swapchainCreateInfo.imageExtent(extent);
		swapchainCreateInfo.presentMode(surface.getMode());
		swapchainCreateInfo.oldSwapchain(oldSwapchain.getVk());
		
		swapchain = new Swapchain(device, surface, swapchainCreateInfo);
		if (name != null) swapchain.setName("Swapchain (" + name + ")");
		
		RenderEngine.submitAsync(() -> {
			oldDevice.waitIdle();
			for (FrameData frame : oldFrames)
			{
				frame.frameBuffer.close();
				VK10.vkDestroyImageView(oldDevice.getVk(), frame.imageView, null);
			}
			oldSwapchain.close();
		});
		
		initialize(depthTexture);
		isSwapchainValid = true;
		return true;
	}
	
	public Result startPresentation() throws VulkanException
	{
		swapchainRecreationLock.lock();
		if (!isSwapchainValid) return Result.SWAPCHAIN_OUT_OF_DATE;
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			FrameData frame = frames[frameIndex];
			int result = KHRSwapchain.vkAcquireNextImageKHR(device.getVk(), swapchain.getVk(), Integer.MAX_VALUE, frame.getImageAvailable(), 0, frame.getImageIndex());
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				isSwapchainValid = false;
				return Result.SWAPCHAIN_OUT_OF_DATE;
			}
			if (result < 0) return Result.FAILED;
			
			graphicsQueueSemaphor.acquireUninterruptibly();
			device.resetFences(frame.getFence());
			
			return Result.SUCCESS;
		}
		finally
		{
			swapchainRecreationLock.unlock();
		}
	}
	
	public Result endPresentation(CommandBuffer commandBuffer) throws VulkanException
	{
		return endPresentation(commandBuffer, () -> {});
	}
	
	public Result endPresentation(CommandBuffer commandBuffer, Runnable onCommandBufferEndAction) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			FrameData frame = frames[frameIndex];
			
			LongBuffer p1 = stack.longs(frame.getRenderFinished());
			
			VkSubmitInfo submitInfo = VkSubmitInfo.malloc(stack);
			submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
			submitInfo.pNext(0);
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitSemaphores(stack.longs(frame.getImageAvailable()));
			submitInfo.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			submitInfo.pCommandBuffers(VulkanUtils.toAddressBuffer(stack, commandBuffer));
			submitInfo.pSignalSemaphores(p1);
			
			RenderEngine.composeAfter(device.getGraphicsQueue().submitAsync(submitInfo, frame.getFence()), fences -> {
				try
				{
					onCommandBufferEndAction.run();
				}
				finally
				{
					graphicsQueueSemaphor.release();
				}
			});
			
			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
			presentInfo.pWaitSemaphores(p1);
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(swapchain.getVk()));
			presentInfo.pImageIndices(frame.getImageIndex());
			presentInfo.pResults(null);
			
			int result = device.getPresentQueue().present(presentInfo);
			
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR)
			{
				isSwapchainValid = false;
				return Result.SWAPCHAIN_OUT_OF_DATE;
			}
			VulkanErrorUtils.assertNoError(result, "Error presenting queue");
			
			frameIndex = (frameIndex + 1) % swappingBuffers;
			
			return Result.SUCCESS;
		}
	}
	
	public RenderPass getRenderPass()
	{
		return renderPass;
	}
	
	public int getFrameIndex()
	{
		return frameIndex;
	}
	
	public FrameBuffer getFrameBuffer(int frameIndex)
	{
		return frames[frameIndex].getFrameBuffer();
	}
	
	@Override
	public void close()
	{
		if (frames != null) for (FrameData frame : frames) frame.close();
		swapchain.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Device", device);
		Debug.print("Surface", surface);
		Debug.print("Swapchain" + (isSwapchainValid ? "" : " Invalid"), swapchain);
		Debug.print("RenderPass ", renderPass);
		Debug.print("Frames [");
		Debug.indent();
		for (int i = 0; i < frames.length; i++)
		{
			Debug.print("Frame " + i + (i == frameIndex ? " Current" : "") + " [");
			FrameData frame = frames[i];
			Debug.indent();
			Debug.print("Image", frame.image);
			Debug.print("ImageView", frame.imageView);
			Debug.print("FrameBuffer", frame.frameBuffer);
			Debug.print("ImageAvailable", frame.imageAvailable);
			Debug.print("RenderFinished", frame.renderFinished);
			Debug.print("Fence", frame.fence);
			Debug.deindent();
			Debug.print("]");
		}
		Debug.deindent();
		Debug.print("]");
	}
}
