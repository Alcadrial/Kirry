package com.alcadrial.renderengine.render.rendertest;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.framebuffer.FrameBuffer;
import com.alcadrial.renderengine.render.renderer.RenderingMode;
import com.alcadrial.renderengine.render.renderer.universal.UniversalRenderer;
import com.alcadrial.renderengine.render.renderingmanager.RenderingManager;
import com.alcadrial.renderengine.render.textures.Texture;
import com.alcadrial.renderengine.render.textures.TextureTemplate;
import com.alcadrial.renderengine.render.util.RendererUtil;
import com.alcadrial.renderengine.render.window.Window;
import com.alcadrial.renderengine.render.window.widget.Button;
import com.alcadrial.renderengine.render.window.widget.DrawableWidget;

public abstract class VisibleButton extends Button implements DrawableWidget {
	
	private Texture texture;
	
	public VisibleButton(Vector3i pos, Vector2i size, Window window)
	{
		super(pos, size, window);
	}
	
	@Override
	public void initializeRendering(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		texture = new Texture(TextureTemplate.RGBA_2D_TEXTURE);
		texture.setData2D(TextureTemplate.RGBA_2D_TEXTURE, null, getWidth(), getHeight(), 0);
		texture.disableAntiAlias();
	}
	
	@Override
	public void render(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		try (FrameBuffer frameBuffer = FrameBuffer.pushFrameBuffer(texture))
		{
			UniversalRenderer renderer = manager.startUniversal(RenderingMode.QUADS, UniversalRenderTest.RENDER_ENGINE_TEST.getColorProgram());
			renderer.setIndicesUse(false);
			RendererUtil.loadRectangle(renderer, Vector2f.origin(), Vector2f.of(1, 1), 0, Color.LIGHT_GRAY);
			manager.endUniversal(RenderingMode.QUADS, matrix);
		}
	}
	
	@Override
	public void draw(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		UniversalRenderer renderer = manager.startUniversal(RenderingMode.QUADS, UniversalRenderTest.RENDER_ENGINE_TEST.getTextureProgram());
		texture.bind();
		renderer.setIndicesUse(false);
		RendererUtil.loadTexturedRectangle(renderer, getX(), getY(), getX() + getWidth(), getY() + getHeight(), pos.z(), 0, 0, 1, 1);
		manager.endUniversal(RenderingMode.QUADS, matrix);
	}
	
	@Override
	public void clearRenderingData()
	{
		if (texture != null)
		{
			texture.close();
		}
	}
	
	@Override
	public Vector4i drawingArea(Matrix4f matrix, float partialFrame)
	{
		return null;
	}
	
	@Override
	public boolean onScreen()
	{
		return false;
	}
}