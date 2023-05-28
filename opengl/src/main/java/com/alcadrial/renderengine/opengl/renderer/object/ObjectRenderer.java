package com.alcadrial.renderengine.opengl.renderer.object;

import java.util.Objects;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.renderengine.core.util.WorkInProgress;
import com.alcadrial.renderengine.opengl.buffer.ObjectArrayBuffer;
import com.alcadrial.renderengine.opengl.renderer.AbstractShaderRenderer;
import com.alcadrial.renderengine.opengl.renderer.RenderingMode;
import com.alcadrial.renderengine.opengl.shader.ShaderInput;
import com.alcadrial.renderengine.opengl.shader.ShaderProgram;
import com.alcadrial.renderengine.opengl.shader.uniform.ShaderUniform;
import com.alcadrial.renderengine.opengl.shader.uniform.extra.ColorUniform;
import com.alcadrial.renderengine.opengl.util.SwitchableTracker;
import com.alcadrial.renderengine.opengl.util.color.Color;
import com.alcadrial.renderengine.opengl.util.color.ColorFormat;

@WorkInProgress
public class ObjectRenderer extends AbstractShaderRenderer<ObjectArrayBuffer<Object>> {
	
	private static final ShaderUniform<Matrix4f> TRANSFORMATION = ShaderUniform.ofMatrix4f("TRANSFORMATION");
	private static final ShaderUniform<Color> COLOR = new ColorUniform("COLOR", ColorFormat.RGBA);
	private static final ShaderUniform<Color> COLOR_RGB = new ColorUniform("COLOR_RGB", ColorFormat.RGB);
	private static final ShaderUniform<Integer> TEXTURE_SAMPLER = ShaderUniform.ofInt("TEXTURE_SAMPLER");
	
	private BufferableModel buildingValue;
	private Color color;
	private int textureSampler;
	private int offset;
	
	public ObjectRenderer(ShaderProgram program)
	{
		super(program, new ObjectArrayBuffer<>());
		color = Color.BLACK;
	}
	
	public ObjectBuilder start(BufferableModel value)
	{
		buildingValue = value;
		return new ObjectBuilder(this);
	}
	
	protected <B extends BufferableModel> SwitchableTracker<B> build(ObjectBuilder builder)
	{
		return new Tracker0<>(arrayBuffer.add(buildingValue, buildData(buildingValue)));
	}
	
	private float[] buildData(BufferableModel model)
	{
		int vertices = model.vertices();
		int vertexSize = vertexSize() / 4;
		float[] buildedData = new float[vertexSize * vertices];
		for (int i = 0, j = 0; i < vertices; i++, j = 0)
		{
			for (ShaderInput input : program.getProgramInputs())
			{
				float[] vertexData = model.getVertexData(i, input).getData();
				for (int k = 0; k < vertexData.length; k++, j++) buildedData[i * vertexSize + j] = vertexData[k];
			}
		}
		return buildedData;
	}
	
	public void enable(int index)
	{
		arrayBuffer.enable(index);
	}
	
	public void disable(int index)
	{
		arrayBuffer.disable(index);
	}
	
	@Override
	public void draw(RenderingMode mode, Matrix4f matrix, int count, int offset)
	{
		program.enable();
		program.setUniform(TRANSFORMATION, matrix);
		program.setUniform(TEXTURE_SAMPLER, textureSampler);
		program.setUniform(COLOR, color);
		program.setUniform(COLOR_RGB, color);
		draw(mode, arrayBuffer.intSize());
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
	
	public void reset()
	{
		clear();
	}
	
	@Override
	public void clear()
	{
		arrayBuffer.clear();
		offset = 0;
	}
	
	public void merge(ObjectRenderer renderer)
	{
		arrayBuffer.merge(renderer.arrayBuffer);
		offset += renderer.getOffset();
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	@Override
	public String toString()
	{
		return "ObjectRenderer{" + arrayBuffer + "}";
	}
	
	private class Tracker0<B extends BufferableModel> implements SwitchableTracker<B> {
		
		private SwitchableTracker<float[]> tracker;
		
		public Tracker0(SwitchableTracker<float[]> tracker)
		{
			this.tracker = tracker;
		}
		
		@Override
		public void modify(B elementData)
		{
			tracker.modify(buildData(elementData));
		}
		
		@Override
		public boolean turnOn()
		{
			return tracker.turnOn();
		}
		
		@Override
		public boolean turnOff()
		{
			return tracker.turnOff();
		}
		
		@Override
		public void remove()
		{
			tracker.remove();
		}
		
		@Override
		public boolean isOn()
		{
			return tracker.isOn();
		}
		
		@Override
		public boolean isOff()
		{
			return !tracker.isOff();
		}
		
		@Override
		public boolean isValid()
		{
			return tracker.isValid();
		}
	}
}
