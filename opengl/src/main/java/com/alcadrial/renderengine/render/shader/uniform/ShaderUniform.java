package com.alcadrial.renderengine.render.shader.uniform;

import java.util.Objects;

import com.alcadrial.kector.matrix.Matrix3f;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.core.util.NotYetImplemented;

public abstract class ShaderUniform<T> {
	
	public static ShaderUniform<Integer> ofInt(String name)
	{
		return new IntUniform(name);
	}
	
	public static ShaderUniform<Float> ofFloat(String name)
	{
		return new FloatUniform(name);
	}
	
	public static ShaderUniform<Vector2i> ofVector2i(String name)
	{
		return new Vector2iUniform(name);
	}
	
	public static ShaderUniform<Vector2f> ofVector2f(String name)
	{
		return new Vector2fUniform(name);
	}
	
	public static ShaderUniform<Vector3i> ofVector3i(String name)
	{
		return new Vector3iUniform(name);
	}
	
	public static ShaderUniform<Vector3f> ofVector3f(String name)
	{
		return new Vector3fUniform(name);
	}
	
	public static ShaderUniform<Vector4i> ofVector4i(String name)
	{
		return new Vector4iUniform(name);
	}
	
	public static ShaderUniform<Vector4f> ofVector4f(String name)
	{
		return new Vector4fUniform(name);
	}
	
	@NotYetImplemented
	private static ShaderUniform<Void> ofMatrix2f(String name)
	{
		return new Matrix2fUniform(name);
	}
	
	public static ShaderUniform<Matrix3f> ofMatrix3f(String name)
	{
		return new Matrix3fUniform(name);
	}
	
	public static ShaderUniform<Matrix4f> ofMatrix4f(String name)
	{
		return new Matrix4fUniform(name);
	}
	
	private String name;
	
	protected ShaderUniform(String name)
	{
		this.name = Objects.requireNonNull(name, "ShaderUniform name must not be null!");
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
