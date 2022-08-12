package com.alcadra.renderengine.renderer;

import java.util.List;
import java.util.Objects;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.ArrayBufferUpdate;
import com.alcadra.renderengine.buffer.SyncronizableArrayBuffer;
import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderInputType;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.util.Nonnull;
import com.alcadra.renderengine.util.Util;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@UpdatedFrom("BaseRenderer")
@Nonnull
public abstract class AbstractShaderRendererUpdate<AB extends ArrayBufferUpdate<?>> implements ShaderRendererUpdate {
	
	protected final VertexArray vertexArray;
	protected final AB arrayBuffer;
	protected ShaderProgram program;
	private int vertexSize;
	
	protected AbstractShaderRendererUpdate(ShaderProgram program, AB arrayBuffer)
	{
		this.program = Objects.requireNonNull(program);
		this.vertexArray = new VertexArray();
		this.arrayBuffer = Objects.requireNonNull(arrayBuffer);
		initializeArrayBuffer();
	}
	
	protected AbstractShaderRendererUpdate(ShaderProgram program, VertexArray vertexArray, AB arrayBuffer)
	{
		this.program = Objects.requireNonNull(program);
		this.vertexArray = Objects.requireNonNull(vertexArray);
		this.arrayBuffer = Objects.requireNonNull(arrayBuffer);
		initializeArrayBuffer();
	}
	
	protected void initializeArrayBuffer()
	{
		List<ShaderInput> inputs = program.getProgramInputs();
		vertexSize = 0;
		for (ShaderInput input : inputs) vertexSize += input.getType().getLength();
		int index = 0;
		int offset = 0;
		vertexArray.bind();
		vertexArray.bindBuffer(arrayBuffer);
		for (ShaderInput input : inputs)
		{
			ShaderInputType type = input.getType();
			vertexArray.vertexAttributePointer(index, type.getSize(), type.getType(), false, vertexSize, offset);
			vertexArray.enableVertexAttribute(index);
			offset += type.getLength();
			index++;
		}
		VertexArray.unbind();
	}
	
	protected void disableAttributes()
	{
		vertexArray.bind();
		for (int i = program.getProgramInputs().size() - 1; i >= 0; i--) vertexArray.disableVertexAttribute(i);
		VertexArray.unbind();
	}
	
	@Override
	public void setShaderProgram(ShaderProgram program)
	{
		disableAttributes();
		this.program = Objects.requireNonNull(program);
		initializeArrayBuffer();
	}
	
	@SuppressWarnings("rawtypes")
	protected void syncBuffers()
	{
		vertexArray.bindBuffer(arrayBuffer);
		if (arrayBuffer instanceof SyncronizableArrayBuffer s) s.sync();
		Util.printError("BaseRenderer[INTERNAL] array sync");
	}
	
	protected void draw(RenderingMode mode)
	{
		draw(mode, 0, arrayBuffer.size());
	}
	
	protected void draw(RenderingMode mode, int count)
	{
		draw(mode, 0, count);
	}
	
	protected void draw(RenderingMode mode, int start, int count)
	{
		vertexArray.bind();
		syncBuffers();
		GL30.glDrawArrays(mode.ordinal(), start, count);
		Util.printError("BaseRenderer[INTERNAL] draw");
	}
	
	protected int vertexSize()
	{
		return vertexSize;
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
