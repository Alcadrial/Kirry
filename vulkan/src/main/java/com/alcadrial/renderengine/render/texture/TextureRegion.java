package com.alcadrial.renderengine.render.texture;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.getter.Vector2iGetter;
import com.alcadrial.renderengine.render.RenderEngineVulkan;

public class TextureRegion {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	private Texture texture;
	private int xStart;
	private int yStart;
	private int xEnd;
	private int yEnd;
	
	public TextureRegion(Texture texture, int xStart, int yStart, int xEnd, int yEnd)
	{
		this.texture = texture;
		this.xStart = xStart;
		this.yStart = yStart;
		this.xEnd = xEnd;
		this.yEnd = yEnd;
	}
	
	public TextureRegion(Texture texture, Vector2iGetter uvStart, Vector2iGetter uvEnd)
	{
		this.texture = texture;
		this.xStart = uvStart.x();
		this.yStart = uvStart.y();
		this.xEnd = uvEnd.x();
		this.yEnd = uvEnd.y();
	}
	
	public void update(Texture texture, int xStart, int yStart, int xEnd, int yEnd)
	{
		this.texture = texture;
		this.xStart = xStart;
		this.yStart = yStart;
		this.xEnd = xEnd;
		this.yEnd = yEnd;
	}
	
	public Texture getTexture()
	{
		return texture;
	}
	
	public Vector2f getUvStart()
	{
		return Vector2f.of((float) xStart / texture.getWidth(), (float) yStart / texture.getHeight());
	}
	
	public Vector2f getUvEnd()
	{
		return Vector2f.of((float) xEnd / texture.getWidth(), (float) yEnd / texture.getHeight());
	}
	
	public int getXStart()
	{
		return xStart;
	}
	
	public int getYStart()
	{
		return yStart;
	}
	
	public int getXEnd()
	{
		return xEnd;
	}
	
	public int getYEnd()
	{
		return yEnd;
	}
	
	@Override
	public String toString()
	{
		return "TextureRegion{" + texture + ", uv=[" + xStart + ";" + yStart + ")x[" + xEnd + ";" + yEnd + ")}";
	}
}
