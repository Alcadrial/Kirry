package com.alcadrial.renderengine.render.renderer;

public interface IndicesRenderer extends Renderer {
	
	boolean useIndices();
	void setIndicesUse(boolean useIndices);
	
	void syncIndices();
}
