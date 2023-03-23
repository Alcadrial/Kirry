package com.alcadra.renderengine.renderer.universal;

import java.util.Objects;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.buffer.FloatArrayBufferStack;
import com.alcadra.renderengine.renderer.AbstractIndicesRendererUpdate;
import com.alcadra.renderengine.renderer.AbstractVertexBuilder;
import com.alcadra.renderengine.renderer.IndicesRenderer;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.shader.uniform.ShaderUniform;
import com.alcadra.renderengine.util.Nonnull;

@Nonnull
public class UniversalRenderer extends AbstractIndicesRendererUpdate<FloatArrayBufferStack> {
	
	private static final ShaderUniform<Matrix4f> TRANSFORMATION = ShaderUniform.ofMatrix4f("TRANSFORMATION");
	private static final ShaderUniform<Vector4f> COLOR = ShaderUniform.ofVector4f("COLOR");
	private static final ShaderUniform<Vector3f> COLOR_RGB = ShaderUniform.ofVector3f("COLOR_RGB");
	private static final ShaderUniform<Integer> TEXTURE_SAMPLER = ShaderUniform.ofInt("TEXTURE_SAMPLER");
	
	private Vector4f color;
	private int textureSampler;
	private int offset;
	
	public UniversalRenderer(ShaderProgram program)
	{
		super(program, new FloatArrayBufferStack());
		color = Vector4f.getOrigin();
		textureSampler = 0;
		offset = 0;
	}
	
	@Override
	public void drawPoints(int mode, Matrix4f matrix)
	{
		program.enable();
		program.setUniform(TRANSFORMATION, matrix);
		program.setUniform(TEXTURE_SAMPLER, textureSampler);
		program.setUniform(COLOR, color);
		program.setUniform(COLOR_RGB, color.xyz());
		drawIndices(mode);
	}
	
	public VertexBuilder vertex(float x, float y, float z)
	{
		return new VertexBuilder(x, y, z);
	}
	
	public IndicesRenderer vertex(int index)
	{
		elements.add(index);
		return this;
	}
	
	public void pushBuffer(FloatArrayBuffer buffer)
	{
		arrayBuffer.push(buffer);
	}
	
	public void pushBufferMerge(FloatArrayBuffer buffer)
	{
		FloatArrayBuffer b = arrayBuffer.current();
		buffer.merge(b);
		arrayBuffer.push(buffer);
	}
	
	public void pullBuffer()
	{
		arrayBuffer.pull();
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
		
		public void end(int index)
		{
			arrayBuffer.addAll(gatherProgramData());
			map.clear();
			elements.add(index);
		}
	}
}
