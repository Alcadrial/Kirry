package com.alcadra.renderengine.renderer;

import com.alcadra.math.Matrix4f;

public interface Renderer {
	
	void drawVertices(RenderingMode mode, Matrix4f matrix);
	void drawVertices(RenderingMode mode, Matrix4f matrix, int count);
	void drawVertices(RenderingMode mode, Matrix4f matrix, int count, int offset);
}
