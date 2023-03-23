package com.alcadra.renderengine.window.widget;

import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.renderengine.window.Window;

public abstract class Widget {
	
	private Window window;
	private Vector3i pos;
	private Vector2i size;
	
	public Widget(Window window, Vector3i pos, Vector2i size)
	{
		this.window = window;
		this.pos = pos;
		this.size = size;
	}
	
	public final boolean isMouseHovered()
	{
		return isHovered(window.getMousePos());
	}
	
	public boolean isHovered(Vector2i v)
	{
		return v.x() > pos.x() && v.x() < pos.x() + size.x() && v.y() > pos.y() && v.y() < pos.y() + size.y();
	}
	
	protected Vector2i resize(Vector2i oldSize, Vector2i newSize)
	{
		return new Vector2i(new Vector2f(size).multiply(newSize).divide(oldSize));
	}
	
	protected void onResize(Vector2i oldSize, Vector2i newSize)
	{
		
	}
	
	protected final Window getWindow()
	{
		return window;
	}
	
	public final Vector3i getPos()
	{
		return pos;
	}
	
	public final Vector2i getSize()
	{
		return size;
	}
	
	public final int getWidth()
	{
		return size.x();
	}
	
	public final int getHeight()
	{
		return size.y();
	}
	
	public final void setWindow(Window window)
	{
		this.window = window;
	}
	
	public final void setPos(Vector3i pos)
	{
		this.pos = pos;
	}
	
	public final void setSize(Vector2i size)
	{
		Vector2i oldSize = this.size;
		this.size = resize(oldSize, size);
		onResize(oldSize, size);
	}
}