package com.alcadrial.renderengine.opengl.renderer.screen;

import java.util.Optional;
import java.util.function.Supplier;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.renderengine.opengl.renderer.ShaderRenderer;
import com.alcadrial.renderengine.opengl.textures.Texture;

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
