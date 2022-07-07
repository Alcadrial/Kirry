package com.alcadra.renderengine.window.widget;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.renderengine.renderer.ColorRenderer;
import com.alcadra.renderengine.window.Window;

public abstract class VisibleButton extends Button implements RenderizableWidget<ColorRenderer> {
	
	public VisibleButton(Vector3i pos, Vector2i size, Window window)
	{
		super(pos, size, window);
	}
	
	@Override
	public void render(Matrix4f matrix, ColorRenderer colorRenderer, Vector2i pos, float partialTicks)
	{
		colorRenderer.color(127, 127, 127, 255);
		float w = getWindow().getWidth() / 2, h = getWindow().getHeight() / 2;
		colorRenderer.loadQuad(pos.x() - w, pos.y() - h, pos.x() - w + 16, pos.y() - h + 16, pos.z());
		colorRenderer.draw(7, matrix);
	}
}
