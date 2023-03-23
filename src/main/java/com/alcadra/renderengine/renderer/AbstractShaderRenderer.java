package com.alcadra.renderengine.renderer;

import java.util.List;
import java.util.Objects;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.SyncronizableArrayBuffer;
import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderInputType;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.util.Nonnull;

@Nonnull
public abstract class AbstractShaderRenderer<AB extends ArrayBuffer<?>> implements ShaderRenderer {
	
	protected final VertexArray vertexArray;
	protected final AB arrayBuffer;
	protected ShaderProgram program;
	private int vertexSize;
	
	protected AbstractShaderRenderer(ShaderProgram program, AB arrayBuffer)
	{
		this.program = Objects.requireNonNull(program);
		this.vertexArray = new VertexArray();
		this.arrayBuffer = Objects.requireNonNull(arrayBuffer);
		initializeArrayBuffer();
	}
	
	protected AbstractShaderRenderer(ShaderProgram program, VertexArray vertexArray, AB arrayBuffer)
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
		arrayBuffer.bind();
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
		arrayBuffer.bind();
		if (arrayBuffer instanceof SyncronizableArrayBuffer s) s.sync();
	}
	
	protected void draw(RenderingMode mode)
	{
 		draw(mode, 0, arrayBuffer.size() / vertexSize() * 4);
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
