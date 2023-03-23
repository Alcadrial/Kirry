package com.alcadra.renderengine.renderer;

import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.util.Nullable;
import com.alcadra.renderengine.util.update.UpdatedTo;

@UpdatedTo("ShaderRendererUpdate")
@Deprecated(since = "2.0-migration", forRemoval = true)
public interface ShaderRenderer {
	
	@UpdatedTo("getShaderProgram")
	@Nullable
	Shader getShader();
	
	@UpdatedTo("setShaderProgram")
	public void changeShader(@Nullable Shader other);
}
