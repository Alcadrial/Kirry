package com.alcadrial.renderengine.render.shader;

import java.util.Objects;

import com.alcadrial.renderengine.render.RenderEngine;

public class ShaderVariable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static final ShaderVariable PADDING_1 = of(ShaderInputType.INT, "");
	public static final ShaderVariable PADDING_2 = of(ShaderInputType.VECTOR_INT_2, "");
	public static final ShaderVariable PADDING_3 = of(ShaderInputType.VECTOR_INT_3, "");
	public static final ShaderVariable PADDING_4 = of(ShaderInputType.VECTOR_INT_4, "");
	public static final ShaderVariable VERTEX_POSITION = of(ShaderInputType.VECTOR_FLOAT_3, "vertexPosition");
	public static final ShaderVariable COLOR_RGB = of(ShaderInputType.VECTOR_FLOAT_3, "colorRGB");
	public static final ShaderVariable COLOR_RGB_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_3, "colorRGB1");
	public static final ShaderVariable COLOR_RGBA = of(ShaderInputType.VECTOR_FLOAT_4, "color");
	public static final ShaderVariable COLOR_RGBA_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_4, "color1");
	public static final ShaderVariable COLOR = COLOR_RGBA;
	public static final ShaderVariable UV = of(ShaderInputType.VECTOR_FLOAT_2, "uv");
	public static final ShaderVariable UV_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_2, "uv1");
	public static final ShaderVariable UV_EXTRA_2 = of(ShaderInputType.VECTOR_FLOAT_2, "uv2");
	public static final ShaderVariable NORMAL_POSITION = of(ShaderInputType.VECTOR_FLOAT_3, "normalPosition");
	public static final ShaderVariable NORMAL_POSITION_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_3, "normalPosition1");
	public static final ShaderVariable POSITION = of(ShaderInputType.VECTOR_FLOAT_3, "position");
	public static final ShaderVariable POSITION_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_3, "position1");
	public static final ShaderVariable POSITION_EXTRA_2 = of(ShaderInputType.VECTOR_FLOAT_3, "position2");
	public static final ShaderVariable VELOCITY = of(ShaderInputType.VECTOR_FLOAT_3, "velocity");
	public static final ShaderVariable VELOCITY_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_3, "velocity1");
	public static final ShaderVariable VELOCITY_EXTRA_2 = of(ShaderInputType.VECTOR_FLOAT_3, "velocity2");
	public static final ShaderVariable ACCELERATION = of(ShaderInputType.VECTOR_FLOAT_3, "acceleration");
	public static final ShaderVariable ACCELERATION_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_3, "acceleration1");
	public static final ShaderVariable ACCELERATION_EXTRA_2 = of(ShaderInputType.VECTOR_FLOAT_3, "acceleration2");
	
	public static final ShaderVariable MATRIX = of(ShaderInputType.MATRIX_FLOAT_4X4, "MATRIX");
	public static final ShaderVariable PROJECTION = of(ShaderInputType.MATRIX_FLOAT_4X4, "PROJECTION");
	public static final ShaderVariable VIEW = of(ShaderInputType.MATRIX_FLOAT_4X4, "VIEW");
	public static final ShaderVariable MODEL = of(ShaderInputType.MATRIX_FLOAT_4X4, "MODEL");
	public static final ShaderVariable TRANSFORMATION = of(ShaderInputType.MATRIX_FLOAT_4X4, "TRANSFORMATION");
	
	public static ShaderVariable of(ShaderInputType type, String name)
	{
		return new ShaderVariable(type, name);
	}
	
	private ShaderInputType type;
	private String name;
	
	public ShaderVariable(ShaderInputType type, String name)
	{
		this.type = Objects.requireNonNull(type, "ShaderVariable type must not be null");
		this.name = Objects.requireNonNull(name, "ShaderVariable name must not be null");
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
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this == obj || obj instanceof ShaderVariable s && equals(s);
	}
	
	public boolean equals(ShaderVariable uniform)
	{
		return name.equals(uniform.name);
	}
	
	@Override
	public String toString()
	{
		if (name.isBlank()) return "padding" + type.getSize();
		return type + " " + name;
	}
}
