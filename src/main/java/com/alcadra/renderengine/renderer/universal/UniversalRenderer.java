package com.alcadra.renderengine.renderer.universal;

import java.util.Objects;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBufferStack;
import com.alcadra.renderengine.buffer.FloatArrayBufferStack;
import com.alcadra.renderengine.buffer.FloatArrayBufferUpdate;
import com.alcadra.renderengine.renderer.AbstractIndicesRendererUpdate;
import com.alcadra.renderengine.renderer.AbstractVertexBuilder;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.shader.uniform.ShaderUniform;
import com.alcadra.renderengine.util.Nonnull;

@Nonnull
public class UniversalRenderer extends AbstractIndicesRendererUpdate<FloatArrayBufferStack> {
	
	private static final ShaderUniform<Matrix4f> TRANSFORMATION = ShaderUniform.ofMatrix4f("TRANSFORMATION");
	private static final ShaderUniform<Vector4f> COLOR = ShaderUniform.ofVector4f("COLOR");
	private static final ShaderUniform<Vector3f> COLOR_RGB = ShaderUniform.ofVector3f("COLOR_RGB");
	private static final ShaderUniform<Integer> TEXTURE_SAMPLER = ShaderUniform.ofInt("TEXTURE_SAMPLER");
	
	private ElementArrayBufferStack elementStack;
	private Vector4f color;
	private int textureSampler;
	private int offset;
	
	public UniversalRenderer(ShaderProgram program)
	{
		this(program, new ElementArrayBufferStack());
	}
	
	public UniversalRenderer(ShaderProgram program, ElementArrayBufferStack elementStack)
	{
		super(program, new FloatArrayBufferStack(), elementStack);
		this.elementStack = elementStack;
		color = Vector4f.getOrigin();
		textureSampler = 0;
		offset = 0;
	}
	
	@Override
	public void drawPoints(RenderingMode mode, Matrix4f matrix)
	{
		program.enable();
		program.setUniform(TRANSFORMATION, matrix);
		program.setUniform(TEXTURE_SAMPLER, textureSampler);
		program.setUniform(COLOR, color);
		program.setUniform(COLOR_RGB, color.xyz());
		drawIndices(mode);
	}
	
	public void drawVertices(RenderingMode mode, Matrix4f matrix)
	{
		program.enable();
		program.setUniform(TRANSFORMATION, matrix);
		program.setUniform(TEXTURE_SAMPLER, textureSampler);
		program.setUniform(COLOR, color);
		program.setUniform(COLOR_RGB, color.xyz());
		draw(mode);
	}
	
	public VertexBuilder vertex(float x, float y, float z)
	{
		return new VertexBuilder(x, y, z);
	}
	
	public UniversalRenderer vertex(int index)
	{
		elements.add(index);
		return this;
	}
	
	public void pushBuffer()
	{
		pushBuffer(new FloatArrayBufferUpdate());
	}
	
	public void pushBuffer(FloatArrayBufferUpdate arrayBuffer)
	{
		pushBuffer(arrayBuffer, new ElementArrayBuffer());
	}
	
	public void pushBuffer(FloatArrayBufferUpdate arrayBuffer, ElementArrayBuffer elementBuffer)
	{
		this.arrayBuffer.push(arrayBuffer);
		elementStack.push(elementBuffer);
		offset = elementStack.getMax();
	}
	
	public void pushBufferMerge(FloatArrayBufferUpdate buffer)
	{
		pushBufferMerge(buffer, new ElementArrayBuffer());
	}
	
	public void pushBufferMerge(FloatArrayBufferUpdate buffer, ElementArrayBuffer elementBuffer)
	{
		FloatArrayBufferUpdate b = arrayBuffer.current();
		buffer.merge(b);
		arrayBuffer.push(buffer);
		ElementArrayBuffer e = elementStack.current();
		elementBuffer.merge(e, elementBuffer.getMax());
		elementStack.push(elementBuffer);
		offset = elementStack.getMax();
	}
	
	public void pullBuffer()
	{
		arrayBuffer.pull();
		elementStack.pull();
		offset = 0;
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	public void setUniformColor(Vector4f color)
	{
		Objects.requireNonNull(color);
		this.color = color;
	}
	
	public void setUniformColor(float r, float g, float b)
	{
		color = new Vector4f(r / 255, g / 255, b / 255, 1);
	}
	
	public void setUniformColor(float r, float g, float b, float a)
	{
		color = new Vector4f(r / 255, g / 255, b / 255, a / 255);
	}
	
	public void setUniformTextureSampler(int textureSampler)
	{
		if (textureSampler >= 0 && textureSampler < 32) this.textureSampler = textureSampler;
	}
	
	@Override
	public void clear()
	{
		super.clear();
		offset = 0;
	}
	
	public class VertexBuilder extends AbstractVertexBuilder {
		
		public VertexBuilder(float x, float y, float z)
		{
			super(UniversalRenderer.this.program, x, y, z);
		}
		
		@Override
		public void end()
		{
			arrayBuffer.addAll(gatherProgramData());
			map.clear();
			elements.add(offset++);
		}
	}
}
