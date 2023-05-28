package com.alcadrial.renderengine.opengl.renderer;

import com.alcadrial.renderengine.opengl.shader.ShaderProgram;

public interface ShaderRenderer extends Renderer {
	
	ShaderProgram getShaderProgram();
	void setShaderProgram(ShaderProgram other);
}
