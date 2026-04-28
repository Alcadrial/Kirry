package com.alcadrial.renderengine.render.shader.binding;

import java.util.List;

import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.layout.ShaderLayout.Type;

public final class BufferBindingInfo extends BindingInfo {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	private VulkanBuffer buffer;
	
	BufferBindingInfo(VulkanBuffer buffer, int stages, Type descriptorType, int descriptorCount, List<ShaderVariable> variables)
	{
		super(stages, descriptorType, descriptorCount, variables);
		this.buffer = buffer;
	}
	
	public VulkanBuffer getBuffer()
	{
		return buffer;
	}
}
