package com.alcadrial.renderengine.render.renderer;

import com.alcadrial.renderengine.render.shader.ShaderProgram;

public interface ShaderProgrammable {
	
	ShaderProgram getShaderProgram();
	void setShaderProgram(ShaderProgram shader);
}
