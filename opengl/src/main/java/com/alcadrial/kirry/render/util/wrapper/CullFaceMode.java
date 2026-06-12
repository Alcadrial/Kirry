package com.alcadrial.kirry.render.util.wrapper;

import org.lwjgl.opengl.GL11;

public enum CullFaceMode {
	
	FRONT(GL11.GL_FRONT),
	BACK(GL11.GL_BACK),
	FRONT_AND_BACK(GL11.GL_FRONT_AND_BACK);
	
	private int id;
	
	private CullFaceMode(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
}
