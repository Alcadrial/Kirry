package com.alcadra.renderengine.renderer.universal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.renderer.AbstractIndicesRendererUpdate;
import com.alcadra.renderengine.renderer.VertexArray;
import com.alcadra.renderengine.renderer.object.IllegalBuildingState;
import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderInputType;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.shader.uniform.ShaderUniform;
import com.alcadra.renderengine.util.Nonnull;

@Nonnull
public class UniversalRenderer extends AbstractIndicesRendererUpdate<FloatArrayBuffer> {
	
	private static final ShaderUniform<Matrix4f> TRANSFORMATION = ShaderUniform.ofMatrix4f("TRANSFORMATION");
	private static final ShaderUniform<Vector4f> COLOR = ShaderUniform.ofVector4f("COLOR");
	private static final ShaderUniform<Vector3f> COLOR_RGB = ShaderUniform.ofVector3f("COLOR_RGB");
	private static final ShaderUniform<Integer> TEXTURE_SAMPLER = ShaderUniform.ofInt("TEXTURE_SAMPLER");
	
	private Vector4f color;
	private int textureSampler;
	private int offset;
	
	public UniversalRenderer(ShaderProgram program)
	{
		super(program, new FloatArrayBuffer());
		color = Vector4f.getOrigin();
		textureSampler = 0;
		offset = 0;
		initializeArrayBuffer();
	}
	
	@Override
	protected void initializeArrayBuffer()
	{
		List<ShaderInput> inputs = program.getProgramInputs();
		int size = 0;
		for (ShaderInput input : inputs) size += input.getType().getLength();
		int index = 0;
		int stride = 0;
		vertexArray.bind();
		vertexArray.bindBuffer(arrayBuffer);
		for (ShaderInput input : inputs)
		{
			ShaderInputType type = input.getType();
			vertexArray.vertexAttributePointer(index, type.getSize(), type.getType(), false, size, stride);
			vertexArray.enableVertexAttribute(index);
			stride += type.getLength();
			index++;
		}
		VertexArray.unbind();
	}
	
	@Override
	protected void disableAttributes()
	{
		vertexArray.bind();
		for (int i = program.getProgramInputs().size() - 1; i >= 0; i--) vertexArray.disableVertexAttribute(i);
		VertexArray.unbind();
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
	
	public UniversalRenderer vertex(int index)
	{
		elements.add(index);
		return this;
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
	
	public class VertexBuilder {
		
		private final Map<ShaderInput, List<Float>> map;
		
		public VertexBuilder(float x, float y, float z)
		{
			map = new HashMap<>();
			map.put(ShaderInput.VERTEX_POSITION, List.of(x, y, z));
		}
		
		public VertexBuilder set(ShaderInput input, int... values)
		{
			ShaderInputType type = input.getType();
			Float[] floatIntBitsArray = new Float[type.getSize()];
			for (int i = 0; i < type.getSize(); i++) floatIntBitsArray[i] = Float.intBitsToFloat(values[i]);
			map.put(input, Arrays.asList(floatIntBitsArray));
			return this;
		}
		
		public VertexBuilder set(ShaderInput input, Float... values)
		{
			map.put(input, Arrays.asList(values));
			return this;
		}
		
		public VertexBuilder set(ShaderInput input, double... values)
		{
			ShaderInputType type = input.getType();
			Float[] dividedValues = new Float[type.getSize() * 2];
			for (int i = type.getSize() - 1; i >= 0; i--)
			{
				long l = Double.doubleToLongBits(values[i]);
				dividedValues[i * 2] = Float.intBitsToFloat((int) (l >>> 32));
				dividedValues[i * 2 + 1] = Float.intBitsToFloat((int) l);
			}
			map.put(input, Arrays.asList(dividedValues));
			return this;
		}
		
		public VertexBuilder uv(float u, float v)
		{
			return vec2(ShaderInput.UV, u, v);
		}
		
		public VertexBuilder uvExtra1(float u, float v)
		{
			return vec2(ShaderInput.UV_EXTRA_1, u, v);
		}
		
		public VertexBuilder uvExtra2(float u, float v)
		{
			return vec2(ShaderInput.UV_EXTRA_2, u, v);
		}
		
		public VertexBuilder color(float r, float g, float b)
		{
			return vec3(ShaderInput.COLOR_RGB, r / 255, g / 255, b / 255);
		}
		
		public VertexBuilder color(float r, float g, float b, float a)
		{
			return vec4(ShaderInput.COLOR, r / 255, g / 255, b / 255, a / 255);
		}
		
		public VertexBuilder normal(float x, float y, float z)
		{
			return vec3(ShaderInput.NORMAL_POSITION, x, y, z);
		}
		
		public VertexBuilder position(float x, float y, float z)
		{
			return vec3(ShaderInput.POSITION, x, y, z);
		}
		
		public VertexBuilder positionExtra1(float x, float y, float z)
		{
			return vec3(ShaderInput.POSITION_EXTRA_1, x, y, z);
		}
		
		public VertexBuilder positionExtra2(float x, float y, float z)
		{
			return vec3(ShaderInput.POSITION_EXTRA_2, x, y, z);
		}
		
		public VertexBuilder scalarFloat(ShaderInput input, float a)
		{
			if (input.getType().getSize() != 1) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 1 values");
			map.put(input, List.of(a));
			return this;
		}
		
		public VertexBuilder vec2(ShaderInput input, float a, float b)
		{
			if (input.getType().getSize() != 2) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 2 values");
			map.put(input, List.of(a, b));
			return this;
		}
		
		public VertexBuilder vec3(ShaderInput input, float a, float b, float c)
		{
			if (input.getType().getSize() != 3) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 3 values");
			map.put(input, List.of(a, b, c));
			return this;
		}
		
		public VertexBuilder vec4(ShaderInput input, float a, float b, float c, float d)
		{
			if (input.getType().getSize() != 4) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 4 values");
			map.put(input, List.of(a, b, c, d));
			return this;
		}
		
		public void end()
		{
			for (ShaderInput input : program.getProgramInputs())
			{
				List<Float> data = map.get(input);
				if (data != null) arrayBuffer.addAll(data);
			}
			map.clear();
			elements.add(offset++);
		}
		
		public void end(int index)
		{
			for (ShaderInput input : program.getProgramInputs())
			{
				List<Float> data = map.get(input);
				if (data != null) arrayBuffer.addAll(data);
			}
			map.clear();
			elements.add(index);
		}
	}
}
