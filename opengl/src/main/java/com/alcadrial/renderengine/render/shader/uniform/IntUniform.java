package com.alcadrial.renderengine.render.shader.uniform;

import org.lwjgl.opengl.GL20;

class IntUniform extends ShaderUniform<Integer> {
	
	public IntUniform(String name)
	{
		super(name);
	}
	
	@Override
	public void setValue(int location, Integer value)
	{
		GL20.glUniform1i(location, value);
	}
}
