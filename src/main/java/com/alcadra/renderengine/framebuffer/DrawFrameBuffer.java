package com.alcadra.renderengine.framebuffer;

import org.lwjgl.opengl.GL30;

public class DrawFrameBuffer extends FrameBuffer {
	
	public DrawFrameBuffer()
	{
		super(GL30.GL_DRAW_FRAMEBUFFER);
	}
	
}
