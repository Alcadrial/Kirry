package com.alcadra.renderengine.renderer;

import com.alcadra.renderengine.shader.ShaderProgram;

public interface ShaderRenderer {
	
	ShaderProgram getShaderProgram();
	
	public void setShaderProgram(ShaderProgram other);
}
