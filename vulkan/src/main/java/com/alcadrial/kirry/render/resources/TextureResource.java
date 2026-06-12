package com.alcadrial.kirry.render.resources;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.alcadrial.kirry.core.resources.ResourceManager;
import com.alcadrial.kirry.core.resources.ResourceType;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class TextureResource extends AbstractVulkanResource<TextureResource> {

	public static final ResourceType<TextureResource> TYPE = ResourceType.of(TextureResource.class);
	
	public record Properties(
		int width,
		int height,
		int size
	) {}
	
	public TextureResource(Path path, ResourceManager resourceManager)
	{
		super(path, resourceManager);
		properties = new Properties(0, 0, 0);
	}
	
	private @Nullable ByteBuffer data;
	private Properties properties;

	@Override
	protected void loadData(InputStreamSupplier stream) throws IOException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer widthHeightChannels = stack.mallocInt(3);
			long address = MemoryUtil.memAddress(widthHeightChannels);
			long dataAddress = STBImage.nstbi_load(MemoryUtil.memAddress(stack.UTF8(path.toRealPath().toString())), address, address + 4, address + 8, STBImage.STBI_rgb_alpha);
			if (dataAddress == 0) throw new IOException("Cannot load image \"" + path + "\"");
			int size = widthHeightChannels.get(0) * widthHeightChannels.get(1) * STBImage.STBI_rgb_alpha;
			data = MemoryUtil.memByteBuffer(dataAddress, size);
			properties = new Properties(widthHeightChannels.get(0), widthHeightChannels.get(1), size);
		}
	}
	
	public Properties getProperties()
	{
		return properties;
	}

	public void copy(VulkanBuffer buffer) throws VulkanException
	{
		buffer.asBuffer().put(VulkanErrorUtils.nonNull(data));
	}

	public void copy(VulkanBuffer buffer, int offset) throws VulkanException
	{
		buffer.asBuffer().position(offset).put(VulkanErrorUtils.nonNull(data));
	}
	
	@Override
	public void close()
	{
		super.close();
		if (data != null)
		{
			STBImage.stbi_image_free(data);
			data = null;
		}
	}
}
