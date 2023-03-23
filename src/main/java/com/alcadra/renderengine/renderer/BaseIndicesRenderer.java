package com.alcadra.renderengine.renderer;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.shader.Shader;

public abstract class BaseIndicesRenderer<AB extends ArrayBuffer<?>> extends BaseRenderer<AB> implements IndicesRenderer {
	
	protected final ElementArrayBuffer elements;
	
	public BaseIndicesRenderer(Shader shader, VertexArray vertexArray, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		super(shader, vertexArray, arrayBuffer);
		elements = elementArrayBuffer;
	}
	
	@Override
	protected void drawIndices(int mode)
	{
		vertexArray.bind();
		syncBuffers();
		vertexArray.bindBuffer(elements);
		elements.sync();
		GL30.glDrawElements(mode, elements.size(), GL30.GL_UNSIGNED_INT, 0);
	}
	
	@Override
	public void clear()
	{
		super.clear();
		elements.clear();
	}
}
