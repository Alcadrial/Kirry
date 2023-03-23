package com.alcadrial.renderengine.renderer.object;

import com.alcadrial.renderengine.shader.ShaderInput;

public interface BufferableModel {
	
	DataGetter getVertexData(int vertex, ShaderInput shaderInput);
	
	int vertexSize();
	int vertices();
}
