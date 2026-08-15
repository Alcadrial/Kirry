package com.alcadrial.kirry.render.window.widget;

import java.io.IOException;
import java.nio.file.Path;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kirry.core.resources.ResourceManager;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.RenderingDataUtils;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.buffer.writer.DeferredBufferWriter;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.memory.MemoryTransferState;
import com.alcadrial.kirry.render.resources.TextureResource;
import com.alcadrial.kirry.render.texture.TextureBuffer;
import com.alcadrial.kirry.render.texture.TextureManager;
import com.alcadrial.kirry.render.texture.TextureRegion;
import com.alcadrial.kirry.render.util.Lazy;
import com.alcadrial.kirry.render.util.VLazy;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.AbstractWidget;
import com.alcadrial.kirry.render.window.EventRegistry;
import com.alcadrial.kirry.render.window.VulkanWindow;

public class Sprite extends AbstractWidget {
	
	public static Sprite ofFull(VulkanWindow window, String pathname) throws IOException
	{
		return ofWindowed(window, Vector3ir.of(), window.getSurface().getData().size(), pathname);
	}
	
	public static Sprite ofFull(VulkanWindow window, Path path) throws IOException
	{
		return ofWindowed(window, Vector3ir.of(), window.getSurface().getData().size(), path);
	}
	
	public static Sprite ofFull(VulkanWindow window, TextureResource texture)
	{
		return ofWindowed(window, Vector3ir.of(), window.getSurface().getData().size(), texture);
	}
	
	public static Sprite ofWindowed(VulkanWindow window, Vector3ir pos, Vector2ir size, String pathname) throws IOException
	{
		ResourceManager resourceManager = VulkanErrorUtils.nonNull(window.getResourceManager(), "Missing resource manager");
		
		return ofWindowed(window, pos, size, resourceManager.getResource(pathname, TextureResource.TYPE));
	}
	
	public static Sprite ofWindowed(VulkanWindow window, Vector3ir pos, Vector2ir size, Path path) throws IOException
	{
		ResourceManager resourceManager = VulkanErrorUtils.nonNull(window.getResourceManager(), "Missing resource manager");
		
		return ofWindowed(window, pos, size, resourceManager.getResource(path, TextureResource.TYPE));
	}
	
	public static Sprite ofWindowed(VulkanWindow window, Vector3ir pos, Vector2ir size, TextureResource texture)
	{
		return new Sprite(window, pos, size, texture);
	}
	
	private VLazy<VulkanBuffer> lazyVertexBuffer;
	private VLazy<VulkanBuffer> lazyIndexBuffer;
	private Lazy<TextureRegion> lazyTexture;
	private TextureResource textureResource;
	private int textureIndex;
	
	public Sprite(VulkanWindow window, Vector3ir pos, Vector2ir size, TextureResource texture)
	{
		super(window, pos, size);
		textureResource = texture;
		lazyVertexBuffer = new VLazy<>();
		lazyIndexBuffer = new VLazy<>();
		lazyTexture = new Lazy<>();
	}
	
	@Override
	public void initTextures(MemoryManager memoryManager, CommandBuffer commandBuffer, TextureManager textureManager) throws VulkanException
	{
		textureResource.load().join();
		TextureResource.Properties properties = textureResource.getProperties();
		try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, properties.size()))
		{
			VulkanBuffer buffer = transfer.getLocal(properties.size());
			textureResource.copy(buffer);
			lazyTexture.ifPresent(_ -> textureManager.removeTexture(textureResource));
			lazyTexture.set(textureManager.addToAtlas(TextureBuffer.create(buffer, properties.width(), properties.height(), textureResource)));
			textureIndex = textureManager.getAtlasSize() - 1;
		}
	}
	
	@Override
	public void initBuffers(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException
	{
		super.initBuffers(memoryManager, commandBuffer);
		
		try
		{
			TextureRegion texture = lazyTexture.get();
			lazyVertexBuffer.abortAndSet(memoryManager.allocate((5 * 4) * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT));
			lazyIndexBuffer.abortAndSet(memoryManager.allocate(6 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT));
			
			try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, lazyVertexBuffer.get(), lazyIndexBuffer.get()))
			{
				DeferredBufferWriter localVertexWriter = transfer.getWriter(lazyVertexBuffer.get());
				DeferredBufferWriter localIndexWriter = transfer.getWriter(lazyIndexBuffer.get());
				
				Vector2f uvStart = texture.getUvStart();
				Vector2f uvEnd = texture.getUvEnd();
				
				RenderingDataUtils.loadRectangleUv(localVertexWriter, localIndexWriter, 0, pos.x(), pos.y(), pos.x() + size.x(), pos.y() + size.y(), 1, uvStart.x(), uvStart.y(), uvEnd.x(), uvEnd.y());
			}
		}
		catch (VulkanException e)
		{
			lazyVertexBuffer.abort();
			lazyIndexBuffer.abort();
			throw e;
		}
	}
	
	@Override
	public void render(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer) throws VulkanException
	{
		
	}
	
	@Override
	public boolean isRendered()
	{
		return true;
	}
	
	@Override
	public void requestRender()
	{
		
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer, TextureManager textureManager) throws VulkanException
	{
		renderer.setBuffer(0, lazyVertexBuffer.get());
		renderer.setIndexBuffer(lazyIndexBuffer.get());
		renderer.draw(commandBuffer, textureManager.atlasIndexToGlobal(textureIndex));
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
		
	}
	
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void close() throws VulkanException
	{
		lazyVertexBuffer.close();
		lazyIndexBuffer.close();
	}
}