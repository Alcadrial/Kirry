package com.alcadra.renderengine.renderer;

import com.alcadra.renderengine.shader.Shader;

public interface ShaderRenderer {
	
	public void changeShader(Shader other);
	
	Shader getShader();
}
