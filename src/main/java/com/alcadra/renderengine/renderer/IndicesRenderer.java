package com.alcadra.renderengine.renderer;

import com.alcadra.math.Matrix4f;
import com.alcadra.renderengine.util.update.UpdatedTo;

@UpdatedTo("IndicesRendererUpdate")
@Deprecated(since = "2.0-migration", forRemoval = true)
public interface IndicesRenderer {
	
	@UpdatedTo("drawPoints(RenderingMode, Matrix")
	void drawPoints(int mode, Matrix4f matrix);
}
