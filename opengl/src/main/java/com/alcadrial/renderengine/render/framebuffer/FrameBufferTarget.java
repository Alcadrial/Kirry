package com.alcadrial.renderengine.render.framebuffer;

import org.lwjgl.opengl.GL30;

public enum FrameBufferTarget {
	
	READ(GL30.GL_READ_FRAMEBUFFER),
	DRAW(GL30.GL_DRAW_FRAMEBUFFER),
	BOTH(GL30.GL_FRAMEBUFFER);
	
	private int target;
	
	private FrameBufferTarget(int target)
	{
		this.target = target;
	}
	
	public int getTarget()
	{
		return target;
	}
}
