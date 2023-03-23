package com.alcadra.renderengine.renderer;

import java.util.Objects;

import org.lwjgl.opengl.GL11;

import com.alcadra.renderengine.buffer.ArrayBufferUpdate;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@UpdatedFrom("BaseIndicesRenderer")
public abstract class AbstractIndicesRendererUpdate<AB extends ArrayBufferUpdate<?>> extends AbstractShaderRendererUpdate<AB> implements IndicesRendererUpdate {
	
	protected final ElementArrayBuffer elements;
	
	protected AbstractIndicesRendererUpdate(ShaderProgram shader, VertexArray vertexArray, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		super(shader, vertexArray, arrayBuffer);
		elements = Objects.requireNonNull(elementArrayBuffer);
	}
	
	protected AbstractIndicesRendererUpdate(ShaderProgram shader, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		super(shader, arrayBuffer);
		elements = Objects.requireNonNull(elementArrayBuffer);
	}
	
	protected AbstractIndicesRendererUpdate(ShaderProgram shader, VertexArray vertexArray, AB arrayBuffer)
	{
		super(shader, vertexArray, arrayBuffer);
		elements = new ElementArrayBuffer();
	}
	
	protected AbstractIndicesRendererUpdate(ShaderProgram shader, AB arrayBuffer)
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
		vertexArray.bindBuffer(elements);
		elements.sync();
		GL11.glDrawElements(mode.ordinal(), count, GL11.GL_UNSIGNED_INT, 0);
	}
	
	@Override
	public void clear()
	{
		super.clear();
		elements.clear();
	}
}
