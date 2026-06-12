package com.alcadrial.kirry.render.renderer;

import com.alcadrial.kirry.render.shader.ShaderProgram;

public interface ShaderProgrammable {
	
	ShaderProgram getShaderProgram();
	void setShaderProgram(ShaderProgram shader);
}
