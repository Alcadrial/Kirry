package com.alcadrial.renderengine.opengl.renderer;

import java.util.Objects;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.renderengine.opengl.buffer.GlBuffer;

public abstract class AbstractRenderer<AB extends GlBuffer> implements Renderer {
	
	protected final VertexArray vertexArray;
	protected final AB arrayBuffer;
	
	protected AbstractRenderer(AB arrayBuffer)
	{
		vertexArray = new VertexArray();
		this.arrayBuffer = Objects.requireNonNull(arrayBuffer);
	}
	
	protected AbstractRenderer(VertexArray vertexArray, AB arrayBuffer)
	{
		this.vertexArray = Objects.requireNonNull(vertexArray);
		this.arrayBuffer = Objects.requireNonNull(arrayBuffer);
	}
	
	@Override
	public void draw(RenderingMode mode, Matrix4f matrix, int count)
	{
		draw(mode, matrix, count, 0);
	}
}
