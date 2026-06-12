package com.alcadrial.kirry.render.renderer;

public interface IndicesRenderer extends Renderer {
	
	boolean useIndices();
	void setIndicesUse(boolean useIndices);
	
	void syncIndices();
}
