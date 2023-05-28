package com.alcadrial.renderengine.opengl.renderer;

import java.util.List;

import org.lwjgl.opengl.GL30;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.renderengine.core.util.Nonnull;
import com.alcadrial.renderengine.opengl.buffer.GlBuffer;
import com.alcadrial.renderengine.opengl.shader.ShaderInput;
import com.alcadrial.renderengine.opengl.shader.ShaderInputType;
import com.alcadrial.renderengine.opengl.shader.ShaderProgram;
import com.alcadrial.renderengine.opengl.shader.ShaderUtil;

@Nonnull
public abstract class AbstractShaderRenderer<AB extends GlBuffer> extends AbstractRenderer<AB> implements ShaderRenderer {
	
	protected ShaderProgram program;
	private int vertexSize;
	
	protected AbstractShaderRenderer(ShaderProgram program, AB arrayBuffer)
	{
		super(arrayBuffer);
		this.program = ShaderUtil.assertBuildedProgram(program);
		initializeVertexArray();
	}
	
	protected AbstractShaderRenderer(ShaderProgram program, VertexArray vertexArray, AB arrayBuffer)
	{
		super(vertexArray, arrayBuffer);
		this.program = ShaderUtil.assertBuildedProgram(program);
		initializeVertexArray();
	}
	
	protected void initializeVertexArray()
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
		if (this.program != program)
		{
			disableAttributes();
			this.program = ShaderUtil.assertBuildedProgram(program);
			initializeVertexArray();
		}
	}
	
	protected void syncBuffers()
	{
		arrayBuffer.bind();
		arrayBuffer.sync();
	}
	
	protected void draw(RenderingMode mode)
	{
		draw(mode, arrayBuffer.size() * 4 / vertexSize());
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
	public void draw(RenderingMode mode, Matrix4f matrix)
	{
		draw(mode, matrix, arrayBuffer.size() * 4 / vertexSize());
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
