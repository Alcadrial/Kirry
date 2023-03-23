package com.alcadra.renderengine.window.widget;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;

public interface Widget {
	
	abstract boolean isMouseHover();
	abstract boolean isHovered(Vector2i v);
	
	abstract Vector3i getPos();
	abstract Vector2i getSize();
	abstract int getX();
	abstract int getY();
	abstract int getZ();
	abstract int getWidth();
	abstract int getHeight();
	abstract void setPos(Vector3i pos);
	abstract void setSize(Vector2i size);
}
