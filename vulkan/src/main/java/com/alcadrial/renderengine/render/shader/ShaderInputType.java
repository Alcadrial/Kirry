package com.alcadrial.renderengine.render.shader;

import org.lwjgl.vulkan.VK10;

public enum ShaderInputType {
	
	BOOL(VK10.VK_FORMAT_R8_SINT, 1, 4, 1),
	INT(VK10.VK_FORMAT_R32_SINT, 1, 4, 1),
	UNSIGNED_INT(VK10.VK_FORMAT_R32_UINT, 1, 4, 1),
	FLOAT(VK10.VK_FORMAT_R32_SFLOAT, 1, 4, 1),
	DOUBLE(VK10.VK_FORMAT_R64_SFLOAT, 1, 8, 1),
	
	VECTOR_BOOL_2(VK10.VK_FORMAT_R8G8_SINT, 2, 8, 1),
	VECTOR_BOOL_3(VK10.VK_FORMAT_R8G8B8_SINT, 3, 12, 1),
	VECTOR_BOOL_4(VK10.VK_FORMAT_R8G8B8A8_SINT, 4, 16, 1),
	VECTOR_INT_2(VK10.VK_FORMAT_R32G32_SINT, 2, 8, 1),
	VECTOR_INT_3(VK10.VK_FORMAT_R32G32B32_SINT, 3, 12, 1),
	VECTOR_INT_4(VK10.VK_FORMAT_R32G32B32A32_SINT, 4, 16, 1),
	VECTOR_UNSIGNED_INT_2(VK10.VK_FORMAT_R32G32_UINT, 2, 8, 1),
	VECTOR_UNSIGNED_INT_3(VK10.VK_FORMAT_R32G32B32_UINT, 3, 12, 1),
	VECTOR_UNSIGNED_INT_4(VK10.VK_FORMAT_R32G32B32A32_UINT, 4, 16, 1),
	VECTOR_FLOAT_2(VK10.VK_FORMAT_R32G32_SFLOAT, 2, 8, 1),
	VECTOR_FLOAT_3(VK10.VK_FORMAT_R32G32B32_SFLOAT, 3, 12, 1),
	VECTOR_FLOAT_4(VK10.VK_FORMAT_R32G32B32A32_SFLOAT, 4, 16, 1),
	VECTOR_DOUBLE_2(VK10.VK_FORMAT_R64G64_SFLOAT, 2, 16, 1),
	VECTOR_DOUBLE_3(VK10.VK_FORMAT_R64G64B64_SFLOAT, 3, 24, 2),
	VECTOR_DOUBLE_4(VK10.VK_FORMAT_R64G64B64A64_SFLOAT, 4, 32, 2),
	
	MATRIX_FLOAT_2X2(VK10.VK_FORMAT_R32G32_SFLOAT, 4, 16, 2),
	MATRIX_FLOAT_2X3(VK10.VK_FORMAT_R32G32_SFLOAT, 6, 24, 3),
	MATRIX_FLOAT_2X4(VK10.VK_FORMAT_R32G32_SFLOAT, 8, 32, 4),
	MATRIX_FLOAT_3X2(VK10.VK_FORMAT_R32G32B32_SFLOAT, 6, 24, 2),
	MATRIX_FLOAT_3X3(VK10.VK_FORMAT_R32G32B32_SFLOAT, 9, 36, 3),
	MATRIX_FLOAT_3X4(VK10.VK_FORMAT_R32G32B32_SFLOAT, 12, 48, 4),
	MATRIX_FLOAT_4X2(VK10.VK_FORMAT_R32G32B32A32_SFLOAT, 8, 32, 2),
	MATRIX_FLOAT_4X3(VK10.VK_FORMAT_R32G32B32A32_SFLOAT, 12, 48, 3),
	MATRIX_FLOAT_4X4(VK10.VK_FORMAT_R32G32B32A32_SFLOAT, 16, 64, 4),
	MATRIX_DOUBLE_2X2(VK10.VK_FORMAT_R64G64_SFLOAT, 4, 32, 2),
	MATRIX_DOUBLE_2X3(VK10.VK_FORMAT_R64G64_SFLOAT, 6, 48, 3),
	MATRIX_DOUBLE_2X4(VK10.VK_FORMAT_R64G64_SFLOAT, 8, 64, 4),
	MATRIX_DOUBLE_3X2(VK10.VK_FORMAT_R64G64B64_SFLOAT, 6, 48, 4),
	MATRIX_DOUBLE_3X3(VK10.VK_FORMAT_R64G64B64_SFLOAT, 9, 72, 6),
	MATRIX_DOUBLE_3X4(VK10.VK_FORMAT_R64G64B64_SFLOAT, 12, 96, 8),
	MATRIX_DOUBLE_4X2(VK10.VK_FORMAT_R64G64B64A64_SFLOAT, 8, 64, 4),
	MATRIX_DOUBLE_4X3(VK10.VK_FORMAT_R64G64B64A64_SFLOAT, 12, 96, 6),
	MATRIX_DOUBLE_4X4(VK10.VK_FORMAT_R64G64B64A64_SFLOAT, 16, 128, 8);
	
	public static ShaderInputType of(String typeName)
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
		
			default -> null;
		};
	}
	
	private final int type;
	private final int size;
	private final int length;
	private final int locationSize;
	
	ShaderInputType(int type, int size, int length, int locationSize)
	{
		this.type = type;
		this.size = size;
		this.length = length;
		this.locationSize = locationSize;
	}
	
	/**
	 * Returns the primitive type of data of this input and can be unsigned byte, int, unsigned int, float or double.
	 * 
	 * @return the type of data
	 */
	public int getType()
	{
		return type;
	}
	
	/**
	 * Returns the number of primitives contained, e.g. for {@link VECTOR_INT_3} the value returned is 3.
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
}
