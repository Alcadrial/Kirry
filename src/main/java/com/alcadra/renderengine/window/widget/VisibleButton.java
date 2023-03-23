package com.alcadra.renderengine.window.widget;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.renderer.universal.UniversalRenderer;
import com.alcadra.renderengine.window.Window;

public abstract class VisibleButton extends Button implements DrawableWidget<UniversalRenderer> {
	
	public VisibleButton(Vector3i pos, Vector2i size, Window window)
	{
		super(pos, size, window);
	}
	
	@Override
	public void render(Matrix4f matrix, UniversalRenderer renderer, Vector2i pos, float partialTicks)
	{
		
	}
	
	@Override
	public void draw(Matrix4f matrix, UniversalRenderer renderer, float partialTicks)
	{
		renderer.clear();
		renderer.setIndicesUse(false);
		float w = window.getWidth() / 2;
		float h = window.getHeight() / 2;
		renderer.vertex(pos.x() - w, pos.y() - h + 16, pos.z()).color(127, 127, 127).color(127, 127, 127, 255).end();
		renderer.vertex(pos.x() - w + 16, pos.y() - h + 16, pos.z()).color(127, 127, 127).color(127, 127, 127, 255).end();
		renderer.vertex(pos.x() - w + 16, pos.y() - h, pos.z()).color(127, 127, 127).color(127, 127, 127, 255).end();
		renderer.vertex(pos.x() - w, pos.y() - h, pos.z()).color(127, 127, 127).color(127, 127, 127, 255).end();
		renderer.draw(RenderingMode.QUADS, matrix);
	}
}