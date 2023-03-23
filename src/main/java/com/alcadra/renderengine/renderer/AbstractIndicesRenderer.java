package com.alcadra.renderengine.renderer;

import java.util.Objects;

import org.lwjgl.opengl.GL11;

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.shader.ShaderProgram;

public abstract class AbstractIndicesRenderer<AB extends ArrayBuffer<?>> extends AbstractShaderRenderer<AB> implements IndicesRenderer {
	
	protected ElementArrayBuffer elements;
	
	protected AbstractIndicesRenderer(ShaderProgram shader, VertexArray vertexArray, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		super(shader, vertexArray, arrayBuffer);
		elements = Objects.requireNonNull(elementArrayBuffer);
	}
	
	protected AbstractIndicesRenderer(ShaderProgram shader, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		super(shader, arrayBuffer);
		elements = Objects.requireNonNull(elementArrayBuffer);
	}
	
	protected AbstractIndicesRenderer(ShaderProgram shader, VertexArray vertexArray, AB arrayBuffer)
	{
		super(shader, vertexArray, arrayBuffer);
		elements = new ElementArrayBuffer();
	}
	
	protected AbstractIndicesRenderer(ShaderProgram shader, AB arrayBuffer)
	{
		super(shader, arrayBuffer);
		elements = new ElementArrayBuffer();
	}
	
	protected void drawIndices(RenderingMode mode)
	{
		drawIndices(mode, elements.size());
	}
	
	protected void drawIndices(RenderingMode mode, int count)
	{
		vertexArray.bind();
		syncBuffers();
		elements.bind();
		elements.sync();
		GL11.glDrawElements(mode.ordinal(), count, GL11.GL_UNSIGNED_INT, 0);
		arrayBuffer.unbind();
		elements.unbind();
	}
	
	@Override
	public void clear()
	{
		super.clear();
		elements.clear();
	}
}
