package com.alcadrial.renderengine.opengl.renderer;

import com.alcadrial.kector.matrix.Matrix4f;

public interface Renderer {
	
	void draw(RenderingMode mode, Matrix4f matrix);
	void draw(RenderingMode mode, Matrix4f matrix, int count);
	void draw(RenderingMode mode, Matrix4f matrix, int count, int offset);
}
