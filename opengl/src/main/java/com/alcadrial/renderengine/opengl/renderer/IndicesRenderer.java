package com.alcadrial.renderengine.opengl.renderer;

public interface IndicesRenderer extends Renderer {
	
	boolean useIndices();
	void setIndicesUse(boolean useIndices);
}
