package com.alcadra.renderengine.renderer;

import java.util.Objects;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.util.Nonnull;
import com.alcadra.renderengine.util.Util;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@UpdatedFrom("BaseRenderer")
@Nonnull
public abstract class AbstractShaderRendererUpdate<AB extends ArrayBuffer<?>> implements ShaderRendererUpdate {
	
	protected final VertexArray vertexArray;
	protected final AB arrayBuffer;
	protected ShaderProgram program;
	
	public AbstractShaderRendererUpdate(ShaderProgram program, AB arrayBuffer)
	{
		this.program = Objects.requireNonNull(program);
		this.vertexArray = new VertexArray();
		this.arrayBuffer = Objects.requireNonNull(arrayBuffer);
	}
	
	public AbstractShaderRendererUpdate(ShaderProgram program, VertexArray vertexArray, AB arrayBuffer)
	{
		this.program = Objects.requireNonNull(program);
		this.vertexArray = Objects.requireNonNull(vertexArray);
		this.arrayBuffer = Objects.requireNonNull(arrayBuffer);
	}
	
	protected abstract void initializeArrayBuffer();
	
	protected abstract void disableAttributes();
	
	@Override
	public void setShaderProgram(ShaderProgram program)
	{
		disableAttributes();
		this.program = Objects.requireNonNull(program);
		initializeArrayBuffer();
	}
	
	protected void syncBuffers()
	{
		vertexArray.bindBuffer(arrayBuffer);
		arrayBuffer.sync();
		Util.printError("BaseRenderer[INTERNAL] array sync");
	}
	
	protected void draw(int mode)
	{
		draw(mode, 0, arrayBuffer.size());
	}
	
	protected void draw(int mode, int count)
	{
		draw(mode, 0, count);
	}
	
	protected void draw(int mode, int start, int count)
	{
		vertexArray.bind();
		syncBuffers();
		GL30.glDrawArrays(mode, start, count);
		Util.printError("BaseRenderer[INTERNAL] draw");
	}
	
	public void clear()
	{
		arrayBuffer.clear();
	}
	
	@Override
	public ShaderProgram getShaderProgram()
	{
		return program;
	}
}
