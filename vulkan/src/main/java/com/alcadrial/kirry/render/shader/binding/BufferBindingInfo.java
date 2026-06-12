package com.alcadrial.kirry.render.shader.binding;

import java.util.List;

import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.shader.layout.ShaderLayout.Type;

public final class BufferBindingInfo extends BindingInfo {
	
	static
	{
		KirryVulkan.assertLoaded();
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
