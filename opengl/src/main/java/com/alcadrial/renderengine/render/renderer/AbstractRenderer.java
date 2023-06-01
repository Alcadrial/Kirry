package com.alcadrial.renderengine.render.renderer;

import java.util.Objects;

import org.lwjgl.opengl.GL30;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.renderengine.render.buffer.GlBuffer;

public abstract class AbstractRenderer<AB extends GlBuffer> implements Renderer {
	
	protected final VertexArray vertexArray;
	protected final AB arrayBuffer;
	protected boolean autoSync;
	
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
	
	protected void draw(RenderingMode mode, int count)
	{
		draw(mode, count, 0);
	}
	
	protected void draw(RenderingMode mode, int count, int offset)
	{
		vertexArray.bind();
		syncBuffers();
		GL30.glDrawArrays(mode.ordinal(), offset, count);
	}
	
	@Override
	public void draw(RenderingMode mode, Matrix4f matrix, int count)
	{
		draw(mode, matrix, count, 0);
	}
	
	@Override
	public void syncBuffers()
	{
		arrayBuffer.bind();
		if (autoSync) arrayBuffer.sync();
	}
	
	@Override
	public void setAutoSync(boolean autoSync)
	{
		this.autoSync = autoSync;
	}
	
	@Override
	public void clear()
	{
		arrayBuffer.clear();
	}
}
