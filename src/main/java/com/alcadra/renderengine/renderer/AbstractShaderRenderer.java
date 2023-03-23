package com.alcadra.renderengine.renderer;

import java.util.List;

import org.lwjgl.opengl.GL30;

import com.alcadra.math.Matrix4f;
import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.SyncronizableArrayBuffer;
import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderInputType;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.shader.ShaderUtil;
import com.alcadra.renderengine.util.Nonnull;

@Nonnull
public abstract class AbstractShaderRenderer<AB extends ArrayBuffer<?>> extends AbstractRenderer<AB> implements ShaderRenderer {
	
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
		disableAttributes();
		this.program = ShaderUtil.assertBuildedProgram(program);
		initializeVertexArray();
	}
	
	@SuppressWarnings("rawtypes")
	protected void syncBuffers()
	{
		arrayBuffer.bind();
		if (arrayBuffer instanceof SyncronizableArrayBuffer s) s.sync();
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
	public void drawVertices(RenderingMode mode, Matrix4f matrix)
	{
		drawVertices(mode, matrix, arrayBuffer.size() * 4 / vertexSize());
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
