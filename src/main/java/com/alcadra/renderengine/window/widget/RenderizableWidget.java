package com.alcadra.renderengine.window.widget;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.renderengine.renderer.BaseRenderer;

public interface RenderizableWidget<R extends BaseRenderer<?>> extends IWidget {
	
	void render(Matrix4f matrix, R renderer, Vector2i pos, float partialTicks);
}
