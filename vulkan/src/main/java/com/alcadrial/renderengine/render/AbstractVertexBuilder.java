package com.alcadrial.renderengine.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.shader.ShaderInputType;

public abstract class AbstractVertexBuilder<VB extends AbstractVertexBuilder<VB, VR>, VR extends IVertexResult> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected final Map<ShaderInput, float[]> map;
	protected final GraphicsPipeline pipeline;
	
	protected AbstractVertexBuilder(GraphicsPipeline pipeline)
	{
		map = new HashMap<>();
		this.pipeline = pipeline;
	}
	
	protected AbstractVertexBuilder(GraphicsPipeline pipeline, float x, float y, float z)
	{
		this(pipeline);
		map.put(ShaderInput.VERTEX_POSITION, new float[] {x, y, z});
	}
	
	public VB set(ShaderInput input, int... values)
	{
		float[] floatIntBitsArray = new float[input.getType().getSize()];
		for (int i = 0; i < floatIntBitsArray.length; i++) floatIntBitsArray[i] = Float.intBitsToFloat(values[i]);
		map.put(input, floatIntBitsArray);
		return myself();
	}
	
	public VB set(ShaderInput input, float... values)
	{
		map.put(input, values);
		return myself();
	}
	
	public VB set(ShaderInput input, double... values)
	{
		ShaderInputType type = input.getType();
		float[] dividedValues = new float[type.getSize() * 2];
		for (int i = type.getSize() - 1; i >= 0; i--)
		{
			long l = Double.doubleToLongBits(values[i]);
			dividedValues[i * 2] = Float.intBitsToFloat((int) (l >>> 32));
			dividedValues[i * 2 + 1] = Float.intBitsToFloat((int) l);
		}
		map.put(input, dividedValues);
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
		map.put(input, new float[] {a});
		return myself();
	}
	
	public VB vec2(ShaderInput input, float a, float b)
	{
		if (input.getType().getSize() != 2) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 2 values");
		map.put(input, new float[] {a, b});
		return myself();
	}
	
	public VB vec3(ShaderInput input, float a, float b, float c)
	{
		if (input.getType().getSize() != 3) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 3 values");
		map.put(input, new float[] {a, b, c});
		return myself();
	}
	
	public VB vec4(ShaderInput input, float a, float b, float c, float d)
	{
		if (input.getType().getSize() != 4) throw new IllegalBuildingState("ShaderInput require " + input.getType().getSize() + " values, but the method inserts 4 values");
		map.put(input, new float[] {a, b, c, d});
		return myself();
	}
	
	protected List<Float> gatherProgramData()
	{
		List<ShaderInput> inputs = pipeline.getInputs();
		List<Float> list = new ArrayList<>(inputs.size() * 4);
		for (ShaderInput input : inputs)
		{
			float[] data = map.get(input);
			if (data != null) for (float f : data) list.add(f);
			else for (int i = input.getType().getSize(); i > 0; i--) list.add(0F);
		}
		return List.copyOf(list);
	}
	
	protected float[] gatherProgramDataArray()
	{
		List<ShaderInput> inputs = pipeline.getInputs();
		List<Float> list = new ArrayList<>(inputs.size() * 4);
		for (ShaderInput input : inputs)
		{
			float[] data = map.get(input);
			if (data != null) for (float f : data) list.add(f);
			else for (int j = input.getType().getSize(); j > 0; j--) list.add(0F);
		}
		float[] array = new float[list.size()];
		for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
		return array;
	}
	
	public abstract VR end();
	
	@SuppressWarnings("unchecked")
	private VB myself()
	{
		return (VB) this;
	}
}
