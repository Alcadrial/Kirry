package com.alcadra.renderengine.textures;

import org.lwjgl.opengl.GL14;

public class Texture2d extends Texture {
	
	protected Texture2d(int width, int height, int internalFormat, int textureFormat, int dataType, int[] pixels)
	{
		super(width, height, GL14.GL_TEXTURE_2D, internalFormat, textureFormat, dataType, pixels);
	}
	
	protected Texture2d(int width, int height, int id, int textureFormat, int dataType)
	{
		super(width, height, id, GL14.GL_TEXTURE_2D, textureFormat, dataType);
	}
}
