package com.alcadra.renderengine.renderer.object;

import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@UpdatedFrom("com.alcadra.renderengine.buffer.Bufferable")
public interface BufferableModel {
	
	DataGetter getVertexData(int vertex, ShaderInput shaderInput);
	
	int vertexSize();
	int vertices();
}
