package com.alcadra.renderengine.renderer;

import com.alcadra.math.Matrix4f;

public interface IndicesRendererUpdate {
	
	void drawPoints(RenderingMode mode, Matrix4f matrix);
}
