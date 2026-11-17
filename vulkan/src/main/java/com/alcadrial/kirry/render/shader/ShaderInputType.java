package com.alcadrial.kirry.render.shader;

import org.jspecify.annotations.Nullable;
import org.lwjgl.vulkan.VK10;

public enum ShaderInputType {
	
	BOOL(VK10.VK_FORMAT_R8_UINT, VK10.VK_FORMAT_R8_UINT, 1, 4, 1, "bool"),
	INT(VK10.VK_FORMAT_R32_SINT, VK10.VK_FORMAT_R32_SINT, 1, 4, 1, "int"),
	UNSIGNED_INT(VK10.VK_FORMAT_R32_UINT, VK10.VK_FORMAT_R32_UINT, 1, 4, 1, "uint"),
	LONG(VK10.VK_FORMAT_R64_SINT, VK10.VK_FORMAT_R64_SINT, 1, 8, 1, "long"),
	UNSIGNED_LONG(VK10.VK_FORMAT_R64_UINT, VK10.VK_FORMAT_R64_UINT, 1, 8, 1, "ulong"),
	FLOAT(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32_SFLOAT, 1, 4, 1, "float"),
	DOUBLE(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64_SFLOAT, 1, 8, 1, "double"),
	
	VECTOR_BOOL_2(VK10.VK_FORMAT_R8_UINT, VK10.VK_FORMAT_R8G8_UINT, 2, 8, 1, "bvec2"),
	VECTOR_BOOL_3(VK10.VK_FORMAT_R8_UINT, VK10.VK_FORMAT_R8G8B8_UINT, 3, 12, 1, "bvec3"),
	VECTOR_BOOL_4(VK10.VK_FORMAT_R8_UINT, VK10.VK_FORMAT_R8G8B8A8_UINT, 4, 16, 1, "bvec4"),
	VECTOR_INT_2(VK10.VK_FORMAT_R32_SINT, VK10.VK_FORMAT_R32G32_SINT, 2, 8, 1, "ivec2"),
	VECTOR_INT_3(VK10.VK_FORMAT_R32_SINT, VK10.VK_FORMAT_R32G32B32_SINT, 3, 12, 1, "ivec3"),
	VECTOR_INT_4(VK10.VK_FORMAT_R32_SINT, VK10.VK_FORMAT_R32G32B32A32_SINT, 4, 16, 1, "ivec4"),
	VECTOR_UNSIGNED_INT_2(VK10.VK_FORMAT_R32_UINT, VK10.VK_FORMAT_R32G32_UINT, 2, 8, 1, "uvec2"),
	VECTOR_UNSIGNED_INT_3(VK10.VK_FORMAT_R32_UINT, VK10.VK_FORMAT_R32G32B32_UINT, 3, 12, 1, "uvec3"),
	VECTOR_UNSIGNED_INT_4(VK10.VK_FORMAT_R32_UINT, VK10.VK_FORMAT_R32G32B32A32_UINT, 4, 16, 1, "uvec4"),
	VECTOR_LONG_2(VK10.VK_FORMAT_R64_SINT, VK10.VK_FORMAT_R64G64_SINT, 2, 16, 1, "lvec2"),
	VECTOR_LONG_3(VK10.VK_FORMAT_R64_SINT, VK10.VK_FORMAT_R64G64B64_SINT, 3, 24, 2, "lvec3"),
	VECTOR_LONG_4(VK10.VK_FORMAT_R64_SINT, VK10.VK_FORMAT_R64G64B64A64_SINT, 4, 32, 2, "lvec4"),
	VECTOR_FLOAT_2(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32_SFLOAT, 2, 8, 1, "vec2"),
	VECTOR_FLOAT_3(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32B32_SFLOAT, 3, 12, 1, "vec3"),
	VECTOR_FLOAT_4(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32B32A32_SFLOAT, 4, 16, 1, "vec4"),
	VECTOR_DOUBLE_2(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64_SFLOAT, 2, 16, 1, "dvec2"),
	VECTOR_DOUBLE_3(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64B64_SFLOAT, 3, 24, 2, "dvec3"),
	VECTOR_DOUBLE_4(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64B64A64_SFLOAT, 4, 32, 2, "dvec4"),
	
