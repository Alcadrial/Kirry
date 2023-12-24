package com.alcadrial.renderengine.render.shader;

import com.alcadrial.renderengine.render.RenderEngine;

public record PreparedShader(Shader shader, ShaderInfo info) {
	
	static
	{
		RenderEngine.assertLoaded();
	}
}
