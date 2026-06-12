package com.alcadrial.kirry.render.shader.layout;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.shader.ShaderVariable;

public abstract sealed class ShaderBufferLayout extends ShaderLayout permits ShaderUniformBufferLayout, ShaderStorageBufferLayout, ShaderCombinedImageSamplerLayout {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	private final int binding;
	private final @Nullable String name;
	private final List<ShaderVariable> variables;
	
	protected ShaderBufferLayout(Type type, int binding, @Nullable String name, int descriptorCount, List<ShaderVariable> variables)
	{
		super(type, descriptorCount);
		this.binding = binding;
		this.name = name;
		this.variables = List.copyOf(variables);
	}
	
	public int getBinding()
	{
		return binding;
	}
	
	public @Nullable String getName()
	{
		return name;
	}
	
	public List<ShaderVariable> getVariables()
	{
		return variables;
	}
}
