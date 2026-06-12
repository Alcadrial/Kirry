package com.alcadrial.kirry.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import com.alcadrial.kector.matrix.Matrix3f;

class Matrix3fUniform extends ShaderUniform<Matrix3f> {
	
	public Matrix3fUniform(String name)
	{
		super(name);
	}
	
	@Override
	public void setValue(int location, Matrix3f value)
	{
		GL20.glUniformMatrix4fv(location, true, value.toArray());
	}
}
