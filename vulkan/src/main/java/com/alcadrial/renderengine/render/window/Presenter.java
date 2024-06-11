package com.alcadrial.renderengine.render.window;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.RenderPass;
import com.alcadrial.renderengine.render.Surface;
import com.alcadrial.renderengine.render.Swapchain;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class Presenter implements AutoCloseable, VulkanDebug {
	
	public static class FrameData {
		
		private long image;
		private long imageView;
		private FrameBuffer frameBuffer;
		private CommandBufferData command;
		
		public FrameData(long image)
		{
			this.image = image;
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
		
		public CommandBufferData getCommand()
		{
			return command;
		}
		
		public void setCommand(CommandBufferData command)
		{
			this.command = command;
		}
	}
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public enum Result {
		SUCCESS,
		// FAILED,
		SWAPCHAIN_OUT_OF_DATE,
		EMPTY_SURFACE;
	}
	
	private final int swappingBuffers;
	
	private Device device;
	private Surface surface;
	private Swapchain swapchain;
	private RenderPass renderPass;
	@Nullable
	private BiFunction<Integer, Integer, Texture> depthTextureFactory;
	
	private boolean isSwapchainValid;
	private ReentrantLock swapchainRecreationLock;
	
	private VkRenderPassBeginInfo renderPassBeginInfo;
	
	private List<FrameData> frames;
	
	private Semaphore graphicsQueueSemaphor;
	
	private int frameIndex;
	
	private Color clearColor;
	private float clearDepth;
	private int clearStencil;
	
	public Presenter(Device device, VulkanWindow window, @Nullable BiFunction<Integer, Integer, Texture> depthTextureFactory, int swappingBuffers)
	{
		this.device = device;
		surface = window.getSurface();
		this.depthTextureFactory = depthTextureFactory;
		this.swappingBuffers = swappingBuffers;
		clearColor = Color.BLACK;
		clearDepth = 1;
		clearStencil = 0;
		graphicsQueueSemaphor = new Semaphore(1);
		swapchainRecreationLock = new ReentrantLock();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PhysicalDevice physicalDevice = device.getPhysicalDevice();
			
			surface.init(physicalDevice, stack);
			
			VkAttachmentDescription.Buffer attachmentDescriptions = VkAttachmentDescription.calloc(depthTextureFactory != null ? 2 : 1, stack);
			
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
			
			Texture depthTexture = null;
			if (depthTextureFactory != null)
			{
				VkExtent2D extent = surface.getExtent();
				depthTexture = depthTextureFactory.apply(extent.width(), extent.height());
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
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getVk(), surface.getVk(), capabilities), "Failed to create swap chain");
			
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
			
			initialize(stack, depthTexture);
		}
		window.addSurfaceUpdateActions(s -> {
			swapchainRecreationLock.lock();
			isSwapchainValid = false;
			swapchainRecreationLock.unlock();
		});
	}
	
	private void initialize(MemoryStack stack, @Nullable Texture depthTexture)
	{
		int[] p = new int[1];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVk(), swapchain.getVk(), p, null));
		long[] p0 = new long[p[0]];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVk(), swapchain.getVk(), p, p0));
		
		frames = new ArrayList<>(p[0]);
		
		for (long image : p0) frames.add(new FrameData(image));
		
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
		
		for (FrameData frame : frames)
		{
			imageViewCreateInfo.image(frame.getImage());
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateImageView(device.getVk(), imageViewCreateInfo, null, p0), "Failed to create image views");
			frame.setImageView(p0[0]);
			
			attachments.put(0, p0[0]);
			framebufferCreateInfo.pAttachments(attachments);
			FrameBuffer frameBuffer = new FrameBuffer(device, framebufferCreateInfo);
			frame.setFrameBuffer(frameBuffer);
		}
		
		renderPassBeginInfo = VkRenderPassBeginInfo.calloc();
		renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
		renderPassBeginInfo.renderPass(renderPass.getVk());
		
		List<VkCommandBuffer> commandBuffers = device.createMultiUseCommandBuffer(stack, 3);
		
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
			
			frames.get(i).setCommand(new CommandBufferData(device, commandBuffers.get(i), p1[0][0], p1[1][0], p1[2][0]));
		}
		isSwapchainValid = true;
	}
	
	private boolean recreateSwapchain(MemoryStack stack)
	{
		if (isSwapchainValid) return false;
		
		VkExtent2D extent = surface.getExtent();
		if (extent.width() == 0 || extent.height() == 0) return false;
		
		Device oldDevice = device;
		List<FrameData> oldFrames = frames;
		Swapchain oldSwapchain = swapchain;
		
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
		
		oldDevice.waitAsyncIdle(() -> {
			for (FrameData frame : oldFrames)
			{
				frame.frameBuffer.close();
				VK10.vkDestroyImageView(oldDevice.getVk(), frame.imageView, null);
			}
			oldSwapchain.close();
		});
		
		initialize(stack, depthTextureFactory != null ? depthTextureFactory.apply(extent.width(), extent.height()) : null);
		isSwapchainValid = true;
		return true;
	}
	
	public void beginRenderPass(CommandBufferData commandBuffer)
	{
		swapchainRecreationLock.lock();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			FrameBuffer frameBuffer = frames.get(frameIndex).getFrameBuffer();
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
		finally
		{
			swapchainRecreationLock.unlock();
		}
	}
	
	public Result present()
	{
		return present((d, i) -> {}, () -> {});
	}
	
	public Result present(BiConsumer<CommandBufferData, Integer> onPresentAction, Runnable onCommandBufferEndAction)
	{
		swapchainRecreationLock.lock();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			recreateSwapchain(stack);
			
			IntBuffer p = stack.mallocInt(1);
			
			CommandBufferData commandBuffer = frames.get(frameIndex).getCommand();
			int result = KHRSwapchain.vkAcquireNextImageKHR(device.getVk(), swapchain.getVk(), Integer.MAX_VALUE, commandBuffer.getImageAvailableSemaphore(), 0, p);
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				isSwapchainValid = false;
				return Result.SWAPCHAIN_OUT_OF_DATE;
			}
			VulkanErrorUtils.assertNoError(result, "Error acquiring next image");
			
			graphicsQueueSemaphor.acquireUninterruptibly();
			device.resetFence(commandBuffer.getFence());
			
			commandBuffer.reset();
			VulkanErrorUtils.assertStatement(commandBuffer.startRecording(), "Failed to record commands");
			onPresentAction.accept(commandBuffer, frameIndex);
			VulkanErrorUtils.assertStatement(commandBuffer.endRecording(), "Failed to record commands");
			
			LongBuffer p1 = stack.longs(commandBuffer.getRenderFinishedSemaphore());
			
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitSemaphores(stack.longs(commandBuffer.getImageAvailableSemaphore()));
			submitInfo.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			submitInfo.pCommandBuffers(VulkanUtils.toAddressBuffer(stack, commandBuffer));
			submitInfo.pSignalSemaphores(p1);
			
			device.getGraphicsQueue().submit(submitInfo, commandBuffer.getFence());
			device.waitAsyncForFences(() -> {
				try
				{
					onCommandBufferEndAction.run();
				}
				finally
				{
					graphicsQueueSemaphor.release();
				}
			}, commandBuffer.getFence());
			
			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
			presentInfo.pWaitSemaphores(p1);
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(swapchain.getVk()));
			presentInfo.pImageIndices(p);
			
			result = device.getPresentQueue().present(presentInfo);
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR)
			{
				isSwapchainValid = false;
				return Result.SWAPCHAIN_OUT_OF_DATE;
			}
			VulkanErrorUtils.assertNoError(result, "Error presenting queue");
			
			frameIndex = (frameIndex + 1) % swappingBuffers;
			
			return Result.SUCCESS;
		}
		finally
		{
			swapchainRecreationLock.unlock();
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
		for (FrameData frame : frames) VK10.vkDestroyImageView(device.getVk(), frame.getImageView(), null);
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
		for (int i = 0; i < frames.size(); i++)
		{
			Debug.print("Frame " + i + (i == frameIndex ? " Current" : "") + " [");
			FrameData frame = frames.get(i);
			Debug.indent();
			Debug.print("Image: " + Debug.address(frame.image));
			Debug.print("ImageView: " + Debug.address(frame.imageView));
			Debug.print("FrameBuffer", frame.frameBuffer);
			Debug.print("Command", frame.command);
			Debug.deindent();
			Debug.print("]");
		}
		Debug.deindent();
		Debug.print("]");
	}
}
