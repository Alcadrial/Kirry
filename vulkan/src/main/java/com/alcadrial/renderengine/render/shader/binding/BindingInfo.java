package com.alcadrial.renderengine.render.shader.binding;

import java.util.ArrayList;
import java.util.List;

import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.layout.ShaderLayout;
import com.alcadrial.renderengine.render.shader.layout.ShaderLayout.Type;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureSampler;

public sealed class BindingInfo permits BufferBindingInfo, ImageBindingInfo {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static class TextureBindingInfoBuilder {
		
		private List<Texture> textures;
		private List<TextureSampler> samplers;
		private List<ShaderVariable> variables;
		private int stages;
		private Type type;
		
		private TextureBindingInfoBuilder(int stages, Type type)
		{
			textures = new ArrayList<>();
			samplers = new ArrayList<>();
			variables = new ArrayList<>();
			this.stages = stages;
			this.type = type;
		}
		
		public TextureBindingInfoBuilder add(Texture texture, TextureSampler sampler, ShaderVariable variable)
		{
			textures.add(texture);
			samplers.add(sampler);
			variables.add(variable);
			return this;
		}
		
		public BindingInfo build()
		{
			return new ImageBindingInfo(stages, type, textures, samplers, variables);
		}
	}
	
	public static BindingInfo ofStorage(VulkanBuffer storageBuffer, int stages, List<ShaderVariable> variables)
	{
		return new BufferBindingInfo(storageBuffer, stages, Type.STORAGE_BUFFER, 1, variables);
	}
	
	public static BindingInfo ofUniform(VulkanBuffer uniformBuffer, int stages, List<ShaderVariable> variables)
	{
		return new BufferBindingInfo(uniformBuffer, stages, Type.UNIFORM_BUFFER, 1, variables);
	}
	
	public static BindingInfo ofUniforms(VulkanBuffer uniformBuffer, int stages, int size, List<ShaderVariable> variables)
	{
		return new BufferBindingInfo(uniformBuffer, stages, Type.UNIFORM_BUFFER, size, variables);
	}
	
	public static BindingInfo ofTexture(Texture texture, TextureSampler sampler, int stages, ShaderVariable variable)
	{
		return ofTextures(stages).add(texture, sampler, variable).build();
	}
	
	public static TextureBindingInfoBuilder ofTextures(int stages)
	{
		return new TextureBindingInfoBuilder(stages, Type.COMBINED_IMAGE_SAMPLER);
	}
	
	private int stages;
	private ShaderLayout.Type descriptorType;
	private int descriptorCount;
	private List<ShaderVariable> variables;
	
	protected BindingInfo(int stages, Type descriptorType, int descriptorCount, List<ShaderVariable> variables)
	{
		this.stages = stages;
		this.descriptorType = descriptorType;
		this.descriptorCount = descriptorCount;
		this.variables = variables;
	}
	
	public int getStages()
	{
		return stages;
	}
	
	public ShaderLayout.Type getDescriptorType()
	{
		return descriptorType;
	}
	
	public int getDescriptorCount()
	{
		return descriptorCount;
	}
	
	public List<ShaderVariable> getVariables()
	{
		return variables;
	}
}
