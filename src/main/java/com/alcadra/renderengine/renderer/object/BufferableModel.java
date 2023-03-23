package com.alcadra.renderengine.renderer.object;

import com.alcadra.renderengine.shader.ShaderInput;

public interface BufferableModel {
	
	DataGetter getVertexData(int vertex, ShaderInput shaderInput);
	
	int vertexSize();
	int vertices();
}
