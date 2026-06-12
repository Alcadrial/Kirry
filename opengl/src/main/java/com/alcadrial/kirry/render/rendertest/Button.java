package com.alcadrial.kirry.render.rendertest;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kirry.core.util.color.Color;
import com.alcadrial.kirry.render.KirryOpenGl;
import com.alcadrial.kirry.render.buffer.FloatArrayBuffer;
import com.alcadrial.kirry.render.framebuffer.FrameBuffer;
import com.alcadrial.kirry.render.renderer.RenderingMode;
import com.alcadrial.kirry.render.renderer.universal.UniversalRenderer;
import com.alcadrial.kirry.render.renderingmanager.RenderingManager;
import com.alcadrial.kirry.render.textures.Texture;
import com.alcadrial.kirry.render.util.RendererUtil;
import com.alcadrial.kirry.render.window.OpenGlWindow;
import com.alcadrial.kirry.render.window.widget.AbstractWidget;
import com.alcadrial.kirry.render.window.widget.DrawableWidget;

public abstract class Button extends AbstractWidget implements DrawableWidget {
	
	private Texture texture;
	private FloatArrayBuffer buffer;
	
	public Button(Vector3i pos, Vector2i size, OpenGlWindow window)
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
		try (var s = KirryOpenGl.configure().cullFace(false).setFrameBuffer(FrameBuffer.createFrameBuffer(getSize())).apply())
		{
			UniversalRenderer renderer = manager.startUniversal(RenderingMode.QUADS, UniversalRenderTest.RENDER_ENGINE_TEST.getColorProgram());
			renderer.setIndicesUse(false);
			RendererUtil.loadRectangle(renderer, Vector2f.of(-1, -1), Vector2f.of(1, 1), 0, Color.mix(Color.RED, Color.YELLOW));
			manager.endUniversal(RenderingMode.QUADS, Matrix4f.IDENTITY);
			texture = FrameBuffer.getBinded().retriveColorTexture();
			
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
		try (var s = KirryOpenGl.configure().cullFace(false).apply())
		{
			UniversalRenderer renderer = manager.startUniversal(RenderingMode.QUADS, UniversalRenderTest.RENDER_ENGINE_TEST.getTextureProgram());
			texture.bind();
			renderer.setIndicesUse(false);
			renderer.pushBuffer(buffer);
			manager.endUniversal(RenderingMode.QUADS, matrix);
			renderer.pullBuffer();
		}
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