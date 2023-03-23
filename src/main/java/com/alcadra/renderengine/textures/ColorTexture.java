package com.alcadra.renderengine.textures;

import org.lwjgl.opengl.GL14;

import com.alcadra.renderengine.util.Image;

public class ColorTexture extends Texture2d {
	
	private static final ColorTexture NULL = new ColorTexture(0, 0, 0, 0, 0);
	
	public ColorTexture(Image image)
	{
		super(image.getBuffered().getWidth(), image.getBuffered().getHeight(), GL14.GL_RGBA, GL14.GL_RGBA, GL14.GL_UNSIGNED_BYTE, image.loadToBuffer());
	}
	
	public ColorTexture(int width, int height)
	{
		super(width, height, GL14.GL_RGBA, GL14.GL_RGBA, GL14.GL_UNSIGNED_BYTE, null);
	}
	
	protected ColorTexture(int width, int height, int id, int textureFormat, int dataType)
	{
		super(width, height, id, textureFormat, dataType);
	}
	
	public static ColorTexture getNull()
	{
		return NULL;
	}
}
