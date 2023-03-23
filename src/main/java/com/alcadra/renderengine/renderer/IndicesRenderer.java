package com.alcadra.renderengine.renderer;

import com.alcadra.math.Matrix4f;

public interface IndicesRenderer extends Renderer {
	
	void drawPoints(RenderingMode mode, Matrix4f matrix);
	void drawPoints(RenderingMode mode, Matrix4f matrix, int count);
	void drawPoints(RenderingMode mode, Matrix4f matrix, int count, int offset);
}
