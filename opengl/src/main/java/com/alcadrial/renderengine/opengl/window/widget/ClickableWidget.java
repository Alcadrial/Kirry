package com.alcadrial.renderengine.opengl.window.widget;

import com.alcadrial.kector.vector.Vector2i;

public interface ClickableWidget extends Widget {
	
	default void onClick(int button, Vector2i mousePos)
	{
		if (button == 0) onLeftClick(mousePos);
		else if (button == 1) onRightClick(mousePos);
	}
	
	void onLeftClick(Vector2i mousePos);
	void onRightClick(Vector2i mousePos);
}
