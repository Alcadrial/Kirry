package com.alcadrial.kirry.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import com.alcadrial.kector.vector.Vector4f;

class Vector4fUniform extends ShaderUniform<Vector4f> {
	
	public Vector4fUniform(String name)
	{
		super(name);
	}
	
	@Override
	public void setValue(int location, Vector4f value)
	{
		GL20.glUniform4f(location, value.x(), value.y(), value.z(), value.w());
	}
}
