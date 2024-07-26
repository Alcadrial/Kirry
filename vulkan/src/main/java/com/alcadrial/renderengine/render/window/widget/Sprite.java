package com.alcadrial.renderengine.render.window.widget;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.test.RenderingDataUtils;
import com.alcadrial.renderengine.render.texture.TextureRegion;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class Sprite extends AbstractWidget {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private VertexBuffer buffer;
	private IndexBuffer indexBuffer;
	private TextureRegion texture;
	
	public Sprite(VulkanWindow window, Vector3i pos, Vector2i size, TextureRegion texture)
	{
		super(window, pos, size);
		this.texture = texture;
	}
	
	@Override
	public void initializeRendering()
	{
		super.initializeRendering();
		indexBuffer = new IndexBuffer(window.getDevice(), 6);
		buffer = new VertexBuffer(window.getDevice(), 3 * 4 + 4 * 4);
	}
	
	@Override
	public void render(Matrix4f matrix, Renderer renderer)
	{
		super.render(matrix, renderer);
		
		Vector3f pos = matrix.multiply(Vector4f.of(this.pos).xyz1()).xyz();
		Vector2f end = matrix.multiply(Vector4f.of(this.pos.add(size)).xyz1()).xy();
		
		RenderingDataUtils.loadRectangleUv(buffer.asFloatBuffer(), indexBuffer.asIntBuffer(), 0, pos.x(), pos.y(), end.x(), end.y(), 0, texture.getUvStart().x(), texture.getUvStart().y(), texture.getUvEnd().x(), texture.getUvEnd().y());
		
		buffer.sendToPhysicalDevice();
		indexBuffer.sendToPhysicalDevice();
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer, float partialFrame)
	{
		renderer.updateUniformBuffers((b, l) -> {
			l.set("TRANSFORMATION", b, new Matrix4f());
			l.set("TEXTURE_SAMPLER", texture.getTexture().getView(), new TextureSampler(buffer.getDevice()));
		});
		renderer.resetVertexBuffer(0);
		renderer.addVertexBuffer(0, buffer);
		renderer.setIndexBuffer(indexBuffer);
		renderer.draw(commandBuffer);
	}
}