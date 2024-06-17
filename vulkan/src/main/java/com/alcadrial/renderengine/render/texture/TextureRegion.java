package com.alcadrial.renderengine.render.texture;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.renderengine.render.RenderEngine;

public class TextureRegion {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private Texture texture;
	private Vector2f uvStart;
	private Vector2f uvEnd;
	
	public TextureRegion(Texture texture, Vector2f uvStart, Vector2f uvEnd)
	{
		this.texture = texture;
		this.uvStart = Vector2f.copyOfImmutable(uvStart);
		this.uvEnd = Vector2f.copyOfImmutable(uvEnd);
	}
	
	public Texture getTexture()
	{
		return texture;
	}
	
	public Vector2f getUvStart()
	{
		return uvStart;
	}
	
	public Vector2f getUvEnd()
	{
		return uvEnd;
	}
}
