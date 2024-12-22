package com.alcadrial.renderengine.render.texture;

import java.util.concurrent.CompletableFuture;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;

import com.alcadrial.renderengine.core.util.Nonnull;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.Memory;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class Texture extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static @Nonnull Texture create1dTexture(Device device, int width, int format, int usage, int aspectMask) throws VulkanException
	{
		return new Texture(device, width, 1, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, 1, VK10.VK_IMAGE_TYPE_1D);
	}
	
	public static @Nonnull Texture create1dTexture(Device device, int width, int format, int usage, int aspectMask, int mipLevels) throws VulkanException
	{
		return new Texture(device, width, 1, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, mipLevels, VK10.VK_IMAGE_TYPE_1D);
	}
	
	public static @Nonnull Texture create2dTexture(Device device, int width, int height, int format, int usage, int aspectMask) throws VulkanException
	{
		return new Texture(device, width, height, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, 1, VK10.VK_IMAGE_TYPE_2D);
	}
	
	public static @Nonnull Texture create2dTexture(Device device, int width, int height, int format, int usage, int aspectMask, int mipLevels) throws VulkanException
	{
		return new Texture(device, width, height, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, mipLevels, VK10.VK_IMAGE_TYPE_2D);
	}
	
	public static @Nonnull Texture create3dTexture(Device device, int width, int height, int depth, int format, int usage, int aspectMask) throws VulkanException
	{
		return new Texture(device, width, height, depth, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, 1, VK10.VK_IMAGE_TYPE_3D);
	}
	
	public static @Nonnull Texture create3dTexture(Device device, int width, int height, int depth, int format, int usage, int aspectMask, int mipLevels) throws VulkanException
	{
		return new Texture(device, width, height, depth, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, mipLevels, VK10.VK_IMAGE_TYPE_3D);
	}
	
	private TextureView view;
	private Memory memory;
	private int length;
	private int width;
	private int height;
	private int depth;
	private int mipLevels;
	private int format;
	private int imageType;
	private int aspectMask;
	private int layout;
	
	private Texture(Device device, int width, int height, int depth, int format, int tiling, int usage, int aspectMask, int mipLevels, int imageType) throws VulkanException
	{
		super(device);
		length = width * height * depth;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.mipLevels = mipLevels;
		this.format = format;
		this.imageType = imageType;
		this.aspectMask = aspectMask;
		layout = VK10.VK_IMAGE_LAYOUT_UNDEFINED;
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkImageCreateInfo createInfo = VkImageCreateInfo.malloc(stack);
			createInfo.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
			createInfo.pNext(0);
			createInfo.flags(0);
			createInfo.imageType(imageType);
			createInfo.format(format);
			createInfo.extent().width(width).height(height).depth(depth);
			createInfo.mipLevels(mipLevels);
			createInfo.arrayLayers(1);
			createInfo.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
			createInfo.tiling(tiling);
			createInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			createInfo.usage(usage);
			createInfo.pQueueFamilyIndices(null);
			createInfo.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateImage(device.getVk(), createInfo, null, p), "Failed to create image");
			vk = p[0];
			
			memory = device.allocateMemory(stack, this, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
		}
		
		view = new TextureView(this);
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	public Memory getMemory()
	{
		return memory;
	}
	
	public CompletableFuture<?> changeLayout(int layout) throws VulkanException
	{
		if (this.layout != layout)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
				changeLayout(commandBuffer, layout);
				return device.submitSingleUseCommandBuffer(stack, commandBuffer);
			}
		}
		return CompletableFuture.completedFuture(null);
	}
	
	public void changeLayout(CommandBuffer commandBuffer, int layout)
	{
		if (this.layout != layout)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
				barrier.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
				barrier.oldLayout(this.layout);
				barrier.newLayout(layout);
				barrier.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED);
				barrier.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED);
				barrier.image(vk);
				barrier.subresourceRange().aspectMask(aspectMask);
				barrier.subresourceRange().baseMipLevel(0);
				barrier.subresourceRange().levelCount(1);
				barrier.subresourceRange().baseArrayLayer(0);
				barrier.subresourceRange().layerCount(1);
				
				int sourceStage = 0;
				int destinationStage = 0;
				
				if (this.layout == VK10.VK_IMAGE_LAYOUT_UNDEFINED)
				{
					barrier.srcAccessMask(VK13.VK_ACCESS_NONE);
					sourceStage = VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
					
					switch (layout)
					{
						case VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL -> {
							barrier.dstAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
							destinationStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
						}
						case VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL -> {
							barrier.dstAccessMask(VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
							destinationStage = VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
						}
						default -> throw new UnsupportedOperationException("Transition not supported: " + this.layout + " -> " + layout);
					}
				}
				else if (this.layout == VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && layout == VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
				{
					barrier.srcAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
					barrier.dstAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT);
					
					sourceStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
					destinationStage = VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
				}
				else if (this.layout == VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL && layout == VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				{
					barrier.srcAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT);
					barrier.dstAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
					
					sourceStage = VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
					destinationStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
				}
				else throw new UnsupportedOperationException("Transition not supported: " + this.layout + " -> " + layout);
				
				commandBuffer.pipelineBarrier(sourceStage, destinationStage, 0, null, null, barrier);
				
				this.layout = layout;
			}
		}
	}
	
	public TextureView getView()
	{
		return view;
	}
	
	public int getLength()
	{
		return length;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getDepth()
	{
		return depth;
	}
	
	public int getMipmapLevels()
	{
		return mipLevels;
	}
	
	public int getFormat()
	{
		return format;
	}
	
	public int getImageType()
	{
		return imageType;
	}
	
	public int getAspectMask()
	{
		return aspectMask;
	}
	
	public int getLayout()
	{
		return layout;
	}
	
	@Override
	public void close()
	{
		if (view != null) view.close();
		if (vk != null) VK10.vkDestroyImage(device.getVk(), vk, null);
		memory.close();
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("ImageView", view);
		Debug.print("Memory", memory);
	}
}
