package com.alcadrial.renderengine.render.window;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.window.widget.ActionResult;

public class BlackScreen implements Screen {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static final Screen BLACK_SCREEN = new BlackScreen();
	
	private BlackScreen()
	{
		
	}
	
	@Override
	public void initializeRendering(StaticBigBuffer staticBigBuffer)
	{
		
	}
	
	@Override
	public void render(Matrix4f matrix, Renderer renderer)
	{
		
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer, float partialFrame)
	{
		
	}
	
	@Override
	public void clearRenderingData()
	{
		
	}
	
	@Override
	public boolean isOnScreen()
	{
		return true;
	}
	
	@Override
	public boolean isRendered()
	{
		return true;
	}
	
	@Override
	public boolean isInitialized()
	{
		return true;
	}
	
	@Override
	public void requestRender()
	{
		
	}
	
	@Override
	public void requestDraw()
	{
		
	}
	
	@Override
	public boolean isDragging()
	{
		return false;
	}
	
	@Override
	public ActionResult onClick(int button, int count, Vector2i mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onPress(int button, Vector2i mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onRelease(int button, Vector2i mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public void onMouseMove(Vector2i oldPos, Vector2i newPos)
	{
		
	}
	
	@Override
	public ActionResult onDragStart(Vector2i mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onDragEnd(Vector2i startPos, Vector2i endPos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onScroll(double xScroll, double yScroll, Vector2i mousePos)
	{
		return ActionResult.CONSUME;
	}

	@Override
	public RenderPass getRenderPass()
	{
		return null;
	}
}
