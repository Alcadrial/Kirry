package com.alcadrial.renderengine.opengl.window.widget;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.opengl.renderingmanager.RenderingManager;

public interface DrawableWidget extends Widget {
	
	void render(Matrix4f matrix, RenderingManager manager, Vector2i pos, float partialTicks);
	
	default void render(Matrix4f matrix, RenderingManager manager, float partialTicks)
	{
		render(matrix, manager, getPos().xy(), partialTicks);
	}
	
	void draw(Matrix4f matrix, RenderingManager manager, float partialTicks);
}
