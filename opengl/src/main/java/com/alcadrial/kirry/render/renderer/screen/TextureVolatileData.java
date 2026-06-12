package com.alcadrial.kirry.render.renderer.screen;

import java.util.Optional;
import java.util.function.Supplier;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kirry.render.renderer.ShaderProgrammable;
import com.alcadrial.kirry.render.textures.Texture;

public class TextureVolatileData implements Supplier<Texture> {
	
	private Optional<Texture> texture;
	
	public TextureVolatileData()
	{
		texture = Optional.empty();
	}
	
	@Override
	public Texture get()
	{
		return texture.orElseGet(this::loadTexture);
	}
	
	public Texture loadTexture()
	{
		Texture t = null;
		texture = Optional.ofNullable(t);
		return t;
	}
	
	protected void draw(ShaderProgrammable renderer, Matrix4f matrix)
	{
		
	}
	
	public void unloadTexture()
	{
		texture.ifPresent(Texture::close);
		texture = Optional.empty();
	}
}
