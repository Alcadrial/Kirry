package com.alcadra.renderengine.renderer.object;

import java.util.Objects;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.buffer.ObjectArrayBufferUpdate;
import com.alcadra.renderengine.renderer.AbstractShaderRendererUpdate;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.shader.uniform.ShaderUniform;
import com.alcadra.renderengine.util.Tracker;
import com.alcadra.renderengine.util.WorkInProgress;
import com.alcadra.renderengine.util.color.Color;
import com.alcadra.renderengine.util.color.VectorColorRGBA;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@WorkInProgress
@UpdatedFrom("ObjectRenderer")
public class ObjectRendererUpdate extends AbstractShaderRendererUpdate<ObjectArrayBufferUpdate<Object>> {
	
	private static final ShaderUniform<Matrix4f> TRANSFORMATION = ShaderUniform.ofMatrix4f("TRANSFORMATION");
	private static final ShaderUniform<Vector4f> COLOR = ShaderUniform.ofVector4f("COLOR");
	private static final ShaderUniform<Vector3f> COLOR_RGB = ShaderUniform.ofVector3f("COLOR_RGB");
	private static final ShaderUniform<Integer> TEXTURE_SAMPLER = ShaderUniform.ofInt("TEXTURE_SAMPLER");
	
	private BufferableModel buildingValue;
	private Vector4f color;
	private int textureSampler;
	private int offset;
	
	public ObjectRendererUpdate(ShaderProgram program)
	{
		super(program, new ObjectArrayBufferUpdate<>());
		color = Color.BLACK;
	}
	
	public ObjectBuilderUpdate start(BufferableModel value)
	{
		buildingValue = value;
		return new ObjectBuilderUpdate(this);
	}
	
	protected <B extends BufferableModel> Tracker<B> build(ObjectBuilderUpdate builder)
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
	
	public void draw(RenderingMode mode, Matrix4f matrix)
	{
		program.enable();
		program.setUniform(TRANSFORMATION, matrix);
		program.setUniform(TEXTURE_SAMPLER, textureSampler);
		program.setUniform(COLOR, color);
		program.setUniform(COLOR_RGB, color.xyz());
		draw(mode, arrayBuffer.intSize());
	}
	
	public void setUniformColor(VectorColorRGBA color)
	{
		this.color = Objects.requireNonNull(color);
	}
	
	public void setUniformColor(float r, float g, float b)
	{
		color = Color.ofVector(r, g, b);
	}
	
	public void setUniformColor(float r, float g, float b, float a)
	{
		color = Color.ofVector(r, g, b, a);
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
	
	public void merge(ObjectRendererUpdate renderer)
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
	
	private class Tracker0<B extends BufferableModel> implements Tracker<B> {
		
		private Tracker<float[]> tracker;
		
		public Tracker0(Tracker<float[]> tracker)
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
