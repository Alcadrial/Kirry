package com.alcadra.renderengine.textures;

import org.lwjgl.opengl.GL14;

public class DepthTexture extends Texture2d {
	
	public DepthTexture(int width, int height)
	{
		super(width, height, GL14.GL_DEPTH_COMPONENT32, GL14.GL_DEPTH_COMPONENT, GL14.GL_FLOAT, null);
	}
	
	protected DepthTexture(int width, int height, int id, int textureFormat, int dataType)
	{
		super(width, height, id, textureFormat, dataType);
	}
}
