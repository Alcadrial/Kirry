package com.alcadra.renderengine.window.widget;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.renderengine.renderer.ColorRenderer;
import com.alcadra.renderengine.window.Window;

public abstract class VisibleButton extends Button implements DrawableWidget<ColorRenderer> {
	
	public VisibleButton(Vector3i pos, Vector2i size, Window window)
	{
		super(pos, size, window);
	}
	
	@Override
	public void render(Matrix4f matrix, ColorRenderer renderer, Vector2i pos, float partialTicks)
	{
		
	}
	
	@Override
	public void draw(Matrix4f matrix, ColorRenderer renderer, float partialTicks)
	{
		renderer.color(127, 127, 127, 255);
		float w = getWindow().getWidth() / 2, h = getWindow().getHeight() / 2;
		Vector3i pos = getPos();
		renderer.loadQuad(pos.x() - w, pos.y() - h, pos.x() - w + 16, pos.y() - h + 16, pos.z());
		renderer.draw(7, matrix);
	}
}
