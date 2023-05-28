package com.alcadrial.renderengine.vulkan;

import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.vulkan.util.Pair;
import com.alcadrial.renderengine.vulkan.util.VulkanErrorUtils;
import com.alcadrial.renderengine.vulkan.util.VulkanUtils;

public class Surface implements AutoCloseable {
	
	private Vulkan vulkan;
	private long surface;
	private Pair<VkSurfaceFormatKHR, ByteBuffer> format;
	private int mode;
	private Pair<VkExtent2D, ByteBuffer> extent;
	private Vector2i maxSize;
	private Vector2i minSize;
	private Vector2i size;
	
	public Surface(Vulkan vulkan, long window)
	{
		this.vulkan = vulkan;
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(GLFWVulkan.glfwCreateWindowSurface(vulkan.getInstance(), window, null, p), "Failed to create surface");
		surface = p[0];
	}
	
	public void init(PhysicalDevice physicalDevice, MemoryStack stack)
	{
		int[] p = new int[1];
		VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getDevice(), surface, p, null), "Failed to initialize surface");
		VulkanErrorUtils.nonNull(p[0], "Failed to initialize surface");
		VkSurfaceFormatKHR.Buffer formats = VkSurfaceFormatKHR.malloc(p[0], stack);
		VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getDevice(), surface, p, formats), "Failed to initialize surface");
		
		VkSurfaceFormatKHR format = null;
		for (VkSurfaceFormatKHR format0 : formats) if (format0.format() == VK10.VK_FORMAT_B8G8R8A8_UNORM && format0.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
		{
			format = format0;
			break;
		}
		VulkanErrorUtils.nonNull(format, "Valid format not found");
		this.format = VulkanUtils.copy(format, VkSurfaceFormatKHR::create);
		
		VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.getDevice(), surface, p, null), "Failed to initialize surface");
		int[] presentModes = new int[p[0]];
		VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.getDevice(), surface, p, presentModes), "Failed to initialize surface");
		
		mode = KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
		for (int i : presentModes) if (i == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR)
		{
			mode = i;
			break;
		}
		
		VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
		VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getDevice(), surface, capabilities), "Failed to initialize surface");
		
		if (capabilities.currentExtent().width() != -1) extent = VulkanUtils.copy(capabilities.currentExtent(), VkExtent2D::create);
		else
		{
			extent = VulkanUtils.allocate(VkExtent2D.SIZEOF, VkExtent2D::create);
			VkExtent2D e = extent.getLeft();
			
			VkExtent2D minExtent = capabilities.minImageExtent();
			VkExtent2D maxExtent = capabilities.maxImageExtent();
			
			maxSize = Vector2i.ofImmutable(minExtent.width(), minExtent.height());
			minSize = Vector2i.ofImmutable(maxExtent.width(), maxExtent.height());
			
			if (e.width() < minExtent.width()) e.width(minExtent.width());
			else if (e.width() > maxExtent.width()) e.width(maxExtent.width());
			
			if (e.height() < minExtent.height()) e.height(minExtent.height());
			else if (e.height() > maxExtent.height()) e.height(maxExtent.height());
			
			extent = new Pair<>(e, null);
		}
		size = Vector2i.ofImmutable(extent.getLeft().width(), extent.getLeft().height());
		System.out.println("size " + size);
	}
	
	public long getSurface()
	{
		return surface;
	}
	
	public VkSurfaceFormatKHR getFormat()
	{
		return format.getLeft();
	}
	
	public void setFormat(VkSurfaceFormatKHR format)
	{
		VulkanErrorUtils.nonNull(format);
		MemoryUtil.memFree(this.format.getRigth());
		this.format = VulkanUtils.copy(format, VkSurfaceFormatKHR::create);
	}
	
	public int getMode()
	{
		return mode;
	}
	
	public VkExtent2D getExtent()
	{
		return extent.getLeft();
	}
	
	public Vector2i getMaxSize()
	{
		return maxSize;
	}
	
	public Vector2i getMinSize()
	{
		return minSize;
	}
	
	public Vector2i getSize()
	{
		return size;
	}
	
	@Override
	public void close()
	{
		MemoryUtil.memFree(format.getRigth());
		MemoryUtil.memFree(extent.getRigth());
		KHRSurface.vkDestroySurfaceKHR(vulkan.getInstance(), surface, null);
	}
}
