package com.alcadrial.renderengine.opengl.window.widget;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;

public interface Widget {
	
	boolean isMouseHover();
	boolean isHovered(Vector2i v);
	
	Vector3i getPos();
	Vector2i getSize();
	int getX();
	int getY();
	int getZ();
	int getWidth();
	int getHeight();
	void setPos(Vector3i pos);
	void setSize(Vector2i size);
}
