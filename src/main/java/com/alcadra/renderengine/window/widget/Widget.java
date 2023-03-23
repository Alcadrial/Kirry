package com.alcadra.renderengine.window.widget;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.renderengine.window.Window;

public abstract class Widget implements IWidget {
	
	private Window window;
	private Vector3i pos;
	private Vector2i size;
	
	public Widget(Window window, Vector3i pos, Vector2i size)
	{
		this.window = window;
		this.pos = Vector3i.copyOf(pos);
		this.size = Vector2i.copyOf(size);
	}
	
	@Override
	public boolean isMouseHover()
	{
		return isHovered(window.getMousePos());
	}
	
	protected final Window getWindow()
	{
		return window;
	}
	
	@Override
	public final Vector3i getPos()
	{
		return Vector3i.copyOf(pos);
	}
	
	@Override
	public final Vector2i getSize()
	{
		return Vector2i.copyOf(size);
	}
	
	@Override
	public int getX()
	{
		return pos.x();
	}
	
	@Override
	public int getY()
	{
		return pos.y();
	}
	
	@Override
	public int getZ()
	{
		return pos.z();
	}
	
	@Override
	public final int getWidth()
	{
		return size.x();
	}
	
	@Override
	public final int getHeight()
	{
		return size.y();
	}
	
	protected final void setWindow(Window window)
	{
		this.window = window;
	}
	
	@Override
	public final void setPos(Vector3i pos)
	{
		Vector3i oldPos = Vector3i.copyOf(this.pos);
		move(pos);
		this.pos = pos.xyz();
		onMove(oldPos, pos);
	}
	
	protected void move(Vector3i newPos)
	{
		
	}
	
	protected void onMove(Vector3i oldPos, Vector3i newPos)
	{
		
	}
	
	@Override
	public final void setSize(Vector2i size)
	{
		Vector2i oldSize = Vector2i.copyOf(this.size.xy());
		resize(size);
		this.size = size.xy();
		onResize(oldSize, size);
	}
	
	protected void resize(Vector2i newSize)
	{
		
	}
	
	protected void onResize(Vector2i oldSize, Vector2i newSize)
	{
		
	}
}