package com.alcadra.renderengine.shader.uniform;

import java.util.Objects;

import com.alcadra.math.Matrix3f;
import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.math.vector.Vector4i;
import com.alcadra.renderengine.util.Nonnull;
import com.alcadra.renderengine.util.NotYetImplemented;

@Nonnull
public abstract class ShaderUniform<T> {
	
	public static ShaderUniform<Integer> ofInt(String name)
	{
		return new IntUniform(Objects.requireNonNull(name));
	}
	
	public static ShaderUniform<Float> ofFloat(String name)
	{
		return new FloatUniform(Objects.requireNonNull(name));
	}
	
	public static ShaderUniform<Vector2i> ofVector2i(String name)
	{
		return new Vector2iUniform(Objects.requireNonNull(name));
	}
	
	public static ShaderUniform<Vector2f> ofVector2f(String name)
	{
		return new Vector2fUniform(Objects.requireNonNull(name));
	}
	
	public static ShaderUniform<Vector3i> ofVector3i(String name)
	{
		return new Vector3iUniform(Objects.requireNonNull(name));
	}
	
	public static ShaderUniform<Vector3f> ofVector3f(String name)
	{
		return new Vector3fUniform(Objects.requireNonNull(name));
	}
	
	public static ShaderUniform<Vector4i> ofVector4i(String name)
	{
		return new Vector4iUniform(Objects.requireNonNull(name));
	}
	
	public static ShaderUniform<Vector4f> ofVector4f(String name)
	{
		return new Vector4fUniform(Objects.requireNonNull(name));
	}
	
	@NotYetImplemented
	private static ShaderUniform<Void> ofMatrix2f(String name)
	{
		return new Matrix2fUniform(Objects.requireNonNull(name));
	}
	
	public static ShaderUniform<Matrix3f> ofMatrix3f(String name)
	{
		return new Matrix3fUniform(Objects.requireNonNull(name));
	}
	
	public static ShaderUniform<Matrix4f> ofMatrix4f(String name)
	{
		return new Matrix4fUniform(Objects.requireNonNull(name));
	}
	
	private String name;
	
	ShaderUniform(String name)
	{
		this.name = name;
	}
	
	public abstract void setValue(int location, T value);
	
	public String getName()
	{
		return name;
	}
	
	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof ShaderUniform s && name.equals(s.name);
	}
	
	@Override
	public String toString()
	{
		return "ShaderUniform{name=" + name + "}";
	}
}
