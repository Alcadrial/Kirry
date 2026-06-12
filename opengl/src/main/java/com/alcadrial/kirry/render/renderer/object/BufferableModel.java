package com.alcadrial.kirry.render.renderer.object;

import com.alcadrial.kirry.render.shader.ShaderInput;

public interface BufferableModel {
	
	DataGetter getVertexData(int vertex, ShaderInput shaderInput);
	
	int vertexSize();
	int vertices();
}
