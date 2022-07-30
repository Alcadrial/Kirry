package com.alcadra.renderengine.shader.uniform;

import org.lwjgl.opengl.GL20;

import com.alcadra.math.Matrix4f;

class Matrix4fUniform extends ShaderUniform<Matrix4f> {
	
	public Matrix4fUniform(String name)
	{
		super(name);
	}
	
	@Override
	public void setValue(int location, Matrix4f value)
	{
		GL20.glUniformMatrix4fv(location, true, value.toArray());
	}
}
