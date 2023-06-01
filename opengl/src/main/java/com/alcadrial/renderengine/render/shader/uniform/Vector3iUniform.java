package com.alcadrial.renderengine.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import com.alcadrial.kector.vector.Vector3i;

class Vector3iUniform extends ShaderUniform<Vector3i> {
	
	public Vector3iUniform(String name)
	{
		super(name);
	}
	
	@Override
	public void setValue(int location, Vector3i value)
	{
		GL20.glUniform3i(location, value.x(), value.y(), value.z());
	}
}
