package com.alcadrial.renderengine.render.shader;

import com.alcadrial.renderengine.render.RenderEngineVulkan;

public record PreparedShader(Shader shader, ShaderType type) {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
}
