package com.alcadra.renderengine.renderer;

import java.util.Objects;

import com.alcadra.math.Matrix4f;
import com.alcadra.renderengine.buffer.ArrayBuffer;

public abstract class AbstractRenderer<AB extends ArrayBuffer<?>> implements Renderer {
	
	protected final VertexArray vertexArray;
	protected final AB arrayBuffer;
	
	protected AbstractRenderer(AB arrayBuffer)
	{
		this.vertexArray = new VertexArray();
		this.arrayBuffer = Objects.requireNonNull(arrayBuffer);
	}
	
	protected AbstractRenderer(VertexArray vertexArray, AB arrayBuffer)
	{
		this.vertexArray = Objects.requireNonNull(vertexArray);
		this.arrayBuffer = Objects.requireNonNull(arrayBuffer);
	}
	
	@Override
	public void drawVertices(RenderingMode mode, Matrix4f matrix, int count)
	{
		drawVertices(mode, matrix, count, 0);
	}
}
