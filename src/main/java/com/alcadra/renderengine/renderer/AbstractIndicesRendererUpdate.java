package com.alcadra.renderengine.renderer;

import java.util.Objects;

import org.lwjgl.opengl.GL11;

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@UpdatedFrom("BaseIndicesRenderer")
public abstract class AbstractIndicesRendererUpdate<AB extends ArrayBuffer<?>> extends AbstractShaderRendererUpdate<AB> implements IndicesRenderer {
	
	protected final ElementArrayBuffer elements;
	
	public AbstractIndicesRendererUpdate(ShaderProgram shader, VertexArray vertexArray, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		super(shader, vertexArray, arrayBuffer);
		elements = Objects.requireNonNull(elementArrayBuffer);
	}
	
	public AbstractIndicesRendererUpdate(ShaderProgram shader, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		super(shader, arrayBuffer);
		elements = Objects.requireNonNull(elementArrayBuffer);
	}
	
	public AbstractIndicesRendererUpdate(ShaderProgram shader, VertexArray vertexArray, AB arrayBuffer)
	{
		super(shader, vertexArray, arrayBuffer);
		elements = new ElementArrayBuffer();
	}
	
	public AbstractIndicesRendererUpdate(ShaderProgram shader, AB arrayBuffer)
	{
		super(shader, arrayBuffer);
		elements = new ElementArrayBuffer();
	}
	
	protected void drawIndices(int mode)
	{
		drawIndices(mode, elements.size());
	}
	
	protected void drawIndices(int mode, int count)
	{
		vertexArray.bind();
		syncBuffers();
		vertexArray.bindBuffer(elements);
		elements.sync();
		GL11.glDrawElements(mode, count, GL11.GL_UNSIGNED_INT, 0);
	}
	
	@Override
	public void clear()
	{
		super.clear();
		elements.clear();
	}
}
