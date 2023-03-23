package com.alcadra.renderengine.shader.uniform;

import org.lwjgl.opengl.GL20;

import com.alcadra.math.vector.Vector2i;

class Vector2iUniform extends ShaderUniform<Vector2i> {
	
	public Vector2iUniform(String name)
	{
		super(name);
	}
	
	@Override
	public void setValue(int location, Vector2i value)
	{
		GL20.glUniform2i(location, value.x(), value.y());
	}
}
