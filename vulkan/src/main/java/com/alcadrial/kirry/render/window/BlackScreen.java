package com.alcadrial.kirry.render.window;

import org.jspecify.annotations.NonNull;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class BlackScreen implements Screen {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public static final Screen BLACK_SCREEN = new BlackScreen();
	
	private BlackScreen()
	{}
	
	@Override
	public void init(MemoryManager memoryManager, CommandBuffer commandBuffer)
	{}
	
	@Override
	public void render(Matrix4f matrix, CommandBuffer commandBuffer)
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
		//Not used if #isOnScreen() is always true
		return (@NonNull RenderPass) null;
	}
	
	@Override
	public void setWindow(VulkanWindow window)
	{}
	
	@Override
	public void registerEvents(EventRegistry events)
	{}
	
	@Override
	public void close()
	{}
}
