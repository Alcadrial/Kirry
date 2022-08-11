package com.alcadra.renderengine.window.widget;

import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.renderengine.window.WindowUpdate;

public abstract class WidgetUpdate implements IWidget {
	
	private WindowUpdate window;
	private Vector3i pos;
	private Vector2i size;
	
	public WidgetUpdate(WindowUpdate window, Vector3i pos, Vector2i size)
	{
		this.window = window;
		this.pos = pos;
		this.size = size;
	}
	
	@Override
	public boolean isMouseHover()
	{
		return isHovered(window.getMousePos());
	}
	
	protected final WindowUpdate getWindow()
	{
		return window;
	}
	
	@Override
	public final Vector3i getPos()
	{
		return pos;
	}
	
	@Override
	public final Vector2i getSize()
	{
		return size;
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
	
	protected final void setWindow(WindowUpdate window)
	{
		this.window = window;
	}
	
	@Override
	public final void setPos(Vector3i pos)
	{
		Vector3i oldPos = this.pos;
		this.pos = move(oldPos, pos);
		onMove(oldPos, pos);
	}
	
	protected Vector3i move(Vector3i oldPos, Vector3i newPos)
	{
		return new Vector3i(new Vector3f(pos.xy()).multiply(newPos).divide(oldPos));
	}
	
	protected void onMove(Vector3i oldPos, Vector3i newPos)
	{
		
	}
	
	@Override
	public final void setSize(Vector2i size)
	{
		Vector2i oldSize = this.size;
		this.size = resize(oldSize, size);
		onResize(oldSize, size);
	}
	
	protected Vector2i resize(Vector2i oldSize, Vector2i newSize)
	{
		return new Vector2i(new Vector2f(size).multiply(newSize).divide(oldSize));
	}
	
	protected void onResize(Vector2i oldSize, Vector2i newSize)
	{
		
	}
}