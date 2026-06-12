package com.alcadrial.kirry.render.window.widget;

import com.alcadrial.kector.vector.Vector2i;

public interface ClickableWidget extends Widget {
	
	boolean isDragging();
	
	ActionResult onClick(int button, int count, Vector2i mousePos);
	ActionResult onPress(int button, Vector2i mousePos);
	ActionResult onRelease(int button, Vector2i mousePos);
	
	ActionResult onDragStart(Vector2i mousePos);
	ActionResult onDragEnd(Vector2i startPos, Vector2i endPos);
	ActionResult onScroll(double xScroll, double yScroll, Vector2i mousePos);
}
