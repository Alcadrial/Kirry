package com.alcadrial.renderengine.render.texture;

import java.io.File;
import java.nio.ByteBuffer;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.Memory;
import com.alcadrial.renderengine.render.buffer.MemoryResource;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanFuture;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class Texture extends MemoryResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static VulkanFuture<TextureBuffer> loadToBufferFromFile(Device device, File file) throws VulkanException
	{
		return RenderEngine.submitAsync(() -> {
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				int[] width = new int[1];
				int[] height = new int[1];
				int[] channels = new int[1];
				ByteBuffer data = STBImage.stbi_load(stack.UTF8(file.getPath()), width, height, channels, STBImage.STBI_rgb_alpha);
				if (data == null) throw new VulkanException("Cannot load image \"" + file + "\"");
				
				VulkanBuffer buffer = VulkanBuffer.forTransfer(device, data.limit());
				buffer.getMemory().mapMemory().getBuffer().put(data).rewind();
				STBImage.stbi_image_free(data.rewind());
				
				return new TextureBuffer(buffer, width[0], height[0]);
			}
		});
	}
	
	public static VulkanFuture<Texture> loadFromFile(Device device, File file, int dstLayout) throws VulkanException
	{
		return loadFromFile(device, file, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, dstLayout);
	}
	
	public static VulkanFuture<Texture> loadFromFile(Device device, File file, int usage, int dstLayout) throws VulkanException
	{
		return RenderEngine.submitAsync(() -> {
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				int[] width = new int[1];
				int[] height = new int[1];
				int[] channels = new int[1];
				ByteBuffer data = STBImage.stbi_load(stack.UTF8(file.getPath()), width, height, channels, STBImage.STBI_rgb_alpha);
				if (data == null) throw new VulkanException("Cannot load image \"" + file + "\"");
				
				VulkanBuffer buffer = VulkanBuffer.forTransfer(device, data.limit());
				buffer.getMemory().mapMemory().getBuffer().put(data).rewind();
				STBImage.stbi_image_free(data.rewind());
				
				Texture texture = Texture.create2dTexture(device, width[0], height[0], VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | usage, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
				
				CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
				texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				commandBuffer.copyBufferToImage(buffer, texture);
				texture.changeLayout(commandBuffer, dstLayout);
				commandBuffer.endRecording();
				device.submitSingleUseCommandBuffer(stack, commandBuffer).join();
				buffer.getMemory().unmapMemory();
				buffer.close();
				
				return texture;
			}
		});
	}
	
	public static Texture create1dTexture(Device device, int width, int format, int usage, int aspectMask) throws VulkanException
	{
		return new Texture(device, width, 1, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, 1, VK10.VK_IMAGE_TYPE_1D);
	}
	
	public static Texture create1dTexture(Device device, int width, int format, int usage, int aspectMask, int mipLevels) throws VulkanException
	{
		return new Texture(device, width, 1, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, mipLevels, VK10.VK_IMAGE_TYPE_1D);
	}
	
	public static Texture create2dTexture(Device device, int width, int height, int format, int usage, int aspectMask) throws VulkanException
	{
		return new Texture(device, width, height, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, 1, VK10.VK_IMAGE_TYPE_2D);
	}
	
	public static Texture create2dTexture(Device device, int width, int height, int format, int usage, int aspectMask, int mipLevels) throws VulkanException
	{
		return new Texture(device, width, height, 1, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, mipLevels, VK10.VK_IMAGE_TYPE_2D);
	}
	
	public static Texture create3dTexture(Device device, int width, int height, int depth, int format, int usage, int aspectMask) throws VulkanException
	{
		return new Texture(device, width, height, depth, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, 1, VK10.VK_IMAGE_TYPE_3D);
	}
	
	public static Texture create3dTexture(Device device, int width, int height, int depth, int format, int usage, int aspectMask, int mipLevels) throws VulkanException
	{
		return new Texture(device, width, height, depth, format, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, mipLevels, VK10.VK_IMAGE_TYPE_3D);
	}
	
	private TextureView view;
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
		length = width * height * depth;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.mipLevels = mipLevels;
		this.format = format;
		this.imageType = imageType;
		this.aspectMask = aspectMask;
		layout = VK10.VK_IMAGE_LAYOUT_UNDEFINED;
		
		long vk;
		Memory memory;
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
			
			memory = device.allocateTextureMemory(stack, p[0], VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
		}
		super(device, memory);
		this.vk = vk;
		
		view = new TextureView(this);
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	public VulkanFuture<?> changeLayout(int layout) throws VulkanException
	{
		if (this.layout != layout)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
				changeLayout(commandBuffer, layout);
				commandBuffer.endRecording();
				return device.submitSingleUseCommandBuffer(stack, commandBuffer);
			}
		}
		return RenderEngine.completedTask();
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
				else if (this.layout == VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL && layout == VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				{
					barrier.srcAccessMask(VK10.VK_ACCESS_TRANSFER_READ_BIT);
					barrier.dstAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
					
					sourceStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
					destinationStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
				}
				else if (this.layout == VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && layout == VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
				{
					barrier.srcAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
					barrier.dstAccessMask(VK10.VK_ACCESS_TRANSFER_READ_BIT);
					
					sourceStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
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
		if (view.getVk() != 0) view.close();
		if (vk != null) VK10.vkDestroyImage(device.getVk(), vk, null);
		if (memory.getVk() != 0) memory.close();
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("ImageView", view);
		Debug.print("Memory", memory);
	}
	
	@Override
	public String toString()
	{
		return "Texture{vk=" + Debug.address(vk) + ", size=" + width + (imageType >= VK10.VK_IMAGE_TYPE_2D ? "x" + height : "") + (imageType >= VK10.VK_IMAGE_TYPE_3D ? "x" + depth : "") + "}";
	}
}
