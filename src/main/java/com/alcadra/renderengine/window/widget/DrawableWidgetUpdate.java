package com.alcadra.renderengine.window.widget;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.renderengine.renderer.AbstractShaderRendererUpdate;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@UpdatedFrom("DrawableWidget")
public interface DrawableWidgetUpdate<R extends AbstractShaderRendererUpdate<?>> extends IWidget {
	
	void render(Matrix4f matrix, R renderer, Vector2i pos, float partialTicks);
	
	default void render(Matrix4f matrix, R renderer, float partialTicks)
	{
		render(matrix, renderer, getPos().xy(), partialTicks);
	}
	
	void draw(Matrix4f matrix, R renderer, float partialTicks);
}
