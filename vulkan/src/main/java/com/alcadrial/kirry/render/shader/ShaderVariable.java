package com.alcadrial.kirry.render.shader;

import java.io.IOException;
import java.util.Map;

import org.eclipse.collections.api.factory.Maps;
import org.jspecify.annotations.Nullable;

import tools.jackson.databind.JsonNode;

public class ShaderVariable {

	private static Map<String, Integer> nextId = Maps.mutable.empty();
	
	public static final ShaderVariable PADDING_1 = ofPadding(ShaderInputType.INT);
	public static final ShaderVariable PADDING_2 = ofPadding(ShaderInputType.VECTOR_INT_2);
	public static final ShaderVariable PADDING_3 = ofPadding(ShaderInputType.VECTOR_INT_3);
	public static final ShaderVariable PADDING_4 = ofPadding(ShaderInputType.VECTOR_INT_4);
	public static final ShaderVariable VERTEX_POSITION = of(ShaderInputType.VECTOR_FLOAT_3, "vertexPosition");
	public static final ShaderVariable COLOR_RGB = of(ShaderInputType.VECTOR_FLOAT_3, "colorRGB");
	public static final ShaderVariable COLOR_RGB_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_3, "colorRGB1");
	public static final ShaderVariable COLOR_RGBA = of(ShaderInputType.VECTOR_FLOAT_4, "color");
	public static final ShaderVariable COLOR_RGBA_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_4, "color1");
	public static final ShaderVariable COLOR = COLOR_RGBA;
	public static final ShaderVariable UV = of(ShaderInputType.VECTOR_FLOAT_2, "uv");
	public static final ShaderVariable UV_EXTRA_1 = of(ShaderInputType.VECTOR_FLOAT_2, "uv1");
	public static final ShaderVariable UV_EXTRA_2 = of(ShaderInputType.VECTOR_FLOAT_2, "uv2");
	public static final ShaderVariable UVUV = of(ShaderInputType.VECTOR_FLOAT_4, "uvuv");
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

	public static final ShaderVariable OFFSET = of(ShaderInputType.INT, "offset");
	public static final ShaderVariable OFFSET_1 = of(ShaderInputType.INT, "offset1");
	public static final ShaderVariable OFFSET_2 = of(ShaderInputType.INT, "offset2");
	
	public static final ShaderVariable STRIDE = of(ShaderInputType.INT, "stride");
	public static final ShaderVariable STRIDE_1 = of(ShaderInputType.INT, "stride1");
	public static final ShaderVariable STRIDE_2 = of(ShaderInputType.INT, "stride2");
	
	public static final ShaderVariable SIZE = of(ShaderInputType.INT, "size");
	public static final ShaderVariable SIZE_1 = of(ShaderInputType.INT, "size1");
	public static final ShaderVariable SIZE_2 = of(ShaderInputType.INT, "size2");
	
	public static final ShaderVariable TEXTURE = of(ShaderInputType.SAMPLER_2D, "TEXTURE_SAMPLER");
	public static final ShaderVariable ATLAS = of(ShaderInputType.SAMPLER_2D, "ATLAS");

	public static ShaderVariable parse(JsonNode json) throws IOException
	{
		return parse(json, "$");
	}
	
	public static ShaderVariable parse(JsonNode json, String location) throws IOException
	{
		String[] declaration = json.requireNonNull().stringValue().split(" ");
		if (declaration.length != 2) throw new IOException("Invalid variable definition of " + location);
		
		ShaderInputType type = ShaderInputType.of(declaration[0].trim());
		if (type == null) throw new IOException("Unknown variable type of " + location);
		
		return of(type, declaration[1].trim());
	}
	
	public static ShaderVariable of(ShaderInputType type, String name)
	{
		return new ShaderVariable(type, name, nextId.computeIfAbsent(name, _ -> nextId.size()));
	}
	
	private static ShaderVariable ofPadding(ShaderInputType type)
	{
		return new ShaderVariable(type, "", -1);
	}
	
	private ShaderInputType type;
	private String name;
	private int id;
	
	ShaderVariable(ShaderInputType type, String name, int id)
	{
		this.type = type;
		this.name = name;
		this.id = id;
	}
	
	public ShaderInputType getType()
	{
		return type;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getId()
	{
		return id;
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		return this == obj || obj instanceof ShaderVariable s && equals(s);
	}
	
	public boolean equals(ShaderVariable variable)
	{
		return id == variable.id;
	}
	
	@Override
	public String toString()
	{
		if (id < 0) return "padding" + type.getSize();
		return type + " " + name;
	}
}
