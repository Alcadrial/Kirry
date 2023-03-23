package com.alcadrial.renderengine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR.Buffer;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class Device implements AutoCloseable {
	
	private VkDevice device;
	private PhysicalDevice physicalDevice;
	private Queue graphicsQueue;
	private Queue presentQueue;
	private long surface;
	private SwapChain swapChain;
	
	public Device(VkInstance instance, long window, long surface, VkDevice device, PhysicalDevice physicalDevice)
	{
		this.device = VulkanErrorUtils.nonNull(device);
		this.physicalDevice = VulkanErrorUtils.nonNull(physicalDevice);
		this.surface = VulkanErrorUtils.nonNull(surface);
		int[][] size = new int[2][1];
		GLFW.glfwGetWindowSize(VulkanErrorUtils.nonNull(window), size[0], size[1]);
		createSwapChain(size[0][0], size[1][0]);
	}
	
	private void createSwapChain(int width, int height)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getDevice(), surface, capabilities), "Failed to create swap chain");
			
			int[] p = new int[1];
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getDevice(), surface, p, null), "Failed to create swap chain");
			
			Buffer formats = null;
			if (p[0] != 0)
			{
				formats = VkSurfaceFormatKHR.malloc(p[0], stack);
				VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getDevice(), surface, p, formats), "Failed to create swap chain");
			}
			
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.getDevice(), surface, p, null), "Failed to create swap chain");
			
			int[] presentModes = null;
			if (p[0] != 0)
			{
				presentModes = new int[p[0]];
				VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.getDevice(), surface, p, presentModes), "Failed to create swap chain");
			}
			
			VkSurfaceFormatKHR format = formats.get(0);
			for (VkSurfaceFormatKHR format0 : formats) if (format0.format() == VK10.VK_FORMAT_B8G8R8_UNORM && format0.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
			{
				format = format0;
				break;
			}
			
			int mode = KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
			for (int i : presentModes) if (i == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR)
			{
				mode = i;
				break;
			}
			
			VkExtent2D extent;
			if (capabilities.currentExtent().width() != 0xFFFFFFFF)
			{
				extent = capabilities.currentExtent();
			}
			else
			{
				
				VkExtent2D actualExtent = VkExtent2D.malloc(stack).set(width, height);
				
				VkExtent2D minExtent = capabilities.minImageExtent();
				VkExtent2D maxExtent = capabilities.maxImageExtent();
				
				if (actualExtent.width() < minExtent.width()) actualExtent.width(minExtent.width());
				else if (actualExtent.width() > maxExtent.width()) actualExtent.width(maxExtent.width());
				
				if (actualExtent.height() < minExtent.height()) actualExtent.height(minExtent.height());
				else if (actualExtent.height() > maxExtent.height()) actualExtent.height(maxExtent.height());
				
				extent = actualExtent;
			}
			
			int[] images = new int[capabilities.minImageCount() + 1];
			
			if (capabilities.maxImageCount() > 0) images[0] = capabilities.maxImageCount();
			
			VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
			
			createInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
			createInfo.surface(surface);
			
			createInfo.minImageCount(images[0]);
			
			createInfo.imageFormat(format.format());
			createInfo.imageColorSpace(format.colorSpace());
			createInfo.imageExtent(extent);
			createInfo.imageArrayLayers(1);
			createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
			
			if (physicalDevice.getQueueGraphicsFamilyIndex() == physicalDevice.getQueueSurfaceFamilyIndex()) createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			else
			{
				createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
				createInfo.pQueueFamilyIndices(stack.ints(physicalDevice.getQueueGraphicsFamilyIndex(), physicalDevice.getQueueSurfaceFamilyIndex()));
			}
			
			createInfo.preTransform(capabilities.currentTransform());
			createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			createInfo.presentMode(mode);
			createInfo.clipped(true);
			
			createInfo.oldSwapchain(VK10.VK_NULL_HANDLE);
			
			long[] p0 = new long[1];
			VulkanErrorUtils.assertNoError(KHRSwapchain.vkCreateSwapchainKHR(device, createInfo, null, p0), "Failed to create swap chain");
			swapChain = new SwapChain(this, p0[0], images, format, extent);
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
	
	public long getSurface()
	{
		return surface;
	}
	
	public SwapChain getSwapChain()
	{
		return swapChain;
	}
	
	@Override
	public void close()
	{
		swapChain.close();
		KHRSwapchain.vkDestroySwapchainKHR(device, swapChain.getSwapchain(), null);
		VK10.vkDestroyDevice(device, null);
	}
}