	MATRIX_FLOAT_2X2(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32_SFLOAT, 4, 16, 2, "mat2x2"),
	MATRIX_FLOAT_2X3(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32_SFLOAT, 6, 24, 3, "mat2x3"),
	MATRIX_FLOAT_2X4(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32_SFLOAT, 8, 32, 4, "mat2x4"),
	MATRIX_FLOAT_3X2(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32B32_SFLOAT, 6, 24, 2, "mat3x2"),
	MATRIX_FLOAT_3X3(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32B32_SFLOAT, 9, 36, 3, "mat3x3"),
	MATRIX_FLOAT_3X4(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32B32_SFLOAT, 12, 48, 4, "mat3x4"),
	MATRIX_FLOAT_4X2(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32B32A32_SFLOAT, 8, 32, 2, "mat4x2"),
	MATRIX_FLOAT_4X3(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32B32A32_SFLOAT, 12, 48, 3, "mat4x3"),
	MATRIX_FLOAT_4X4(VK10.VK_FORMAT_R32_SFLOAT, VK10.VK_FORMAT_R32G32B32A32_SFLOAT, 16, 64, 4, "mat4x4"),
	MATRIX_DOUBLE_2X2(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64_SFLOAT, 4, 32, 2, "dmat2x2"),
	MATRIX_DOUBLE_2X3(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64_SFLOAT, 6, 48, 3, "dmat2x3"),
	MATRIX_DOUBLE_2X4(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64_SFLOAT, 8, 64, 4, "dmat2x4"),
	MATRIX_DOUBLE_3X2(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64B64_SFLOAT, 6, 48, 4, "dmat3x2"),
	MATRIX_DOUBLE_3X3(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64B64_SFLOAT, 9, 72, 6, "dmat3x3"),
	MATRIX_DOUBLE_3X4(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64B64_SFLOAT, 12, 96, 8, "dmat3x4"),
	MATRIX_DOUBLE_4X2(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64B64A64_SFLOAT, 8, 64, 4, "dmat4x2"),
	MATRIX_DOUBLE_4X3(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64B64A64_SFLOAT, 12, 96, 6, "dmat4x3"),
	MATRIX_DOUBLE_4X4(VK10.VK_FORMAT_R64_SFLOAT, VK10.VK_FORMAT_R64G64B64A64_SFLOAT, 16, 128, 8, "dmat4x4"),
	SAMPLER_2D(VK10.VK_FORMAT_R32_UINT, VK10.VK_FORMAT_R32_UINT, 1, 4, 1, "sampler2D");
	
	public static @Nullable ShaderInputType of(String typeName)
	{
		if (typeName.isBlank()) return null;
		return switch (typeName)
		{
			case "bool" -> BOOL;
			case "int" -> INT;
			case "uint" -> UNSIGNED_INT;
			case "float" -> FLOAT;
			case "double" -> DOUBLE;
		
			case "bvec2" -> VECTOR_BOOL_2;
			case "bvec3" -> VECTOR_BOOL_3;
			case "bvec4" -> VECTOR_BOOL_4;
			case "ivec2" -> VECTOR_INT_2;
			case "ivec3" -> VECTOR_INT_3;
			case "ivec4" -> VECTOR_INT_4;
			case "uvec2" -> VECTOR_UNSIGNED_INT_2;
			case "uvec3" -> VECTOR_UNSIGNED_INT_3;
			case "uvec4" -> VECTOR_UNSIGNED_INT_4;
			case "vec2" -> VECTOR_FLOAT_2;
			case "vec3" -> VECTOR_FLOAT_3;
			case "vec4" -> VECTOR_FLOAT_4;
			case "dvec2" -> VECTOR_DOUBLE_2;
			case "dvec3" -> VECTOR_DOUBLE_3;
			case "dvec4" -> VECTOR_DOUBLE_4;
		
			case "mat2x2", "mat2" -> MATRIX_FLOAT_2X2;
			case "mat2x3" -> MATRIX_FLOAT_2X3;
			case "mat2x4" -> MATRIX_FLOAT_2X4;
			case "mat3x2" -> MATRIX_FLOAT_3X2;
			case "mat3x3", "mat3" -> MATRIX_FLOAT_3X3;
			case "mat3x4" -> MATRIX_FLOAT_3X4;
			case "mat4x2" -> MATRIX_FLOAT_4X2;
			case "mat4x3" -> MATRIX_FLOAT_4X3;
			case "mat4x4", "mat4" -> MATRIX_FLOAT_4X4;
		
			case "dmat2x2", "dmat2" -> MATRIX_DOUBLE_2X2;
			case "dmat2x3" -> MATRIX_DOUBLE_2X3;
			case "dmat2x4" -> MATRIX_DOUBLE_2X4;
			case "dmat3x2" -> MATRIX_DOUBLE_3X2;
			case "dmat3x3", "dmat3" -> MATRIX_DOUBLE_3X3;
			case "dmat3x4" -> MATRIX_DOUBLE_3X4;
			case "dmat4x2" -> MATRIX_DOUBLE_4X2;
			case "dmat4x3" -> MATRIX_DOUBLE_4X3;
			case "dmat4x4", "dmat4" -> MATRIX_DOUBLE_4X4;
		
			case "sampler2D" -> SAMPLER_2D;
		
			default -> null;
		};
	}
	
