package com.alcadrial.renderengine.render.shader;

import java.util.List;

import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.shader.layout.ShaderLayout;

public class ShaderLayoutBinding {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	private int stages;
	private ShaderLayout.Type descriptorType;
	private int descriptorCount;
	private List<ShaderVariable> layoutsVariables;

	public ShaderLayoutBinding(int stages, ShaderLayout.Type descriptorType, int descriptorCount, ShaderVariable... layoutsVariables)
	{
		this.stages = stages;
		this.descriptorType = descriptorType;
		this.descriptorCount = descriptorCount;
		this.layoutsVariables = List.of(layoutsVariables);
	}
	public ShaderLayoutBinding(int stages, ShaderLayout.Type descriptorType, int descriptorCount, List<ShaderVariable> layoutsVariables)
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
	
	public List<ShaderVariable> getLayoutsVariables()
	{
		return layoutsVariables;
	}
	
	@Override
	public String toString()
	{
		return "ShaderDescriptorSet{stages=" + stages + ", descriptorType=" + descriptorType + ", layoutsVariables=" + layoutsVariables + "}";
	}
}
