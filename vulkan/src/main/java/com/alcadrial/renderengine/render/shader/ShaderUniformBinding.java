package com.alcadrial.renderengine.render.shader;

import java.util.Set;

import com.alcadrial.renderengine.render.RenderEngine;

public class ShaderUniformBinding {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private int stages;
	private ShaderDescriptorType descriptorType;
	private Set<ShaderUniform> uniformBlocks;
	
	public ShaderUniformBinding(int stages, ShaderDescriptorType descriptorType, Set<ShaderUniform> uniformBlocks)
	{
		this.stages = stages;
		this.descriptorType = descriptorType;
		this.uniformBlocks = uniformBlocks;
	}
	
	public int getStages()
	{
		return stages;
	}
	
	public void setStages(int stages)
	{
		this.stages = stages;
	}
	
	public ShaderDescriptorType getDescriptorType()
	{
		return descriptorType;
	}
	
	public void setDescriptorType(ShaderDescriptorType descriptorType)
	{
		this.descriptorType = descriptorType;
	}
	
	public Set<ShaderUniform> getUniforms()
	{
		return uniformBlocks;
	}
	
	public void setUniforms(Set<ShaderUniform> uniformBlocks)
	{
		this.uniformBlocks = uniformBlocks;
	}
	
	@Override
	public String toString()
	{
		return "ShaderUniformBinding{stages=" + stages + ", descriptorType=" + descriptorType + ", uniformBlocks=" + uniformBlocks + "}";
	}
}
