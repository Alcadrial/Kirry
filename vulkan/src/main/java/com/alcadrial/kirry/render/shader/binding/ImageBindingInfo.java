package com.alcadrial.kirry.render.shader.binding;

import java.util.List;

import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.shader.layout.ShaderLayout.Type;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.texture.TextureSampler;

public final class ImageBindingInfo extends BindingInfo {
	
	static
	{
		KirryVulkan.assertLoaded();
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
