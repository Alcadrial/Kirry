package com.alcadrial.renderengine.render;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.util.AbstractVulkanResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class Surface extends AbstractVulkanResource<Long> {
	
	static
	{
		RenderEngine.assertLoaded();
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
			format = VK10.VK_FORMAT_B8G8R8A8_UNORM;
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
			VulkanErrorUtils.assertNoError(GLFWVulkan.glfwCreateWindowSurface(((@NonNull Vulkan) RenderEngine.getUnsafeVulkan()).getInstance(), windowVk, null, p), "Failed to create surface");
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
	
	private static record InitData(PhysicalDevice physicalDevice, Pair<VkSurfaceFormatKHR, ByteBuffer> format, int mode, Pair<VkExtent2D, ByteBuffer> extent, Vector2i maxSize, Vector2i minSize, Vector2i size) {}
	
	private static final VarHandle DATA_HANDLE;
	static {
		try
		{
			DATA_HANDLE = MethodHandles.lookup().findVarHandle(Surface.class, "data", InitData.class);
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
	private InitData data;
	
	private Surface(Builder builder, long vk, VulkanWindow window) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			this.vk = vk;
			this.window = window;
			format = builder.format;
			colorspace = builder.colorspace;
			targetPresentMode = builder.targetPresentMode;
			data = init();
		}
	}

	private InitData init() throws VulkanException
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
			Pair<VkSurfaceFormatKHR, ByteBuffer> format = VulkanUtils.copy(VulkanErrorUtils.nonNull(vkFormat, "Valid format not found"), VkSurfaceFormatKHR::create);
			
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
			
			Pair<VkExtent2D, ByteBuffer> extent;
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
				extent = VulkanUtils.allocate(VkExtent2D.SIZEOF, VkExtent2D::create);
				VkExtent2D e = extent.l();
				
				if (e.width() < minExtent.width()) e.width(minExtent.width());
				else if (e.width() > maxExtent.width()) e.width(maxExtent.width());
				
				if (e.height() < minExtent.height()) e.height(minExtent.height());
				else if (e.height() > maxExtent.height()) e.height(maxExtent.height());
			}
			Vector2i size = Vector2i.ofImmutable(extent.l().width(), extent.l().height());
			return new InitData(physicalDevice, format, mode, extent, maxSize, minSize, size);
		}
	}
	public void reinit() throws VulkanException
	{
		InitData data = init();
		DATA_HANDLE.compareAndSet(this.data, data);
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
	
	public VkSurfaceFormatKHR getFormat()
	{
		return data.format.l();
	}
	
	public int getMode()
	{
		return data.mode;
	}
	
	public VkExtent2D getExtent()
	{
		return data.extent.l();
	}
	
	public Vector2i getMaxSize()
	{
		return data.maxSize;
	}
	
	public Vector2i getMinSize()
	{
		return data.minSize;
	}
	
	public Vector2i getSize()
	{
		return data.size;
	}
	
	@Override
	public void close()
	{
		InitData data = this.data;
		MemoryUtil.memFree(data.format.r());
		MemoryUtil.memFree(data.extent.r());
		KHRSurface.vkDestroySurfaceKHR(RenderEngine.getVulkan().getInstance(), vk, null);
		vk = 0L;
		super.close();
	}
	
	@Override
	public void debug()
	{
		InitData data = this.data;
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("MaxSize: " + data.maxSize.x() + "x" + data.maxSize.y());
		Debug.print("MinSize: " + data.minSize.x() + "x" + data.minSize.y());
		Debug.print("Size: " + data.size.x() + "x" + data.size.y());
	}
}
