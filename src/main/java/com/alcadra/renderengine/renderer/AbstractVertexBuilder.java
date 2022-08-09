package com.alcadra.renderengine.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alcadra.renderengine.renderer.object.IllegalBuildingState;
import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderInputType;
import com.alcadra.renderengine.shader.ShaderProgram;

public abstract class AbstractVertexBuilder {
	
	protected final Map<ShaderInput, List<Float>> map;
	protected final ShaderProgram program;
	
	protected AbstractVertexBuilder(ShaderProgram program)
	{
		map = new HashMap<>();
		this.program = program;
	}
	
	protected AbstractVertexBuilder(ShaderProgram program, float x, float y, float z)
	{
		this(program);
		map.put(ShaderInput.VERTEX_POSITION, List.of(x, y, z));
	}
	
	public AbstractVertexBuilder set(ShaderInput input, int... values)
	{
		ShaderInputType type = input.getType();
		Float[] floatIntBitsArray = new Float[type.getSize()];
		for (int i = 0; i < type.getSize(); i++) floatIntBitsArray[i] = Float.intBitsToFloat(values[i]);
		map.put(input, Arrays.asList(floatIntBitsArray));
		return this;
	}
	
	public AbstractVertexBuilder set(ShaderInput input, Float... values)
	{
		map.put(input, Arrays.asList(values));
		return this;
	}
	
	public AbstractVertexBuilder set(ShaderInput input, double... values)
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
	
	public AbstractVertexBuilder uv(float u, float v)
	{
		return vec2(ShaderInput.UV, u, v);
	}
	
	public AbstractVertexBuilder uvExtra1(float u, float v)
	{
		return vec2(ShaderInput.UV_EXTRA_1, u, v);
	}
	
	public AbstractVertexBuilder uvExtra2(float u, float v)
	{
		return vec2(ShaderInput.UV_EXTRA_2, u, v);
	}
	
	public AbstractVertexBuilder color(float r, float g, float b)
	{
		return vec3(ShaderInput.COLOR_RGB, r / 255, g / 255, b / 255);
	}
	
	public AbstractVertexBuilder color(float r, float g, float b, float a)
	{
		return vec4(ShaderInput.COLOR, r / 255, g / 255, b / 255, a / 255);
	}
	
	public AbstractVertexBuilder normal(float x, float y, float z)
	{
		return vec3(ShaderInput.NORMAL_POSITION, x, y, z);
	}
	
	public AbstractVertexBuilder position(float x, float y, float z)
	{
		return vec3(ShaderInput.POSITION, x, y, z);
	}
	
	public AbstractVertexBuilder positionExtra1(float x, float y, float z)
	{
		return vec3(ShaderInput.POSITION_EXTRA_1, x, y, z);
	}
	
	public AbstractVertexBuilder positionExtra2(float x, float y, float z)
	{
		return vec3(ShaderInput.POSITION_EXTRA_2, x, y, z);
	}
	
	public AbstractVertexBuilder scalarFloat(ShaderInput input, float a)
	{
		if (input.getType().getSize() != 1) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 1 values");
		map.put(input, List.of(a));
		return this;
	}
	
	public AbstractVertexBuilder vec2(ShaderInput input, float a, float b)
	{
		if (input.getType().getSize() != 2) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 2 values");
		map.put(input, List.of(a, b));
		return this;
	}
	
	public AbstractVertexBuilder vec3(ShaderInput input, float a, float b, float c)
	{
		if (input.getType().getSize() != 3) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 3 values");
		map.put(input, List.of(a, b, c));
		return this;
	}
	
	public AbstractVertexBuilder vec4(ShaderInput input, float a, float b, float c, float d)
	{
		if (input.getType().getSize() != 4) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 4 values");
		map.put(input, List.of(a, b, c, d));
		return this;
	}
	
	protected List<Float> gatherProgramData()
	{
		List<Float> list = new ArrayList<>(map.size() * 4);
		for (ShaderInput input : program.getProgramInputs())
		{
			List<Float> data = map.get(input);
			if (data != null) list.addAll(data);
		}
		return List.copyOf(list);
	}
	
	public abstract void end();
}
