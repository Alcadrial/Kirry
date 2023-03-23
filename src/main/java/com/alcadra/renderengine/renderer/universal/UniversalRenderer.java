package com.alcadra.renderengine.renderer.universal;

import java.util.Objects;

import com.alcadra.math.matrix.Matrix4f;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBufferStack;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.buffer.FloatArrayBufferStack;
import com.alcadra.renderengine.buffer.SimpleBuffer;
import com.alcadra.renderengine.renderer.AbstractIndicesRenderer;
import com.alcadra.renderengine.renderer.AbstractVertexBuilder;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.shader.uniform.ShaderUniform;
import com.alcadra.renderengine.shader.uniform.extra.ColorUniform;
import com.alcadra.renderengine.util.Nonnull;
import com.alcadra.renderengine.util.color.Color;
import com.alcadra.renderengine.util.color.ColorFormat;

@Nonnull
public class UniversalRenderer extends AbstractIndicesRenderer<FloatArrayBufferStack> {
	
	private static final ShaderUniform<Matrix4f> TRANSFORMATION = ShaderUniform.ofMatrix4f("TRANSFORMATION");
	private static final ShaderUniform<Color> COLOR = new ColorUniform("COLOR", ColorFormat.RGBA);
	private static final ShaderUniform<Color> COLOR_RGB = new ColorUniform("COLOR_RGB", ColorFormat.RGB);
	private static final ShaderUniform<Integer> TEXTURE_SAMPLER = ShaderUniform.ofInt("TEXTURE_SAMPLER");
	
	private BufferNode head;
	private ElementArrayBufferStack elementStack;
	private Color color;
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
		color = Color.BLACK;
		textureSampler = 0;
		offset = 0;
	}
	
	@Override
	public void draw(RenderingMode mode, Matrix4f matrix, int count, int offset)
	{
		program.enable();
		program.setUniform(TRANSFORMATION, matrix);
		program.setUniform(TEXTURE_SAMPLER, textureSampler);
		program.setUniform(COLOR, color);
		program.setUniform(COLOR_RGB, color);
		super.draw(mode, matrix, count, offset);
	}
	
	public VertexBuilder vertex(float x, float y, float z)
	{
		return new VertexBuilder(x, y, z);
	}
	
	public UniversalRenderer vertex(int index)
	{
		if (useIndices) elements.add(index);
		return this;
	}
	
	public void pushBuffer()
	{
		pushBuffer0(pushNewBuffer(new FloatArrayBuffer()), pushNewBuffer(new ElementArrayBuffer()));
	}
	
	public void pushBuffer(FloatArrayBuffer arrayBuffer)
	{
		if (head != null) head.count++;
		pushBuffer0(arrayBuffer, pushNewBuffer(new ElementArrayBuffer()));
	}
	
	public void pushBuffer(FloatArrayBuffer arrayBuffer, ElementArrayBuffer elementBuffer)
	{
		if (head != null) head.count += 2;
		pushBuffer0(arrayBuffer, elementBuffer);
	}
	
	private void pushBuffer0(FloatArrayBuffer arrayBuffer, ElementArrayBuffer elementBuffer)
	{
		this.arrayBuffer.push(arrayBuffer);
		elementStack.push(elementBuffer);
		offset = elementBuffer.getMax() + 1;
		initializeVertexArray();
	}
	
	public void pushBufferMerge(FloatArrayBuffer buffer)
	{
		if (head != null) head.count++;
		pushBufferMerge0(buffer, new ElementArrayBuffer());
	}
	
	public void pushBufferMerge(FloatArrayBuffer buffer, ElementArrayBuffer elementBuffer)
	{
		if (head != null) head.count += 2;
		pushBufferMerge0(buffer, elementBuffer);
	}
	
	private void pushBufferMerge0(FloatArrayBuffer buffer, ElementArrayBuffer elementBuffer)
	{
		FloatArrayBuffer b = arrayBuffer.current();
		buffer.merge(b);
		arrayBuffer.push(buffer);
		ElementArrayBuffer e = elementStack.current();
		elementBuffer.merge(e, elementBuffer.getMax());
		elementStack.push(elementBuffer);
		offset = elementStack.getMax() + 1;
		initializeVertexArray();
	}
	
	private <B extends SimpleBuffer> B pushNewBuffer(B buffer)
	{
		BufferNode n = head;
		head = new BufferNode(buffer);
		head.next = n;
		return buffer;
	}
	
	public void pullBuffer()
	{
		if (head != null)
		{
			if (head.count == 0)
			{
				head.buffer.close();
				head = head.next;
			}
			else head.count--;
		}
		arrayBuffer.pull();
		if (head != null)
		{
			if (head.count == 0)
			{
				head.buffer.close();
				head = head.next;
			}
			else head.count--;
		}
		elementStack.pull();
		offset = elementStack.getMax() + 1;
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	public void setUniformColor(Color color)
	{
		this.color = Objects.requireNonNull(color);
	}
	
	public void setUniformColor(float r, float g, float b)
	{
		color = Color.of(r, g, b);
	}
	
	public void setUniformColor(float r, float g, float b, float a)
	{
		color = Color.of(r, g, b, a);
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
	
	public void bind()
	{
		vertexArray.bind();
	}
	
	public class VertexBuilder extends AbstractVertexBuilder<VertexBuilder> {
		
		public VertexBuilder(float x, float y, float z)
		{
			super(UniversalRenderer.this.program, x, y, z);
		}
		
		@Override
		public void end()
		{
			arrayBuffer.addAll(gatherProgramData());
			map.clear();
			if (useIndices) elements.add(offset++);
		}
	}
	
	private class BufferNode {
		
		private SimpleBuffer buffer;
		private BufferNode next;
		private int count;
		
		public BufferNode(SimpleBuffer buffer)
		{
			this.buffer = buffer;
			count = 0;
		}
	}
}
