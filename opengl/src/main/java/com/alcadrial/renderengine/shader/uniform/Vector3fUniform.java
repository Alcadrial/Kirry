package com.alcadrial.renderengine.shader.uniform;

import org.lwjgl.opengl.GL20;

import com.alcadrial.kector.vector.Vector3f;

class Vector3fUniform extends ShaderUniform<Vector3f> {
	
	public Vector3fUniform(String name)
	{
		super(name);
	}
	
	@Override
	public void setValue(int location, Vector3f value)
	{
		GL20.glUniform3f(location, value.x(), value.y(), value.z());
	}
}
