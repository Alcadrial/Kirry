package com.alcadrial.kirry.render.texture;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;

public class TextureRegion {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	private @Nullable Texture texture;
	private int xStart;
	private int yStart;
	private int xEnd;
	private int yEnd;
	
	public TextureRegion(@Nullable Texture texture, int xStart, int yStart, int xEnd, int yEnd)
	{
		this.texture = texture;
		this.xStart = xStart;
		this.yStart = yStart;
		this.xEnd = xEnd;
		this.yEnd = yEnd;
	}
	
	public TextureRegion(@Nullable Texture texture, Vector2ir uvStart, Vector2ir uvEnd)
	{
		this.texture = texture;
		xStart = uvStart.x();
		yStart = uvStart.y();
		xEnd = uvEnd.x();
		yEnd = uvEnd.y();
	}
	
	public void update(@Nullable Texture texture, int xStart, int yStart, int xEnd, int yEnd)
	{
		this.texture = texture;
		this.xStart = xStart;
		this.yStart = yStart;
		this.xEnd = xEnd;
		this.yEnd = yEnd;
	}
	
	public boolean validate()
	{
		return texture != null;
	}
	
	public Texture getTexture()
	{
		return VulkanErrorUtils.nonNull(texture, "Missing texture");
	}
	
	public Vector2f getUvStart()
	{
		Texture texture = getTexture();
		return Vector2f.of((float) xStart / texture.getWidth(), (float) yStart / texture.getHeight());
	}
	
	public Vector2f getUvEnd()
	{
		Texture texture = getTexture();
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
		return "TextureRegion{" + texture + ", uv=[" + xStart + ";" + xEnd + ")x[" + yStart + ";" + yEnd + ")}";
	}
}
