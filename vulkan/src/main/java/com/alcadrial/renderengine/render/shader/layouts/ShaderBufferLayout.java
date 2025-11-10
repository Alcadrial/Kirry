package com.alcadrial.renderengine.render.shader.layouts;

import java.util.List;

import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.ShaderVariable;

public abstract sealed class ShaderBufferLayout extends ShaderLayout permits ShaderUniformBufferLayout, ShaderStorageBufferLayout, ShaderCombinedImageSamplerLayout {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private final int binding;
	private final @Nullable String name;
	private final @Nullable List<ShaderVariable> variables;
	
	protected ShaderBufferLayout(Type type, int binding, @Nullable String name, @Nullable List<ShaderVariable> variables)
	{
		super(type);
		this.binding = binding;
		this.name = name;
		this.variables = variables != null ? List.copyOf(variables) : null;
	}
	
	public int getBinding()
	{
		return binding;
	}
	
	@Nullable
	public String getName()
	{
		return name;
	}
	
	@Nullable
	public List<ShaderVariable> getVariables()
	{
		return variables;
	}
}
