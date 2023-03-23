package com.alcadrial.renderengine.renderer;

import java.util.Objects;

import com.alcadra.math.matrix.Matrix4f;
import com.alcadrial.renderengine.buffer.ArrayBuffer;

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
	public void draw(RenderingMode mode, Matrix4f matrix, int count)
	{
		draw(mode, matrix, count, 0);
	}
}
