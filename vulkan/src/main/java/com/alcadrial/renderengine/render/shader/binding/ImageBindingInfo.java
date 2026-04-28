package com.alcadrial.renderengine.render.shader.binding;

import java.util.List;

import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.layout.ShaderLayout.Type;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureSampler;

public final class ImageBindingInfo extends BindingInfo {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	private List<Texture> textures;
	private List<TextureSampler> samplers;
	
	ImageBindingInfo(int stages, Type descriptorType, List<Texture> textures, List<TextureSampler> samplers, List<ShaderVariable> variables)
	{
		super(stages, descriptorType, variables.size(), variables);
		this.textures = textures;
		this.samplers = samplers;
	}
	
	public List<Texture> getTextures()
	{
		return textures;
	}
	
	public List<TextureSampler> getSamplers()
	{
		return samplers;
	}
}
