package com.alcadrial.renderengine.render.shader;

import java.util.Objects;

public class ShaderUniform {
	
	private String name;
	private ShaderInputType type;
	
	public ShaderUniform(ShaderInputType type, String name)
	{
		this.name = Objects.requireNonNull(name, "ShaderUniform name must not be null");
		this.type = Objects.requireNonNull(type, "ShaderUniform type must not be null");
	}
	
	public String getName()
	{
		return name;
	}
	
	public ShaderInputType getType()
	{
		return type;
	}
	
	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof ShaderUniform s && equals(s);
	}
	
	public boolean equals(ShaderUniform uniform)
	{
		return name.equals(uniform.name);
	}
	
	@Override
	public String toString()
	{
		return "ShaderUniform{" + type + " " + name + "}";
	}
}
