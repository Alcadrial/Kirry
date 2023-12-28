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
	private Set<ShaderVariable> layoutsVariables;
	
	public ShaderLayoutBinding(int stages, ShaderLayout.Type descriptorType, Set<ShaderVariable> layoutsVariables)
	{
		this.stages = stages;
		this.descriptorType = descriptorType;
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
	
	public void setDescriptorType(ShaderLayout.Type descriptorType)
	{
		this.descriptorType = descriptorType;
	}
	
	public Set<ShaderVariable> getLayoutsVariables()
	{
		return layoutsVariables;
	}
	
	public void setLayoutsVariables(Set<ShaderVariable> layoutsVariables)
	{
		this.layoutsVariables = layoutsVariables;
	}
	
	@Override
	public String toString()
	{
		return "ShaderDescriptorSet{stages=" + stages + ", descriptorType=" + descriptorType + ", layoutsVariables=" + layoutsVariables + "}";
	}
}
