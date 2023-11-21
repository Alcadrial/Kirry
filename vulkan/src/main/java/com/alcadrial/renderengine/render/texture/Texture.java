package com.alcadrial.renderengine.render.texture;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.util.LongDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class Texture extends LongDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static Texture create1dTexture(Device device, int width, int format, int usage)
	{
		return new Texture(device, width, 1, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, 1, VK10.VK_IMAGE_TYPE_1D);
	}
	
	public static Texture create1dTexture(Device device, int width, int format, int usage, int mipLevels)
	{
		return new Texture(device, width, 1, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, mipLevels, VK10.VK_IMAGE_TYPE_1D);
	}
	
	public static Texture create2dTexture(Device device, int width, int height, int format, int usage)
	{
		return new Texture(device, width, height, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, 1, VK10.VK_IMAGE_TYPE_2D);
	}
	
	public static Texture create2dTexture(Device device, int width, int height, int format, int usage, int mipLevels)
	{
		return new Texture(device, width, height, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, mipLevels, VK10.VK_IMAGE_TYPE_2D);
	}
	
	public static Texture create3dTexture(Device device, int width, int height, int depth, int format, int usage)
	{
		return new Texture(device, width, height, depth, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, 1, VK10.VK_IMAGE_TYPE_3D);
	}
	
	public static Texture create3dTexture(Device device, int width, int height, int depth, int format, int usage, int mipLevels)
	{
		return new Texture(device, width, height, depth, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, mipLevels, VK10.VK_IMAGE_TYPE_3D);
	}
	
	private VulkanBuffer buffer;
	private TextureView view;
	private long memory;
	private int length;
	private int width;
	private int height;
	private int depth;
	private int mipLevels;
	private int format;
	private int imageType;
	private int layout;
	
	private Texture(Device device, int width, int height, int depth, int format, int tiling, int usage, int mipLevels, int imageType)
	{
		super(device);
		length = width * height * depth;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.mipLevels = mipLevels;
		this.format = format;
		this.imageType = imageType;
		layout = VK10.VK_IMAGE_LAYOUT_UNDEFINED;
		
		buffer = new VulkanBuffer(device, length * 4, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkImageCreateInfo createInfo = VkImageCreateInfo.calloc(stack);
			createInfo.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
			createInfo.imageType(imageType);
			createInfo.extent().width(width).height(height).depth(depth);
			createInfo.mipLevels(mipLevels);
			createInfo.arrayLayers(1);
			createInfo.format(format);
			createInfo.tiling(tiling);
			createInfo.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			createInfo.usage(usage);
			createInfo.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
			createInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateImage(device.getVk(), createInfo, null, p), "Failed to create image");
			vk = p[0];
			
			memory = device.allocateImageMemory(stack, vk, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
		}
		
		view = new TextureView(this);
	}
	
	public long getMemory()
	{
		return memory;
	}
	
	public void sendToPhysicalDevice()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			sendToPhysicalDevice(commandBuffer);
			device.submitSingleUseCommandBuffer(stack, commandBuffer);
		}
	}
	
	public void sendToPhysicalDevice(VkCommandBuffer commandBuffer)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkBufferImageCopy.Buffer region = VkBufferImageCopy.malloc(1, stack);
			region.bufferOffset(0);
			region.bufferRowLength(0);
			region.bufferImageHeight(0);
			region.imageSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			region.imageSubresource().mipLevel(0);
			region.imageSubresource().baseArrayLayer(0);
			region.imageSubresource().layerCount(1);
			region.imageOffset().set(0, 0, 0);
			region.imageExtent().set(width, height, depth);
			
			VK10.vkCmdCopyBufferToImage(commandBuffer, buffer.getVk(), vk, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);
		}
	}
	
	public void changeLayout(int layout)
	{
		if (this.layout != layout)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkCommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
				changeLayout(commandBuffer, layout);
				device.submitSingleUseCommandBuffer(stack, commandBuffer);
			}
		}
	}
	
	/**
	 * Layout is updated instantly, without waiting the execution of the command buffer
	 */
	public void changeLayout(VkCommandBuffer commandBuffer, int layout)
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
				barrier.subresourceRange().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
				barrier.subresourceRange().baseMipLevel(0);
				barrier.subresourceRange().levelCount(1);
				barrier.subresourceRange().baseArrayLayer(0);
				barrier.subresourceRange().layerCount(1);
				
				int sourceStage = 0;
				int destinationStage = 0;
				
				if (this.layout == VK10.VK_IMAGE_LAYOUT_UNDEFINED && layout == VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				{
					barrier.srcAccessMask(VK13.VK_ACCESS_NONE);
					barrier.dstAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
					
					sourceStage = VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
					destinationStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
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
				else VulkanErrorUtils.error("Unsupported layout change (" + this.layout + " -> " + layout + ")");
				
				VK10.vkCmdPipelineBarrier(commandBuffer, sourceStage, destinationStage, 0, null, null, barrier);
				
				this.layout = layout;
			}
		}
	}
	
	public VulkanBuffer getBuffer()
	{
		return buffer;
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
	
	public int getLayout()
	{
		return layout;
	}
	
	@Override
	public void close()
	{
		view.close();
		VK10.vkDestroyImage(device.getVk(), vk, null);
		VK10.vkFreeMemory(device.getVk(), memory, null);
		buffer.close();
		super.close();
	}
}
