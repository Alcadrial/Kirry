package com.alcadrial.renderengine.render.renderer.object;

import com.alcadrial.renderengine.render.shader.ShaderInput;

public interface BufferableModel {
	
	DataGetter getVertexData(int vertex, ShaderInput shaderInput);
	
	int vertexSize();
	int vertices();
}
