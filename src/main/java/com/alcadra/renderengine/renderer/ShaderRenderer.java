package com.alcadra.renderengine.renderer;

import com.alcadra.renderengine.shader.ShaderProgram;

public interface ShaderRenderer extends Renderer {
	
	ShaderProgram getShaderProgram();
	
	public void setShaderProgram(ShaderProgram other);
}
