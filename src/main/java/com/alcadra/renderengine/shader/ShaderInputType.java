package com.alcadra.renderengine.shader;

import com.alcadra.renderengine.util.WorkInProgress;

@WorkInProgress
public enum ShaderInputType {
	
	BOOL(1),
	INT(4),
	UNSIGNED_INT(4),
	FLOAT(4),
	DOUBLE(8),
	
	VECTOR_BOOL_2(2),
	VECTOR_BOOL_3(3),
	VECTOR_BOOL_4(4),
	VECTOR_INT_2(8),
	VECTOR_INT_3(12),
	VECTOR_INT_4(16),
	VECTOR_UNSIGNED_INT_2(8),
	VECTOR_UNSIGNED_INT_3(12),
	VECTOR_UNSIGNED_INT_4(16),
	VECTOR_FLOAT_2(8),
	VECTOR_FLOAT_3(12),
	VECTOR_FLOAT_4(16),
	VECTOR_DOUBLE_2(16),
	VECTOR_DOUBLE_3(24),
	VECTOR_DOUBLE_4(32),
	
	MATRIX_FLOAT_2X2(16),
	MATRIX_FLOAT_2X3(24),
	MATRIX_FLOAT_2X4(32),
	MATRIX_FLOAT_3X2(24),
	MATRIX_FLOAT_3X3(36),
	MATRIX_FLOAT_3X4(48),
	MATRIX_FLOAT_4X2(32),
	MATRIX_FLOAT_4X3(48),
	MATRIX_FLOAT_4X4(64),
	MATRIX_DOUBLE_2X2(32),
	MATRIX_DOUBLE_2X3(48),
	MATRIX_DOUBLE_2X4(64),
	MATRIX_DOUBLE_3X2(48),
	MATRIX_DOUBLE_3X3(72),
	MATRIX_DOUBLE_3X4(96),
	MATRIX_DOUBLE_4X2(64),
	MATRIX_DOUBLE_4X3(96),
	MATRIX_DOUBLE_4X4(128);
	
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
		
			case "mat2x2" -> MATRIX_FLOAT_2X2;
			case "mat2x3" -> MATRIX_FLOAT_2X3;
			case "mat2x4" -> MATRIX_FLOAT_2X4;
			case "mat3x2" -> MATRIX_FLOAT_3X2;
			case "mat3x3" -> MATRIX_FLOAT_3X3;
			case "mat3x4" -> MATRIX_FLOAT_3X4;
			case "mat4x2" -> MATRIX_FLOAT_4X2;
			case "mat4x3" -> MATRIX_FLOAT_4X3;
			case "mat4x4" -> MATRIX_FLOAT_4X4;
			case "dmat2x2" -> MATRIX_DOUBLE_2X2;
			case "dmat2x3" -> MATRIX_DOUBLE_2X3;
			case "dmat2x4" -> MATRIX_DOUBLE_2X4;
			case "dmat3x2" -> MATRIX_DOUBLE_3X2;
			case "dmat3x3" -> MATRIX_DOUBLE_3X3;
			case "dmat3x4" -> MATRIX_DOUBLE_3X4;
			case "dmat4x2" -> MATRIX_DOUBLE_4X2;
			case "dmat4x3" -> MATRIX_DOUBLE_4X3;
			case "dmat4x4" -> MATRIX_DOUBLE_4X4;
			default -> null;
		};
	}
	
	private int length;
	
	private ShaderInputType(int length)
	{
		this.length = length;
	}
	
	public int getLength()
	{
		return length;
	}
}
