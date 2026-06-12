package com.alcadrial.kirry.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import com.alcadrial.kector.vector.Vector2f;

class Vector2fUniform extends ShaderUniform<Vector2f> {
	
	public Vector2fUniform(String name)
	{
		super(name);
	}
	
	@Override
	public void setValue(int location, Vector2f value)
	{
		GL20.glUniform2f(location, value.x(), value.y());
	}
}
