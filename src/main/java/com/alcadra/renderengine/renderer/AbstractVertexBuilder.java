package com.alcadra.renderengine.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderInputType;
import com.alcadra.renderengine.shader.ShaderProgram;

public abstract class AbstractVertexBuilder<VB extends AbstractVertexBuilder<VB>> {
	
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
	
	public VB set(ShaderInput input, int... values)
	{
		ShaderInputType type = input.getType();
		Float[] floatIntBitsArray = new Float[type.getSize()];
		for (int i = 0; i < type.getSize(); i++) floatIntBitsArray[i] = Float.intBitsToFloat(values[i]);
		map.put(input, Arrays.asList(floatIntBitsArray));
		return myself();
	}
	
	public VB set(ShaderInput input, Float... values)
	{
		map.put(input, Arrays.asList(values));
		return myself();
	}
	
	public VB set(ShaderInput input, double... values)
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
		return myself();
	}
	
	public VB uv(float u, float v)
	{
		return vec2(ShaderInput.UV, u, v);
	}
	
	public VB uvExtra1(float u, float v)
	{
		return vec2(ShaderInput.UV_EXTRA_1, u, v);
	}
	
	public VB uvExtra2(float u, float v)
	{
		return vec2(ShaderInput.UV_EXTRA_2, u, v);
	}
	
	public VB color(float r, float g, float b)
	{
		return vec3(ShaderInput.COLOR_RGB, r / 255, g / 255, b / 255);
	}
	
	public VB color(float r, float g, float b, float a)
	{
		return vec4(ShaderInput.COLOR, r / 255, g / 255, b / 255, a / 255);
	}
	
	public VB normal(float x, float y, float z)
	{
		return vec3(ShaderInput.NORMAL_POSITION, x, y, z);
	}
	
	public VB position(float x, float y, float z)
	{
		return vec3(ShaderInput.POSITION, x, y, z);
	}
	
	public VB positionExtra1(float x, float y, float z)
	{
		return vec3(ShaderInput.POSITION_EXTRA_1, x, y, z);
	}
	
	public VB positionExtra2(float x, float y, float z)
	{
		return vec3(ShaderInput.POSITION_EXTRA_2, x, y, z);
	}
	
	public VB scalarFloat(ShaderInput input, float a)
	{
		if (input.getType().getSize() != 1) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 1 values");
		map.put(input, List.of(a));
		return myself();
	}
	
	public VB vec2(ShaderInput input, float a, float b)
	{
		if (input.getType().getSize() != 2) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 2 values");
		map.put(input, List.of(a, b));
		return myself();
	}
	
	public VB vec3(ShaderInput input, float a, float b, float c)
	{
		if (input.getType().getSize() != 3) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 3 values");
		map.put(input, List.of(a, b, c));
		return myself();
	}
	
	public VB vec4(ShaderInput input, float a, float b, float c, float d)
	{
		if (input.getType().getSize() != 4) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 4 values");
		map.put(input, List.of(a, b, c, d));
		return myself();
	}
	
	protected List<Float> gatherProgramData()
	{
		List<Float> list = new ArrayList<>(program.getProgramInputs().size() * 4);
		for (ShaderInput input : program.getProgramInputs())
		{
			List<Float> data = map.get(input);
			if (data != null) list.addAll(data);
			else for (int i = input.getType().getSize(); i > 0; i--) list.add(0F);
		}
		return List.copyOf(list);
	}
	
	public abstract void end();
	
	@SuppressWarnings("unchecked")
	private VB myself()
	{
		return (VB) this;
	}
}
