package com.alcadrial.renderengine.render.window.widget;

import org.jspecify.annotations.NonNull;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.buffer.AbstractBigVulkanBuffer.PhysicalDeviceTransferState;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.texture.TextureRegion;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.AbstractWidget;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class Sprite extends AbstractWidget {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private VulkanBuffer vertexBuffer;
	private VulkanBuffer indexBuffer;
	private TextureRegion texture;
	
	public Sprite(VulkanWindow window, Vector3i pos, Vector2i size, TextureRegion texture)
	{
		super(window, pos, size);
		this.texture = texture;
	}
	
	
	
	@Override
	public void initializeRendering(StaticBigBuffer staticBigBuffer) throws VulkanException
	{
		super.initializeRendering(staticBigBuffer);
		vertexBuffer = staticBigBuffer.allocate((3 + 4) * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		indexBuffer = staticBigBuffer.allocate(6 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		
		try (PhysicalDeviceTransferState transfer = staticBigBuffer.startLocalBuffering())
		{
			BufferWriter localVertexWriter = transfer.beginTransfer(vertexBuffer);
			BufferWriter localIndexWriter = transfer.beginTransfer(indexBuffer);
			
			Vector3f pos = Vector3f.of(this.pos);
			Vector2f end = Vector2f.of(this.pos.add(size).xy());
			
			//RenderingDataUtils.loadRectangleUv(localVertexWriter, localIndexWriter, 0, pos.x(), pos.y(), end.x(), end.y(), 0, texture.getUvStart().x(), texture.getUvStart().y(), texture.getUvEnd().x(), texture.getUvEnd().y());
		}
	}
	
	@Override
	public void render(@NonNull Matrix4f matrix, @NonNull Renderer renderer)
	{
		super.render(matrix, renderer);
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer, float partialFrame) throws VulkanException
	{
		renderer.setBuffer(0, vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
		renderer.draw(commandBuffer);
	}
}