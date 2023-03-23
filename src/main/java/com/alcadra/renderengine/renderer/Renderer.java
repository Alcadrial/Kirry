package com.alcadra.renderengine.renderer;

import com.alcadra.math.Matrix4f;

public interface Renderer {
	
	void draw(RenderingMode mode, Matrix4f matrix);
	void draw(RenderingMode mode, Matrix4f matrix, int count);
	void draw(RenderingMode mode, Matrix4f matrix, int count, int offset);
}
