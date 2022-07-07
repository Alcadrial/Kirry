package com.alcadra.renderengine.window.widget;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;

public interface IWidget {
	
	public abstract boolean isMouseHover();
	
	public default boolean isHovered(Vector2i v)
	{
		Vector3i pos = getPos();
		Vector2i size = getSize();
		return v.x() > pos.x() && v.x() < pos.x() + size.x() && v.y() > pos.y() && v.y() < pos.y() + size.y();
	}
	
	public abstract Vector3i getPos();
	public abstract Vector2i getSize();
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract void setPos(Vector3i pos);
	public abstract void setSize(Vector2i size);
}
