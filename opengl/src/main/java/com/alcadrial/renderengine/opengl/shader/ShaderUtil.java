package com.alcadrial.renderengine.opengl.shader;

public class ShaderUtil {
	
	private ShaderUtil()
	{}
	
	public static ShaderProgram assertBuildedProgram(ShaderProgram program)
	{
		if (program == null || !program.getStatus().isBuilded()) throw new IllegalArgumentException("Program must be builded!");
		return program;
	}
}
