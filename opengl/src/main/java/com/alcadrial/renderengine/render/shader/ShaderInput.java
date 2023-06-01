package com.alcadrial.renderengine.render.shader;

import java.util.Objects;

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
		Objects.requireNonNull(name, "ShaderInput name must not be null!");
		if (name.isBlank()) throw new IllegalArgumentException("ShaderInput name must not be blank!");
		return new ShaderInput(Objects.requireNonNull(type, "ShaderInput type must not be null!"), name);
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
		return 31 * result + (name == null ? 0 : name.hashCode());
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
