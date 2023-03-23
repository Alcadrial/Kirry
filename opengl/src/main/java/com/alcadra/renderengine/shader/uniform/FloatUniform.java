package com.alcadra.renderengine.shader.uniform;

import org.lwjgl.opengl.GL20;

class FloatUniform extends ShaderUniform<Float> {
	
	public FloatUniform(String name)
	{
		super(name);
	}
	
	@Override
	public void setValue(int location, Float value)
	{
		GL20.glUniform1f(location, value);
	}
}
