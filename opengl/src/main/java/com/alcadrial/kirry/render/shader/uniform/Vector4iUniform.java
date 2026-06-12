package com.alcadrial.kirry.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import com.alcadrial.kector.vector.Vector4i;

class Vector4iUniform extends ShaderUniform<Vector4i> {
	
	public Vector4iUniform(String name)
	{
		super(name);
	}
	
	@Override
	public void setValue(int location, Vector4i value)
	{
		GL20.glUniform4i(location, value.x(), value.y(), value.z(), value.w());
	}
}