	public static @Nullable ShaderInputType ofMatrix(boolean doublePrecision, int columns, int rows)
	{
		return switch (columns + rows * 4 + (doublePrecision ? 16 : 0))
		{
			case 5 -> MATRIX_FLOAT_2X2;
			case 6 -> MATRIX_FLOAT_2X3;
			case 7 -> MATRIX_FLOAT_2X4;
			case 9 -> MATRIX_FLOAT_3X2;
			case 10 -> MATRIX_FLOAT_3X3;
			case 11 -> MATRIX_FLOAT_3X4;
			case 13 -> MATRIX_FLOAT_4X2;
			case 14 -> MATRIX_FLOAT_4X3;
			case 15 -> MATRIX_FLOAT_4X4;
		
			case 21 -> MATRIX_DOUBLE_2X2;
			case 22 -> MATRIX_DOUBLE_2X3;
			case 23 -> MATRIX_DOUBLE_2X4;
			case 25 -> MATRIX_DOUBLE_3X2;
			case 26 -> MATRIX_DOUBLE_3X3;
			case 27 -> MATRIX_DOUBLE_3X4;
			case 29 -> MATRIX_DOUBLE_4X2;
			case 30 -> MATRIX_DOUBLE_4X3;
			case 31 -> MATRIX_DOUBLE_4X4;
			default -> null;
		};
	}
	
	private final int primitiveType;
	private final int type;
	private final int size;
	private final int length;
	private final int locationSize;
	private final String name;
	
	ShaderInputType(int primitiveType, int type, int size, int length, int locationSize, String name)
	{
		this.primitiveType = primitiveType;
		this.type = type;
		this.size = size;
		this.length = length;
		this.locationSize = locationSize;
		this.name = name;
	}
	
	/**
	 * Returns the primitive type of data of this input and can be unsigned byte, int, unsigned int, float or double.
	 * <p>
	 * Possible values are:
	 * <ul>
	 * <li>{@link VK10#VK_FORMAT_R8_UINT}</li>
	 * <li>{@link VK10#VK_FORMAT_R32_SINT}</li>
	 * <li>{@link VK10#VK_FORMAT_R32_UINT}</li>
	 * <li>{@link VK10#VK_FORMAT_R32_SFLOAT}</li>
	 * <li>{@link VK10#VK_FORMAT_R64_SFLOAT}</li>
	 * </ul>
	 * 
	 * @return the type of data
	 */
	public int getPrimitiveType()
	{
		return primitiveType;
	}
	
	/**
	 * Returns the primitive type of data of this input and can be unsigned byte, int, unsigned int, float or double.
	 * 
	 * @return the type of data
	 */
	public int getVulkanType()
	{
		return type;
	}
	
	/**
	 * Returns the number of primitives contained, e.g. for {@link #VECTOR_INT_3} the value returned is 3.
	 * 
	 * @return the number of values
	 */
	public int getSize()
	{
		return size;
	}
	
	/**
	 * Returns the length in bytes of this type.
	 * 
	 * @return the length in bytes
	 */
	public int getLength()
	{
		return length;
	}
	
	/**
	 * Returns the size of the location in the shader of this type.
	 * 
	 * @return the locations used
	 */
	public int getLocationSize()
	{
		return locationSize;
	}
	
	/**
	 * Returns the name (glsl for non long-based types) of this type.
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	private boolean checkRange(ShaderInputType start, ShaderInputType end)
	{
		return start.compareTo(this) <= 0 && compareTo(end) <= 0;
	}
	
	public boolean isMatrix()
	{
		return checkRange(MATRIX_FLOAT_2X2, MATRIX_DOUBLE_4X4);
	}
	
	public boolean isSquareMatrix()
	{
		return this == MATRIX_FLOAT_2X2 || this == MATRIX_FLOAT_3X3 || this == MATRIX_FLOAT_4X4 || this == MATRIX_DOUBLE_2X2 || this == MATRIX_DOUBLE_3X3 || this == MATRIX_DOUBLE_4X4;
	}
	
	public boolean isVector()
	{
		return checkRange(VECTOR_BOOL_2, VECTOR_DOUBLE_4);
	}
	
	public boolean isFloatVector()
	{
		return checkRange(VECTOR_FLOAT_2, VECTOR_DOUBLE_4);
	}
	
	public boolean isIntegerVector()
	{
		return checkRange(VECTOR_INT_2, VECTOR_LONG_4);
	}
	
	public boolean isScalar()
	{
		return checkRange(BOOL, DOUBLE);
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
