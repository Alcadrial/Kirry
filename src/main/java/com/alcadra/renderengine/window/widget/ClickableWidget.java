package com.alcadra.renderengine.window.widget;

import com.alcadra.math.vector.Vector2i;

public interface ClickableWidget extends IWidget {
	
	default void onClick(int button, Vector2i mousePos)
	{
		if (button == 0) onLeftClick(mousePos);
		else if (button == 1) onRightClick(mousePos);
	}
	
	void onLeftClick(Vector2i mousePos);
	void onRightClick(Vector2i mousePos);
}
