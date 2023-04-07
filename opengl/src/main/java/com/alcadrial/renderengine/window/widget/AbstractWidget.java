package com.alcadrial.renderengine.window.widget;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.window.Window;

public abstract class AbstractWidget implements Widget {
	
	protected Window window;
	protected Vector3i pos;
	protected Vector2i size;
	
	public AbstractWidget(Window window, Vector3i pos, Vector2i size)
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
	
	@Override
	public boolean isHovered(Vector2i v)
	{
		return v.x() > pos.x() && v.x() < pos.x() + size.x() && v.y() > pos.y() && v.y() < pos.y() + size.y();
	}
	
	@Override
	public Vector3i getPos()
	{
		return Vector3i.copyOf(pos);
	}
	
	@Override
	public Vector2i getSize()
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
	public int getWidth()
	{
		return size.x();
	}
	
	@Override
	public int getHeight()
	{
		return size.y();
	}
	
	@Override
	public void setPos(Vector3i pos)
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
	public void setSize(Vector2i size)
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