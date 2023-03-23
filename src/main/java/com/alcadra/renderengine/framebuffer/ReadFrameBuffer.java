package com.alcadra.renderengine.framebuffer;

import org.lwjgl.opengl.GL30;

public class ReadFrameBuffer extends FrameBuffer {
	
	public ReadFrameBuffer()
	{
		super(GL30.GL_READ_FRAMEBUFFER);
	}
}
