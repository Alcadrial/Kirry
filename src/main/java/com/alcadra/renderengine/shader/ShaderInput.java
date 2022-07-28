package com.alcadra.renderengine.shader;

import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.math.vector.Vector4i;
import com.alcadra.renderengine.util.WorkInProgress;

@WorkInProgress
public class ShaderInput<I> {
	
	public static final String VERTEX_POSITION = "vertexPosition";
	public static final String UV = "uv";
	public static final String UV_EXTRA_1 = "uv1";
	public static final String UV_EXTRA_2 = "uv2";
	public static final String NORMAL_POSITION = "normalPosition";
	public static final String POSITION = "position";
	public static final String POSITION_EXTRA_1 = "position1";
	public static final String POSITION_EXTRA_2 = "position2";
	
	public static ShaderInput<Byte> of(String name, byte value)
	{
		return new ShaderInput<>(name, value, 1);
	}
	
	public static ShaderInput<Short> of(String name, short value)
	{
		return new ShaderInput<>(name, value, 2);
	}
	
	public static ShaderInput<Character> of(String name, char value)
	{
		return new ShaderInput<>(name, value, 2);
	}
	
	public static ShaderInput<Integer> of(String name, int value)
	{
		return new ShaderInput<>(name, value, 4);
	}
	
	public static ShaderInput<Float> of(String name, float value)
	{
		return new ShaderInput<>(name, value, 4);
	}
	
	public static ShaderInput<Long> of(String name, long value)
	{
		return new ShaderInput<>(name, value, 8);
	}
	
	public static ShaderInput<Double> of(String name, double value)
	{
		return new ShaderInput<>(name, value, 8);
	}
	
	public static ShaderInput<Vector2i> of(String name, Vector2i value)
	{
		return new ShaderInput<>(name, value, 8);
	}
	
	public static ShaderInput<Vector2f> of(String name, Vector2f value)
	{
		return new ShaderInput<>(name, value, 8);
	}
	
	public static ShaderInput<Vector3i> of(String name, Vector3i value)
	{
		return new ShaderInput<>(name, value, 12);
	}
	
	public static ShaderInput<Vector3f> of(String name, Vector3f value)
	{
		return new ShaderInput<>(name, value, 12);
	}
	
	public static ShaderInput<Vector4i> of(String name, Vector4i value)
	{
		return new ShaderInput<>(name, value, 16);
	}
	
	public static ShaderInput<Vector4f> of(String name, Vector4f value)
	{
		return new ShaderInput<>(name, value, 16);
	}
	
	private String name;
	private I input;
	private int length;
	
	ShaderInput(String name, I input, int length)
	{
		this.name = name;
		this.input = input;
		this.length = length;
	}
	
	public String getName()
	{
		return name;
	}
	
	public I getInput()
	{
		return input;
	}
	
	public int getLength()
	{
		return length;
	}
}
