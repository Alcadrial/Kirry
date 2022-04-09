package com.alcadra.renderengine.window.widget;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.renderengine.renderer.ColorRenderer;
import com.alcadra.renderengine.renderer.TextureRenderer;

public interface RenderizableWidget {
	
	void render(Matrix4f matrix, ColorRenderer colorRenderer, TextureRenderer textureRenderer, Vector2i pos, float partialTicks);
}
