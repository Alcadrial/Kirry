package com.alcadra.renderengine.buffer;

import com.alcadra.renderengine.renderer.object.VertexModifier;
import com.alcadra.renderengine.util.update.UpdatedTo;

@Deprecated(since = "2.0-migration", forRemoval = true)
@UpdatedTo("com.alcadra.renderengine.renderer.object.BufferableModel")
public interface Bufferable {
	
	int[][] getData();
	int[] getVertexData(int index, VertexModifier modifier, int[] minimalData);
	
	int vertexSize();
	int vertices();
}
