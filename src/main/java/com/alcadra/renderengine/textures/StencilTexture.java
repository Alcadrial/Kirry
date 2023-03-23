package com.alcadra.renderengine.textures;

import org.lwjgl.opengl.GL30;

public class StencilTexture extends Texture2d {
	
	public StencilTexture(int width, int height)
	{
		super(width, height, GL30.GL_DEPTH_STENCIL, GL30.GL_STENCIL_INDEX, GL30.GL_INT, null);
	}
	
	protected StencilTexture(int width, int height, int id, int textureFormat, int dataType)
	{
		super(width, height, id, textureFormat, dataType);
	}
}
