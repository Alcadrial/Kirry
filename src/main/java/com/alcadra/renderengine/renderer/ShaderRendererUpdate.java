package com.alcadra.renderengine.renderer;

import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@UpdatedFrom("ShaderRenderer")
public interface ShaderRendererUpdate {
	
	@UpdatedFrom("getShader")
	ShaderProgram getShaderProgram();
	
	@UpdatedFrom("changeShader")
	public void setShaderProgram(ShaderProgram other);
}
