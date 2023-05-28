package com.alcadrial.renderengine.opengl.renderer.object;

import com.alcadrial.renderengine.opengl.shader.ShaderInput;

public interface BufferableModel {
	
	DataGetter getVertexData(int vertex, ShaderInput shaderInput);
	
	int vertexSize();
	int vertices();
}
