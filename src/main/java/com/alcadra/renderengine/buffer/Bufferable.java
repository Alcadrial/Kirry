package com.alcadra.renderengine.buffer;

import com.alcadra.renderengine.renderer.object.VertexModifier;

public interface Bufferable {
	
	int[][] getData();
	int[] getVertexData(int index, VertexModifier modifier, int[] minimalData);
	
	int vertexSize();
	int vertices();
}
