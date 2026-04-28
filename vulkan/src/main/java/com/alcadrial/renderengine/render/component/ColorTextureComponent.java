package com.alcadrial.renderengine.render.component;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import com.alcadrial.renderengine.core.component.AbstractComponent;
import com.alcadrial.renderengine.core.component.ComponentType;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.texture.Atlas;
import com.alcadrial.renderengine.render.texture.TextureBuffer;
import com.alcadrial.renderengine.render.texture.TextureRegion;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class ColorTextureComponent extends AbstractComponent<ColorTextureComponent.Params, ColorTextureComponent.Properties> {
	
	public enum Type implements ComponentType<ColorTextureComponent> {
		
		INSTANCE;
		
		@Override
		public int getId()
		{
			return 1;
		}
	}
	
	public record Params(
		Atlas atlas
	) {}
	
	public record Properties(
		@Nullable TextureRegion region
	) {}
	
	public static ColorTextureComponent ofFile(Device device, File file)
	{
		return new ColorTextureComponent(new TextureLoader(device, file));
	}
	
	private Loader loader;
	private @Nullable TextureRegion region;
	
	public ColorTextureComponent(Loader loader)
	{
		this.loader = loader;
	}
	
	@Override
	public CompletableFuture<?> reload()
	{
		CompletableFuture<?> future = new CompletableFuture<>();
		RenderEngineVulkan.submitAsync(() -> {
			try
			{
				loader.load();
				future.complete(null);
				reloadCounter++;
			}
			catch (Throwable e)
			{
				future.completeExceptionally(e);
			}
			return null;
		});
		return future;
	}
	
	@Override
	public void init(Params params)
	{
		TextureBuffer buffer = loader.buffer;
		if (buffer == null) return;
		region = params.atlas.add(buffer);
		
		loader.buffer = null;
	}
	
	@Override
	public void reinit(Params params)
	{
		TextureBuffer buffer = loader.buffer;
		if (buffer == null) return;
		Atlas atlas = params.atlas;
		atlas.freeRegion((@NonNull TextureRegion) region);
		region = atlas.add(buffer);
		
		loader.buffer = null;
	}
	
	@Override
	public Properties getProperties()
	{
		return new Properties(region);
	}
	
	@Override
	public void close()
	{
		clear();
	}
	
	private void clear()
	{
		if (loader.buffer != null) loader.buffer.getBuffer().close();
		loader.buffer = null;
	}
	
	@Override
	public String toString()
	{
		return loader.toString();
	}
	
	private static abstract class Loader {
		
		@Nullable
		TextureBuffer buffer;
		
		public abstract void load() throws IOException, VulkanException;
	}
	
	private static class TextureLoader extends Loader {
		
		Device device;
		File file;
		
		public TextureLoader(Device device, File file)
		{
			this.device = device;
			this.file = file;
		}
		
		@Override
		public void load() throws IOException, VulkanException
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				int[] width = new int[1];
				int[] height = new int[1];
				int[] channels = new int[1];
				ByteBuffer data = STBImage.stbi_load(stack.UTF8(file.getPath()), width, height, channels, STBImage.STBI_rgb_alpha);
				if (data == null) throw new VulkanException("Cannot load image \"" + file + "\"");
				
				VulkanBuffer buffer = VulkanBuffer.forTransfer(device, data.limit());
				buffer.getMemory().mapMemory().getBuffer().put(data).rewind();
				buffer.getMemory().unmapMemory();
				STBImage.stbi_image_free(data.rewind());
				
				this.buffer = new TextureBuffer(buffer, width[0], height[0]);
			}
		}
	}
}
