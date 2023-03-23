package com.alcadra.renderengine.renderer.object;

import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@UpdatedFrom("Bufferable")
public interface BufferableModel {
	
	DataGetter getVertexData(int vertex, ShaderInput shaderInput);
	
	int vertexSize();
	int vertices();
}
