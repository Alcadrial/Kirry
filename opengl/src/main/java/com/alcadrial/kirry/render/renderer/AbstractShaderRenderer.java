package com.alcadrial.kirry.render.renderer;

import java.util.List;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kirry.render.buffer.GlBuffer;
import com.alcadrial.kirry.render.shader.ShaderInput;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.shader.ShaderUtil;

public abstract class AbstractShaderRenderer<AB extends GlBuffer> extends AbstractRenderer<AB> implements ShaderProgrammable {
	
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
	
	@Override
	protected void draw(RenderingMode mode)
	{
		draw(mode, -1);
	}
	
	@Override
	public void draw(RenderingMode mode, Matrix4f matrix)
	{
		draw(mode, matrix, -1);
	}
	
	@Override
	public int vertexSize()
	{
		return vertexSize;
	}
	
	@Override
	public ShaderProgram getShaderProgram()
	{
		return program;
	}
}
