package com.alcadrial.renderengine.render.test.widgets;

import java.util.LinkedList;
import java.util.List;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.window.Screen;
import com.alcadrial.renderengine.render.window.widget.ActionResult;
import com.alcadrial.renderengine.render.window.widget.Widget;

public class BasicScreen implements Screen {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private List<Widget> widgets;
	private RenderPass renderPass;
	
	public BasicScreen()
	{
		widgets = new LinkedList<>();
		renderPass = RenderPassTemplate.colorDepthTextureTemplate(RenderEngine.getVulkan().getWindow().getSurface().getFormat().format()).createRenderPass(RenderEngine.getVulkan().getDevice());
	}
	
	public void add(Widget e)
	{
		widgets.add(e);
	}
	
	@Override
	public RenderPass getRenderPass()
	{
		return renderPass;
	}
	
	@Override
	public void initializeRendering()
	{
		for (Widget widget : widgets) if (!widget.isInitialized()) widget.initializeRendering();
	}
	
	@Override
	public void render(Matrix4f matrix, Renderer renderer)
	{
		for (Widget widget : widgets) if (!widget.isRendered()) widget.render(matrix, renderer);
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer, float partialFrame)
	{
		
		for (Widget widget : widgets) if (!widget.isOnScreen()) widget.draw(matrix, commandBuffer, renderer, partialFrame);
	}
	
	@Override
	public void clearRenderingData()
	{
		
	}
	
	@Override
	public boolean isOnScreen()
	{
		return false;
	}
	
	@Override
	public boolean isRendered()
	{
		return false;
	}
	
	@Override
	public boolean isInitialized()
	{
		return false;
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
}
