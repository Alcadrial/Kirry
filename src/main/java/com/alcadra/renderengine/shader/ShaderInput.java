package com.alcadra.renderengine.shader;

import java.util.Objects;

import com.alcadra.renderengine.util.WorkInProgress;

@WorkInProgress
public class ShaderInput {
	
	public static final ShaderInput VERTEX_POSITION = of(ShaderInputType.VECTOR_FLOAT_3, "vertexPosition");
	public static final ShaderInput COLOR_RGB = of(ShaderInputType.VECTOR_FLOAT_3, "colorRGB");
	public static final ShaderInput COLOR_RGBA = of(ShaderInputType.VECTOR_FLOAT_4, "color");
	public static final ShaderInput COLOR = COLOR_RGBA;
	public static final ShaderInput UV = of(ShaderInputType.VECTOR_FLOAT_2, "uv");
	public static final ShaderInput UV_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_2, "uv1");
	public static final ShaderInput UV_EXTRA_2 = of(ShaderInputType.VECTOR_FLOAT_2, "uv2");
	public static final ShaderInput NORMAL_POSITION = of(ShaderInputType.VECTOR_FLOAT_3, "normalPosition");
	public static final ShaderInput POSITION = of(ShaderInputType.VECTOR_FLOAT_3, "position");
	public static final ShaderInput POSITION_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_3, "position1");
	public static final ShaderInput POSITION_EXTRA_2 = of(ShaderInputType.VECTOR_FLOAT_3, "position2");
	
	public static ShaderInput of(ShaderInputType type, String name)
	{
		Objects.requireNonNull(name);
		if (name.isBlank()) throw new IllegalArgumentException("ShaderInput name cannot be blank!");
		return new ShaderInput(Objects.requireNonNull(type), name);
	}
	
	private ShaderInputType type;
	private String name;
	
	ShaderInput(ShaderInputType type, String name)
	{
		this.type = type;
		this.name = name;
	}
	
	public ShaderInputType getType()
	{
		return type;
	}
	
	public String getName()
	{
		return name;
	}
	
	@Override
	public int hashCode()
	{
		int result = 1;
		result = 31 * result + (type == null ? 0 : type.hashCode());
		result = 31 * result + (name == null ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this == obj || obj instanceof ShaderInput input && equals(input);
	}
	
	public boolean equals(ShaderInput input)
	{
		return type == input.type && name.equals(input.name);
	}
	
	@Override
	public String toString()
	{
		return type + " " + name;
	}
}
