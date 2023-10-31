package com.alcadrial.renderengine.render.rendertest;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.FloatArrayBuffer;
import com.alcadrial.renderengine.render.framebuffer.FrameBuffer;
import com.alcadrial.renderengine.render.renderer.RenderingMode;
import com.alcadrial.renderengine.render.renderer.universal.UniversalRenderer;
import com.alcadrial.renderengine.render.renderingmanager.RenderingManager;
import com.alcadrial.renderengine.render.textures.Texture;
import com.alcadrial.renderengine.render.util.RendererUtil;
import com.alcadrial.renderengine.render.window.Window;
import com.alcadrial.renderengine.render.window.widget.AbstractWidget;
import com.alcadrial.renderengine.render.window.widget.DrawableWidget;

public abstract class Button extends AbstractWidget implements DrawableWidget {
	
	private Texture texture;
	private FloatArrayBuffer buffer;
	
	public Button(Vector3i pos, Vector2i size, Window window)
	{
		super(window, pos, size);
	}
	
	@Override
	public void initializeRendering(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		buffer = new FloatArrayBuffer(64);
	}
	
	@Override
	public void render(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		try (FrameBuffer frameBuffer = FrameBuffer.pushFrameBuffer(getSize(), true, false, false))
		{
			RenderEngine.disableCullFace();
			UniversalRenderer renderer = manager.startUniversal(RenderingMode.QUADS, UniversalRenderTest.RENDER_ENGINE_TEST.getColorProgram());
			renderer.setIndicesUse(false);
			RendererUtil.loadRectangle(renderer, Vector2f.of(-1, -1), Vector2f.of(1, 1), 0, Color.mix(Color.RED, Color.YELLOW));
			manager.endUniversal(RenderingMode.QUADS, Matrix4f.IDENTITY);
			RenderEngine.enableCullFace();
			texture = frameBuffer.retriveColorTexture();
			
			renderer.clear();
			renderer.setShaderProgram(UniversalRenderTest.RENDER_ENGINE_TEST.getTextureProgram());
			renderer.pushBuffer(buffer);
			RendererUtil.loadTexturedRectangle(renderer, getX(), getY(), getX() + getWidth(), getY() + getHeight(), pos.z(), 0, 0, 1, 1);
			buffer.trimToSize();
			renderer.pullBuffer();
		}
	}
	
	@Override
	public void draw(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		RenderEngine.disableCullFace();
		UniversalRenderer renderer = manager.startUniversal(RenderingMode.QUADS, UniversalRenderTest.RENDER_ENGINE_TEST.getTextureProgram());
		texture.bind();
		renderer.setIndicesUse(false);
		renderer.pushBuffer(buffer);
		manager.endUniversal(RenderingMode.QUADS, matrix);
		renderer.pullBuffer();
		RenderEngine.enableCullFace();
	}
	
	@Override
	public void clearRenderingData()
	{
		if (texture != null)
		{
			texture.close();
		}
	}
}