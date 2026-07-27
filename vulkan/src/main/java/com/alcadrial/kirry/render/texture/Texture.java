package com.alcadrial.kirry.render.texture;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import org.jspecify.annotations.NonNull;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;

import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.buffer.Memory;
import com.alcadrial.kirry.render.buffer.Memory.Mapping;
import com.alcadrial.kirry.render.buffer.MemoryResource;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.future.VulkanFuture;

public class Texture extends MemoryResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public static VulkanFuture<TextureBuffer> loadToBufferFromFile(Device device, Path path) throws VulkanException
	{
		return VulkanFuture.submitAsync(() -> {
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				IntBuffer widthHeightChannels = stack.mallocInt(3);
				long address = MemoryUtil.memAddress(widthHeightChannels);
				long dataAddress = STBImage.nstbi_load(MemoryUtil.memAddress(stack.UTF8(path.toRealPath().toString())), address, address + 4, address + 8, STBImage.STBI_rgb_alpha);
				if (dataAddress == 0) throw new VulkanException("Cannot load image \"" + path + "\"");
				ByteBuffer data = MemoryUtil.memByteBuffer(dataAddress, widthHeightChannels.get(0) * widthHeightChannels.get(1) * STBImage.STBI_rgb_alpha);
				
				VulkanBuffer buffer = VulkanBuffer.forTransfer(device, data.limit());
				((@NonNull Mapping) buffer.getMemory().getMapping()).getBuffer().put(data).rewind();
				STBImage.stbi_image_free(data.rewind());
				
				return TextureBuffer.create(buffer, widthHeightChannels.get(0), widthHeightChannels.get(1), path);
			}
		});
	}
	
	public static VulkanFuture<Texture> loadFromFile(Device device, Path path, int dstLayout) throws VulkanException
	{
		return loadFromFile(device, path, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, dstLayout);
	}
	
	public static VulkanFuture<Texture> loadFromFile(Device device, Path path, int usage, int dstLayout) throws VulkanException
	{
		return VulkanFuture.submitAsync(() -> {
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				IntBuffer widthHeightChannels = stack.mallocInt(3);
				long address = MemoryUtil.memAddress(widthHeightChannels);
				long dataAddress = STBImage.nstbi_load(MemoryUtil.memAddress(stack.UTF8(path.toRealPath().toString())), address, address + 4, address + 8, STBImage.STBI_rgb_alpha);
				if (dataAddress == 0) throw new VulkanException("Cannot load image \"" + path + "\"");
				ByteBuffer data = MemoryUtil.memByteBuffer(dataAddress, widthHeightChannels.get(0) * widthHeightChannels.get(1) * STBImage.STBI_rgb_alpha);
				
				VulkanBuffer buffer = VulkanBuffer.forTransfer(device, data.limit());
				((@NonNull Mapping) buffer.getMemory().getMapping()).getBuffer().put(data).rewind();
				STBImage.stbi_image_free(data.rewind());
				
				Texture texture = Texture.create2dTexture(device, widthHeightChannels.get(0), widthHeightChannels.get(1), VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | usage, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
				
				device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
					texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
					commandBuffer.copyBufferToImage(buffer, texture);
					texture.changeLayout(commandBuffer, dstLayout);
				}).join();
				buffer.close();
				
				return texture;
			}
		});
	}
	
	public static VulkanFuture<?> saveToPngFile(Texture texture, Path path)
	{
		return VulkanFuture.submitAsync(() -> {
			boolean result = STBImageWrite.stbi_write_png(path.toString(), texture.width, texture.height, 4, texture.memory.getMapping().getBuffer(), 0);
			if (!result) throw new VulkanException("Failed to write image to \"" + path + "\"");
		});
	}
	
	public static Texture create1dTexture(Device device, int width, int format, int usage, int aspectMask) throws VulkanException
	{
		return create1dTexture(device, width, format, usage, aspectMask, 1, 1);
	}
	
	public static Texture create1dTexture(Device device, int width, int format, int usage, int aspectMask, int samples, int mipLevels) throws VulkanException
	{
		return new Texture(device, width, 1, 1, format, samples, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, mipLevels, VK10.VK_IMAGE_TYPE_1D);
	}
	
	public static Texture create2dTexture(Device device, int width, int height, int format, int usage, int aspectMask) throws VulkanException
	{
		return create2dTexture(device, width, height, format, usage, aspectMask, 1, 1);
	}
	
	public static Texture create2dTexture(Device device, int width, int height, int format, int usage, int aspectMask, int samples, int mipLevels) throws VulkanException
	{
		return new Texture(device, width, height, 1, format, samples, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, mipLevels, VK10.VK_IMAGE_TYPE_2D);
	}
	
	public static Texture create3dTexture(Device device, int width, int height, int depth, int format, int usage, int aspectMask) throws VulkanException
	{
		return create3dTexture(device, width, height, depth, format, usage, aspectMask, 1, 1);
	}
	
	public static Texture create3dTexture(Device device, int width, int height, int depth, int format, int usage, int aspectMask, int samples, int mipLevels) throws VulkanException
	{
		return new Texture(device, width, height, depth, format, samples, VK10.VK_IMAGE_TILING_OPTIMAL, usage, aspectMask, mipLevels, VK10.VK_IMAGE_TYPE_3D);
	}
	
	private TextureView view;
	private int length;
	private int width;
	private int height;
	private int depth;
	private int format;
	private int samples;
	private int mipLevels;
	private int imageType;
	private int aspectMask;
	private int layout;
	private int mipLayout;
	
	private Texture(Device device, int width, int height, int depth, int format, int samples, int tiling, int usage, int aspectMask, int mipLevels, int imageType) throws VulkanException
	{
		length = width * height * depth;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.format = format;
		this.samples = samples;
		this.mipLevels = mipLevels;
		this.imageType = imageType;
		this.aspectMask = aspectMask;
		layout = VK10.VK_IMAGE_LAYOUT_UNDEFINED;
		mipLayout = VK10.VK_IMAGE_LAYOUT_UNDEFINED;
		
		if (mipLevels > 1) usage |= VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
		
		long vk;
		Memory memory;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateImage(device.getVk(), VkImageCreateInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.imageType(imageType)
				.format(format)
				.extent(e -> e.set(width, height, depth))
				.mipLevels(mipLevels)
				.arrayLayers(1)
				.samples(samples)
				.tiling(tiling)
				.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
				.usage(usage)
				.pQueueFamilyIndices(null)
				.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED), null, p), "Failed to create image");
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
		view.setName(name + " View");
		this.name = name;
	}
	
	public VulkanFuture<?> changeLayout(int layout) throws VulkanException
	{
		if (this.layout != layout)
		{
			return device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> changeLayout(commandBuffer, layout));
		}
		return VulkanFuture.completedTask();
	}
	
	public void changeLayout(CommandBuffer commandBuffer, int layout)
	{
		changeLayout(commandBuffer, layout, 0, 1);
	}
	
	public void changeMipLayout(CommandBuffer commandBuffer, int layout)
	{
		changeLayout(commandBuffer, layout, 1, mipLevels);
	}
	
	private void changeLayout(CommandBuffer commandBuffer, int layout, int baseMipLevel, int levelCount)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.malloc(1, stack)
				.sType$Default()
				.pNext(0)
				.oldLayout(this.layout)
				.newLayout(layout)
				.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
				.image(vk)
				.subresourceRange(r -> r
					.aspectMask(aspectMask)
					.baseMipLevel(baseMipLevel)
					.levelCount(levelCount)
					.baseArrayLayer(0)
					.layerCount(1)
				);
			changeLayout(commandBuffer, barrier);
			this.layout = layout;
		}
	}
	
	private void changeLayout(CommandBuffer commandBuffer, VkImageMemoryBarrier.Buffer barrier)
	{
		int sourceStage = 0;
		int destinationStage = 0;
		
		switch (barrier.oldLayout())
		{
			case VK10.VK_IMAGE_LAYOUT_UNDEFINED -> {
				barrier.srcAccessMask(VK13.VK_ACCESS_NONE);
				sourceStage = VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
			}
			case VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL -> {
				barrier.srcAccessMask(VK10.VK_ACCESS_TRANSFER_READ_BIT);
				sourceStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
			}
			case VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL -> {
				barrier.srcAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
				sourceStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
			}
			case VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL -> {
				barrier.srcAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT);
				sourceStage = VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
			}
			default -> throw new UnsupportedOperationException("Transition not supported: " + barrier.oldLayout() + " -> " + barrier.newLayout());
		}
		switch (barrier.newLayout())
		{
			case VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL -> {
				barrier.dstAccessMask(VK10.VK_ACCESS_TRANSFER_READ_BIT);
				destinationStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
			}
			case VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL -> {
				barrier.dstAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
				destinationStage = VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
			}
			case VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL -> {
				barrier.dstAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT);
				destinationStage = VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
			}
			case VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL -> {
				barrier.dstAccessMask(VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
				destinationStage = VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
			}
			default -> throw new UnsupportedOperationException("Transition not supported: " + barrier.oldLayout() + " -> " + barrier.newLayout());
		}
		
		commandBuffer.pipelineBarrier(sourceStage, destinationStage, 0, null, null, barrier);
	}
	
	public void generateMipmaps(CommandBuffer commandBuffer)
	{
		generateMipmaps(commandBuffer, layout);
	}
	
	public void generateMipmaps(CommandBuffer commandBuffer, int targetLayout)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkImageBlit.Buffer regions = VkImageBlit.malloc(1, stack)
				.srcSubresource(r -> r
					.aspectMask(aspectMask)
					.baseArrayLayer(0)
					.layerCount(1)
				)
				.srcOffsets(os -> os
					.apply(o -> o.x(0).y(0).z(0))
					.apply(o -> o.z(1))
					.rewind()
				)
				.dstSubresource(r -> r
					.aspectMask(aspectMask)
					.baseArrayLayer(0)
					.layerCount(1)
				)
				.dstOffsets(os -> os
					.apply(o -> o.x(0).y(0).z(0))
					.apply(o -> o.z(1))
					.rewind()
				);
			VkImageMemoryBarrier.Buffer barriers = VkImageMemoryBarrier.malloc(1, stack)
				.sType$Default()
				.pNext(0)
				.srcAccessMask(VK10.VK_ACCESS_TRANSFER_READ_BIT)
				.dstAccessMask(VK10.VK_ACCESS_TRANSFER_WRITE_BIT)
				.oldLayout(layout)
				.newLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
				.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
				.image(vk)
				.subresourceRange(r -> r
					.aspectMask(aspectMask)
					.baseMipLevel(0)
					.levelCount(1)
					.baseArrayLayer(0)
					.layerCount(1)
				);
			
			if (layout != VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
			{
				changeLayout(commandBuffer, barriers);
			}
			if (mipLayout != VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
			{
				barriers.subresourceRange().baseMipLevel(1);
				barriers.subresourceRange().levelCount(mipLevels - 1);
				barriers.oldLayout(mipLayout);
				barriers.newLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				changeLayout(commandBuffer, barriers);
			}
			
			barriers.oldLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			barriers.newLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
			
			int[] data = {width, height, width / 2, height / 2};
			for (int i = 0; i < mipLevels - 2; i++)
			{
				int j = i;
				regions
					.srcSubresource(r -> r.mipLevel(j))
					.srcOffsets(os -> os.apply(1, o -> o.x(data[0]).y(data[1])))
					.dstSubresource(r -> r.mipLevel(j + 1))
					.dstOffsets(os -> os.apply(1, o -> o.x(data[2]).y(data[3])));
				commandBuffer.blitImage(this, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, this, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, regions, VK10.VK_FILTER_LINEAR);
				data[0] = data[2];
				data[1] = data[3];
				if (data[2] > 1) data[2] /= 2;
				if (data[3] > 1) data[3] /= 2;
				barriers.subresourceRange().baseMipLevel(j + 1);
				barriers.subresourceRange().levelCount(1);
				commandBuffer.pipelineBarrier(VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, 0, null, null, barriers);
			}
			regions
				.srcSubresource(r -> r.mipLevel(mipLevels - 2))
				.srcOffsets(os -> os.apply(1, o -> o.x(data[0]).y(data[1])))
				.dstSubresource(r -> r.mipLevel(mipLevels - 1))
				.dstOffsets(os -> os.apply(1, o -> o.x(data[2]).y(data[3])));
			commandBuffer.blitImage(this, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, this, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, regions, VK10.VK_FILTER_LINEAR);
			
			barriers.oldLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
			barriers.newLayout(targetLayout);
			barriers.subresourceRange().baseMipLevel(0);
			barriers.subresourceRange().levelCount(mipLevels - 1);
			changeLayout(commandBuffer, barriers);
			barriers.oldLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			barriers.subresourceRange().baseMipLevel(mipLevels - 1);
			barriers.subresourceRange().levelCount(1);
			changeLayout(commandBuffer, barriers);
			mipLayout = layout = targetLayout;
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
	
	public int getFormat()
	{
		return format;
	}
	
	public int getSamples()
	{
		return samples;
	}
	
	public int getMipLevels()
	{
		return mipLevels;
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
