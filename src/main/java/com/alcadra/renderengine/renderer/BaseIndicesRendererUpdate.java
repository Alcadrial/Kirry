package com.alcadra.renderengine.renderer;

import java.util.Objects;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@UpdatedFrom("BaseIndicesRenderer")
public abstract class BaseIndicesRendererUpdate<AB extends ArrayBuffer<?>> extends BaseRendererUpdate<AB> implements IndicesRenderer {
	
	protected final ElementArrayBuffer elements;
	
	public BaseIndicesRendererUpdate(ShaderProgram shader, VertexArray vertexArray, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		super(shader, vertexArray, arrayBuffer);
		elements = Objects.requireNonNull(elementArrayBuffer);
	}
	
	public BaseIndicesRendererUpdate(ShaderProgram shader, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		super(shader, arrayBuffer);
		elements = Objects.requireNonNull(elementArrayBuffer);
	}
	
	public BaseIndicesRendererUpdate(ShaderProgram shader, VertexArray vertexArray, AB arrayBuffer)
	{
		super(shader, vertexArray, arrayBuffer);
		elements = new ElementArrayBuffer();
	}
	
	public BaseIndicesRendererUpdate(ShaderProgram shader, AB arrayBuffer)
	{
		super(shader, arrayBuffer);
		elements = new ElementArrayBuffer();
	}
	
	protected void drawIndices(int mode)
	{
		draw(mode, elements.size());
	}
	
	protected void drawIndices(int mode, int count)
	{
		vertexArray.bind();
		syncBuffers();
		vertexArray.bindBuffer(elements);
		elements.sync();
		GL30.glDrawElements(mode, count, GL30.GL_UNSIGNED_INT, 0);
	}
	
	@Override
	public void clear()
	{
		super.clear();
		elements.clear();
	}
}
