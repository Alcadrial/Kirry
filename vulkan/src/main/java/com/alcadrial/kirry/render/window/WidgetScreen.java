package com.alcadrial.kirry.render.window;

import java.util.LinkedList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class WidgetScreen implements Screen {
	
	private List<Widget> widgets;
	
	public WidgetScreen()
	{
		widgets = new LinkedList<>();
	}
	
	@Override
	public void setWindow(VulkanWindow window)
	{}
	
	@Override
	public void init(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException
	{
		
	}
	
	@Override
	public void render(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer, float elapsed)
	{}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed) throws VulkanException
	{}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer) throws VulkanException
	{}
	
	@Override
	public void clearRenderingData()
	{}
	
	@Override
	public boolean isOnSurface()
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
	{}
	
	@Override
	public void requestDraw()
	{}
	
	@Override
	public boolean isDragging()
	{
		return false;
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
		for (Widget widget : widgets)
		{
			widget.registerEvents(registry);
		}
	}
	
	@Override
	public ActionResult onClick(int button, int count, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onPress(int button, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onRelease(int button, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public void onMouseMove(Vector2ir oldPos, Vector2ir newPos)
	{}
	
	@Override
	public ActionResult onDragStart(Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onDragEnd(Vector2ir startPos, Vector2ir endPos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onScroll(double xScroll, double yScroll, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public RenderPass getRenderPass()
	{
		return (@NonNull RenderPass) null;
	}
	
	@Override
	public void close() throws VulkanException
	{}
}
