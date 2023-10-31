package com.alcadrial.renderengine.render.window.widget;

import com.alcadrial.kector.vector.Vector2i;

public interface ClickableWidget extends Widget {
	
	void onClick(int button, int count, Vector2i mousePos);
	void onPress(int button, Vector2i mousePos);
	void onRelease(int button, Vector2i mousePos);
	
	void onDragStart(Vector2i mousePos);
	void onDragEnd(Vector2i mousePos);
	void onDrag(Vector2i startPos, Vector2i endPos);
}
