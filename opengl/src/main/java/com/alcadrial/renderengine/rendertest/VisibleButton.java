package com.alcadrial.renderengine.rendertest;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.renderer.RenderingMode;
import com.alcadrial.renderengine.renderer.universal.UniversalRenderer;
import com.alcadrial.renderengine.renderingmanager.RenderingManager;
import com.alcadrial.renderengine.window.Window;
import com.alcadrial.renderengine.window.widget.Button;
import com.alcadrial.renderengine.window.widget.DrawableWidget;

public abstract class VisibleButton extends Button implements DrawableWidget {
	
	public VisibleButton(Vector3i pos, Vector2i size, Window window)
	{
		super(pos, size, window);
	}
	
	@Override
	public void render(Matrix4f matrix, RenderingManager manager, Vector2i pos, float partialTicks)
	{
		
	}
	
	@Override
	public void draw(Matrix4f matrix, RenderingManager manager, float partialTicks)
	{
		UniversalRenderer renderer = manager.startUniversal(RenderingMode.QUADS, UniversalRenderTest.RENDER_ENGINE_TEST.getColorProgram());
		renderer.setIndicesUse(false);
		float w = window.getWidth() / 2;
		float h = window.getHeight() / 2;
		renderer.vertex(pos.x() - w, pos.y() - h + 16, pos.z()).color(127, 127, 127).color(127, 127, 127, 255).end();
		renderer.vertex(pos.x() - w + 16, pos.y() - h + 16, pos.z()).color(127, 127, 127).color(127, 127, 127, 255).end();
		renderer.vertex(pos.x() - w + 16, pos.y() - h, pos.z()).color(127, 127, 127).color(127, 127, 127, 255).end();
		renderer.vertex(pos.x() - w, pos.y() - h, pos.z()).color(127, 127, 127).color(127, 127, 127, 255).end();
		manager.endUniversal(RenderingMode.QUADS, matrix);
	}
}