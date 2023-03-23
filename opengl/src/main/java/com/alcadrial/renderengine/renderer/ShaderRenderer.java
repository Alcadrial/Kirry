package com.alcadrial.renderengine.renderer;

import com.alcadrial.renderengine.shader.ShaderProgram;

public interface ShaderRenderer extends Renderer {
	
	ShaderProgram getShaderProgram();
	void setShaderProgram(ShaderProgram other);
}
