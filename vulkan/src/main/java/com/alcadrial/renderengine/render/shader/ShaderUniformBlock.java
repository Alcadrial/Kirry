package com.alcadrial.renderengine.render.shader;

import java.util.Set;

import com.alcadrial.renderengine.core.util.Nullable;

public class ShaderUniformBlock {
	
	private int binding;
	private String name;
	private Set<ShaderUniform> uniforms;
	
	public ShaderUniformBlock(int binding, Set<ShaderUniform> variables)
	{
		this(binding, null, variables);
	}
	
	public ShaderUniformBlock(int binding, @Nullable String name, Set<ShaderUniform> uniforms)
	{
		this.binding = binding;
		this.name = name;
		this.uniforms = uniforms;
	}
	
	/**
	 * 
	 * @return the binding of this block
	 */
	public int getBinding()
	{
		return binding;
	}
	
	/**
	 * 
	 * @return the name of this block, if any
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * 
	 * @return the uniforms in this block
	 */
	public Set<ShaderUniform> getUniforms()
	{
		return uniforms;
	}
}
