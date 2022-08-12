package com.alcadra.renderengine.shader;

import org.lwjgl.opengl.GL11;

public enum ShaderInputType {
	
	BOOL(GL11.GL_UNSIGNED_BYTE, 1, 4),
	INT(GL11.GL_INT, 1, 4),
	UNSIGNED_INT(GL11.GL_UNSIGNED_INT, 1, 4),
	FLOAT(GL11.GL_FLOAT, 1, 4),
	DOUBLE(GL11.GL_DOUBLE, 1, 8),
	
	VECTOR_BOOL_2(GL11.GL_UNSIGNED_BYTE, 2, 8),
	VECTOR_BOOL_3(GL11.GL_UNSIGNED_BYTE, 3, 12),
	VECTOR_BOOL_4(GL11.GL_UNSIGNED_BYTE, 4, 16),
	VECTOR_INT_2(GL11.GL_INT, 2, 8),
	VECTOR_INT_3(GL11.GL_INT, 3, 12),
	VECTOR_INT_4(GL11.GL_INT, 4, 16),
	VECTOR_UNSIGNED_INT_2(GL11.GL_UNSIGNED_INT, 2, 8),
	VECTOR_UNSIGNED_INT_3(GL11.GL_UNSIGNED_INT, 3, 12),
	VECTOR_UNSIGNED_INT_4(GL11.GL_UNSIGNED_INT, 4, 16),
	VECTOR_FLOAT_2(GL11.GL_FLOAT, 2, 8),
	VECTOR_FLOAT_3(GL11.GL_FLOAT, 3, 12),
	VECTOR_FLOAT_4(GL11.GL_FLOAT, 4, 16),
	VECTOR_DOUBLE_2(GL11.GL_DOUBLE, 2, 16),
	VECTOR_DOUBLE_3(GL11.GL_DOUBLE, 3, 24),
	VECTOR_DOUBLE_4(GL11.GL_DOUBLE, 4, 32),
	
	MATRIX_FLOAT_2X2(GL11.GL_FLOAT, 4, 16),
	MATRIX_FLOAT_2X3(GL11.GL_FLOAT, 6, 24),
	MATRIX_FLOAT_2X4(GL11.GL_FLOAT, 8, 32),
	MATRIX_FLOAT_3X2(GL11.GL_FLOAT, 6, 24),
	MATRIX_FLOAT_3X3(GL11.GL_FLOAT, 9, 36),
	MATRIX_FLOAT_3X4(GL11.GL_FLOAT, 12, 48),
	MATRIX_FLOAT_4X2(GL11.GL_FLOAT, 8, 32),
	MATRIX_FLOAT_4X3(GL11.GL_FLOAT, 12, 48),
	MATRIX_FLOAT_4X4(GL11.GL_FLOAT, 16, 64),
	MATRIX_DOUBLE_2X2(GL11.GL_DOUBLE, 4, 32),
	MATRIX_DOUBLE_2X3(GL11.GL_DOUBLE, 6, 48),
	MATRIX_DOUBLE_2X4(GL11.GL_DOUBLE, 8, 64),
	MATRIX_DOUBLE_3X2(GL11.GL_DOUBLE, 6, 48),
	MATRIX_DOUBLE_3X3(GL11.GL_DOUBLE, 9, 72),
	MATRIX_DOUBLE_3X4(GL11.GL_DOUBLE, 12, 96),
	MATRIX_DOUBLE_4X2(GL11.GL_DOUBLE, 8, 64),
	MATRIX_DOUBLE_4X3(GL11.GL_DOUBLE, 12, 96),
	MATRIX_DOUBLE_4X4(GL11.GL_DOUBLE, 16, 128);
	
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
	
	private ShaderInputType(int type, int size, int length)
	{
		this.type = type;
		this.size = size;
		this.length = length;
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
	 * Returns the number of primitives contained, e.g. for {@link ShaderInputType.VECTOR_INT_3} the value returned is 3.
	 * {@link ShaderType}
	 * 
	 * @return the number of values
	 */
	public int getSize()
	{
		return size;
	}
	
	public int getLength()
	{
		return length;
	}
}
