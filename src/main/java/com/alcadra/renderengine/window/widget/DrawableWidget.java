package com.alcadra.renderengine.window.widget;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.renderengine.renderer.BaseRenderer;
import com.alcadra.renderengine.util.update.UpdatedTo;

@UpdatedTo("DrawableWidgetUpdate")
@Deprecated(since = "2.0-migration", forRemoval = true)
public interface DrawableWidget<R extends BaseRenderer<?>> extends IWidget {
	
	void render(Matrix4f matrix, R renderer, Vector2i pos, float partialTicks);
	
	default void render(Matrix4f matrix, R renderer, float partialTicks)
	{
		render(matrix, renderer, getPos().xy(), partialTicks);
	}
	
	void draw(Matrix4f matrix, R renderer, float partialTicks);
}
