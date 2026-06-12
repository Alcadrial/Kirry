package com.alcadrial.kirry.render.renderer;

import java.util.Objects;

import org.lwjgl.opengl.GL11;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kirry.render.buffer.ElementArrayBuffer;
import com.alcadrial.kirry.render.buffer.GlBuffer;
import com.alcadrial.kirry.render.shader.ShaderProgram;

public abstract class AbstractIndicesRenderer<AB extends GlBuffer> extends AbstractShaderRenderer<AB> implements IndicesRenderer {
	
	protected ElementArrayBuffer elements;
	protected boolean useIndices;
	
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
		drawIndices(mode, -1);
	}
	
	protected void drawIndices(RenderingMode mode, int count)
	{
		drawIndices(mode, count, 0);
	}
	
	protected void drawIndices(RenderingMode mode, int count, int offset)
	{
		vertexArray.bind();
		syncBuffers();
		syncIndices();
		GL11.glDrawElements(mode.ordinal(), count <= -1 ? elements.getGpuSize() : count, GL11.GL_UNSIGNED_INT, offset);
	}
	
	@Override
	public void draw(RenderingMode mode, Matrix4f matrix)
	{
		if (useIndices) draw(mode, matrix, -1);
		else super.draw(mode, matrix);
	}
	
	@Override
	public void draw(RenderingMode mode, Matrix4f matrix, int count, int offset)
	{
		if (useIndices) drawIndices(mode, count, offset);
		else draw(mode, count, offset);
	}
	
	@Override
	public void syncIndices()
	{
		elements.bind();
		if (autoSync) elements.sync();
	}
	
	@Override
	public boolean useIndices()
	{
		return useIndices;
	}
	
	@Override
	public void setIndicesUse(boolean useIndices)
	{
		this.useIndices = useIndices;
	}
	
	@Override
	public void clear()
	{
		super.clear();
		elements.clear();
	}
}
