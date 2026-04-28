package com.alcadrial.renderengine.render;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.util.AbstractVulkanResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class Surface extends AbstractVulkanResource<Long> {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	public static class Builder {
		
		private int format;
		private int colorspace;
		private int targetPresentMode;
		private long windowVk;
		private long vk;
		private @Nullable VulkanWindow window;
		
		public Builder()
		{
			format = VK10.VK_FORMAT_B8G8R8A8_SRGB;
			colorspace = KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
			targetPresentMode = KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
			windowVk = 0;
			vk = 0;
			window = null;
		}
		
		public Builder windowVk(long windowVk)
		{
			this.windowVk = windowVk;
			return this;
		}
		
		public long getVk()
		{
			return vk;
		}
		
		public Builder create() throws VulkanException
		{
			long windowVk;
			if (window != null)
			{
				windowVk = window.getVk();
			}
			else
			{
				windowVk = VulkanErrorUtils.nonNull(this.windowVk, "Window must be set before creating a surface");
			}
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(GLFWVulkan.glfwCreateWindowSurface(((@NonNull Vulkan) RenderEngineVulkan.getUnsafeVulkan()).getInstance(), windowVk, null, p), "Failed to create surface");
			vk = p[0];
			return this;
		}
		
		public Builder window(VulkanWindow window)
		{
			this.window = window;
			return this;
		}
		
		public Surface build() throws VulkanException
		{
			return new Surface(this, VulkanErrorUtils.nonNull(vk, "Surface must be created before building"), VulkanErrorUtils.nonNull(this.window, "Window must be built before building a surface"));
		}
	}
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	public static record SurfaceData(PhysicalDevice physicalDevice, VkSurfaceFormatKHR format, int mode, VkExtent2D extent, Vector2i maxSize, Vector2i minSize, Vector2i size) {}
	
	private static final VarHandle DATA_HANDLE;
	static
	{
		try
		{
			DATA_HANDLE = MethodHandles.lookup().findVarHandle(Surface.class, "data", SurfaceData.class);
		}
		catch (Exception e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}
	private final VulkanWindow window;
	private final int format;
	private final int colorspace;
	private final int targetPresentMode;
	private SurfaceData data;
	
	private Surface(Builder builder, long vk, VulkanWindow window) throws VulkanException
	{
		this.vk = vk;
		this.window = window;
		format = builder.format;
		colorspace = builder.colorspace;
		targetPresentMode = builder.targetPresentMode;
		data = init();
	}
	
	private SurfaceData init() throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PhysicalDevice physicalDevice = window.getDevice().getPhysicalDevice();
			
			int[] p = new int[1];
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getVk(), vk, p, null), "Failed to initialize surface");
			VulkanErrorUtils.nonNull(p[0], "Failed to initialize surface");
			VkSurfaceFormatKHR.Buffer formats = VkSurfaceFormatKHR.malloc(p[0], stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getVk(), vk, p, formats), "Failed to initialize surface");
			
			VkSurfaceFormatKHR vkFormat = null;
			for (VkSurfaceFormatKHR format0 : formats) if (format0.format() == format && format0.colorSpace() == colorspace)
			{
				vkFormat = format0;
				break;
			}
			VkSurfaceFormatKHR format = VulkanUtils.copy(VulkanErrorUtils.nonNull(vkFormat, "Valid format not found"), VkSurfaceFormatKHR::create);
			
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.getVk(), vk, p, null), "Failed to initialize surface");
			int[] presentModes = new int[p[0]];
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.getVk(), vk, p, presentModes), "Failed to initialize surface");
			
			int mode = KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
			for (int i : presentModes) if (i == targetPresentMode)
			{
				mode = i;
				break;
			}
			
			VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getVk(), vk, capabilities), "Failed to initialize surface");
			
			VkExtent2D extent = VkExtent2D.malloc();
			VkExtent2D minExtent = capabilities.minImageExtent();
			VkExtent2D maxExtent = capabilities.maxImageExtent();
			Vector2i maxSize = Vector2i.ofImmutable(minExtent.width(), minExtent.height());
			Vector2i minSize = Vector2i.ofImmutable(maxExtent.width(), maxExtent.height());
			if (capabilities.currentExtent().width() != 0xFFFFFFFF)
			{
				extent = VulkanUtils.copy(capabilities.currentExtent(), VkExtent2D::create);
			}
			else
			{
				if (extent.width() < minExtent.width()) extent.width(minExtent.width());
				else if (extent.width() > maxExtent.width()) extent.width(maxExtent.width());
				
				if (extent.height() < minExtent.height()) extent.height(minExtent.height());
				else if (extent.height() > maxExtent.height()) extent.height(maxExtent.height());
			}
			Vector2i size = Vector2i.ofImmutable(extent.width(), extent.height());
			return new SurfaceData(physicalDevice, format, mode, extent, maxSize, minSize, size);
		}
	}
	
	public SurfaceData reinit() throws VulkanException
	{
		SurfaceData oldData = this.data;
		SurfaceData data = init();
		if (DATA_HANDLE.compareAndSet(this, this.data, data))
		{
			oldData.format.free();
			oldData.extent.free();
		}
		return data;
	}
	
	@Override
	public Long getVk()
	{
		return vk;
	}
	
	public VulkanWindow getWindow()
	{
		return window;
	}
	
	public SurfaceData getData()
	{
		return data;
	}
	
	@Override
	public void close()
	{
		SurfaceData data = this.data;
		data.format.free();
		data.extent.free();
		KHRSurface.vkDestroySurfaceKHR(RenderEngineVulkan.getVulkan().getInstance(), vk, null);
		vk = 0L;
		super.close();
	}
	
	@Override
	public void debug()
	{
		SurfaceData data = this.data;
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("MaxSize: " + data.maxSize.x() + "x" + data.maxSize.y());
		Debug.print("MinSize: " + data.minSize.x() + "x" + data.minSize.y());
		Debug.print("Size: " + data.size.x() + "x" + data.size.y());
	}
}
