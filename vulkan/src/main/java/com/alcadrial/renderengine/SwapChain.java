package com.alcadrial.renderengine;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public class SwapChain implements AutoCloseable {
	
	private Device device;
	private long swapChain;
	private int imageFormat;
	private VkExtent2D extent;
	private List<Long> images;
	private List<Long> imageViews;
	
	public SwapChain(Device device, long swapChain, int[] availables, VkSurfaceFormatKHR format, VkExtent2D extent)
	{
		this.device = device;
		this.swapChain = swapChain;
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapChain, availables, null));
		long[] p = new long[availables[0]];
		VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapChain, availables, p));
		
		images = new ArrayList<>(availables[0]);
		
		for (long image : p) images.add(image);
		
		imageFormat = format.format();
		this.extent = VkExtent2D.create().set(extent);
		
		imageViews = new ArrayList<>(images.size());
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] imageView = new long[1];
			
			for (long image : images)
			{
				VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack);
				
				createInfo.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
				createInfo.image(image);
				createInfo.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D);
				createInfo.format(imageFormat);
				
				createInfo.components().r(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
				createInfo.components().g(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
				createInfo.components().b(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
				createInfo.components().a(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
				
				createInfo.subresourceRange().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
				createInfo.subresourceRange().baseMipLevel(0);
				createInfo.subresourceRange().levelCount(1);
				createInfo.subresourceRange().baseArrayLayer(0);
				createInfo.subresourceRange().layerCount(1);
				
				VulkanErrorUtils.assertNoError(VK10.vkCreateImageView(device.getDevice(), createInfo, null, imageView), "Failed to create image views");
				imageViews.add(imageView[0]);
			}
		}
	}
	
	public Device getDevice()
	{
		return device;
	}
	
	public long getSwapchain()
	{
		return swapChain;
	}
	
	public int getImageFormat()
	{
		return imageFormat;
	}
	
	public VkExtent2D getExtent()
	{
		return extent;
	}
	
	public List<Long> getImages()
	{
		return images;
	}
	
	public List<Long> getImageViews()
	{
		return imageViews;
	}
	
	@Override
	public void close()
	{
		for (long imageView : imageViews) VK10.vkDestroyImageView(device.getDevice(), imageView, null);
	}
}
