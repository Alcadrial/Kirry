package com.alcadrial.kirry.render.resources;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.lwjgl.stb.STBImage;

import com.alcadrial.kirry.core.resources.ResourceManager;
import com.alcadrial.kirry.core.resources.ResourceType;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.texture.Texture;
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
		int[] p = new int[2];
		data = Texture.loadRaw(path, p);
		properties = new Properties(p[0], p[1], data.limit());
	}
	
	public Properties getProperties()
	{
		return properties;
	}
	
	public void copy(VulkanBuffer buffer) throws VulkanException
	{
		ByteBuffer d = VulkanErrorUtils.nonNull(data);
		buffer.asBuffer().put(d);
		d.position(0);
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
