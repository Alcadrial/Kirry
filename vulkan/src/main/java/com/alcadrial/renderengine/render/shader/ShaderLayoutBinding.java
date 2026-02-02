package com.alcadrial.renderengine.render.shader;

import java.util.Set;

import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;

public class ShaderLayoutBinding {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private int stages;
	private ShaderLayout.Type descriptorType;
	private int descriptorCount;
	private Set<ShaderVariable> layoutsVariables;
	
	public ShaderLayoutBinding(int stages, ShaderLayout.Type descriptorType, int descriptorCount, Set<ShaderVariable> layoutsVariables)
	{
		this.stages = stages;
		this.descriptorType = descriptorType;
		this.descriptorCount = descriptorCount;
		this.layoutsVariables = layoutsVariables;
	}
	
	public int getStages()
	{
		return stages;
	}
	
	public void setStages(int stages)
	{
		this.stages = stages;
	}
	
	public ShaderLayout.Type getDescriptorType()
	{
		return descriptorType;
	}
	
	public int getDescriptorCount()
	{
		return descriptorCount;
	}
	
	public Set<ShaderVariable> getLayoutsVariables()
	{
		return layoutsVariables;
	}
	
	@Override
	public String toString()
	{
		return "ShaderDescriptorSet{stages=" + stages + ", descriptorType=" + descriptorType + ", layoutsVariables=" + layoutsVariables + "}";
	}
}
