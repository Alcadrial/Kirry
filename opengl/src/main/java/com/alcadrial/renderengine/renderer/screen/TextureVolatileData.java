package com.alcadrial.renderengine.renderer.screen;

import java.util.Optional;
import java.util.function.Supplier;

import com.alcadra.math.matrix.Matrix4f;
import com.alcadrial.renderengine.renderer.ShaderRenderer;
import com.alcadrial.renderengine.textures.Texture;

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
		texture = Optional.of(t);
		return t;
	}
	
	protected void draw(ShaderRenderer renderer, Matrix4f matrix)
	{
		
	}
	
	public void unloadTexture()
	{
		texture.ifPresent(Texture::close);
		texture = Optional.empty();
	}
}
