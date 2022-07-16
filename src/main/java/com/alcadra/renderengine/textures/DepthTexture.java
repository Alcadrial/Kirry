package com.alcadra.renderengine.textures;

import org.lwjgl.opengl.GL14;

public class DepthTexture extends Texture2d {
	
	private static final DepthTexture NULL = new DepthTexture(0, 0, 0, 0, 0);
	
	public static DepthTexture getNull()
	{
		return NULL;
	}
	
	public DepthTexture(int width, int height)
	{
		super(width, height, GL14.GL_DEPTH_COMPONENT32, GL14.GL_DEPTH_COMPONENT, GL14.GL_FLOAT, null);
	}
	
	protected DepthTexture(int width, int height, int id, int textureFormat, int dataType)
	{
		super(width, height, id, textureFormat, dataType);
	}
}
