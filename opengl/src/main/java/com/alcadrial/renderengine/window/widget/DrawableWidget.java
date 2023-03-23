package com.alcadrial.renderengine.window.widget;

import com.alcadra.math.matrix.Matrix4f;
import com.alcadra.math.vector.Vector2i;
import com.alcadrial.renderengine.renderer.ShaderRenderer;

public interface DrawableWidget<R extends ShaderRenderer> extends Widget {
	
	void render(Matrix4f matrix, R renderer, Vector2i pos, float partialTicks);
	
	default void render(Matrix4f matrix, R renderer, float partialTicks)
	{
		render(matrix, renderer, getPos().xy(), partialTicks);
	}
	
	void draw(Matrix4f matrix, R renderer, float partialTicks);
}
