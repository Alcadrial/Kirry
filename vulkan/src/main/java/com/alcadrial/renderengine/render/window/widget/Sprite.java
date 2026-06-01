package com.alcadrial.renderengine.render.window.widget;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.RenderingDataUtils;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.buffer.writer.DeferredBufferWriter;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.memory.MemoryManager;
import com.alcadrial.renderengine.render.memory.MemoryTransferState;
import com.alcadrial.renderengine.render.texture.Atlas;
import com.alcadrial.renderengine.render.texture.TextureBuffer;
import com.alcadrial.renderengine.render.texture.TextureRegion;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.AbstractWidget;
import com.alcadrial.renderengine.render.window.EventRegistry;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class Sprite extends AbstractWidget {
	
	private VulkanBuffer vertexBuffer;
	private VulkanBuffer indexBuffer;
	private TextureBuffer textureBuffer;
	private TextureRegion texture;
	
	public Sprite(VulkanWindow window, Vector3i pos, Vector2i size, TextureBuffer texture)
	{
		super(window, pos, size);
		this.textureBuffer = texture;
	}
	
	@Override
	public void initAtlas(Atlas atlas)
	{
		texture = atlas.add(textureBuffer);
	}
	
	@Override
	public void initBuffers(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException
	{
		super.initBuffers(memoryManager, commandBuffer);
		vertexBuffer = memoryManager.allocate((5 * 4) * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		indexBuffer = memoryManager.allocate(6 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		
		try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, vertexBuffer, indexBuffer))
		{
			DeferredBufferWriter localVertexWriter = transfer.getWriter(vertexBuffer);
			DeferredBufferWriter localIndexWriter = transfer.getWriter(indexBuffer);
			
			Vector2f end = Vector2f.of(pos.xy().add(size));
			Vector2f uvStart = texture.getUvStart();
			Vector2f uvEnd = texture.getUvEnd();
			
			RenderingDataUtils.loadRectangleUv(localVertexWriter, localIndexWriter, 0, pos.x(), pos.y(), end.x(), end.y(), 1, uvStart.x(), uvStart.y(), uvEnd.x(), uvEnd.y());
		}
	}
	
	@Override
	public void render(Matrix4f matrix, Renderer renderer)
	{
		super.render(matrix, renderer);
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer, float partialFrame) throws VulkanException
	{
		renderer.setBuffer(0, vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
		
	}
	
	@Override
	public void clearRenderingData()
	{
		throw new UnsupportedOperationException();
	}
}