package com.alcadrial.renderengine.render.window.widget;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;

public interface Widget {
	
	boolean isMouseHover();
	boolean isHovered(Vector2i v);
	void onMouseEnter(Vector2i enterPos);
	void onMouseExit(Vector2i exitPos);
	
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
