package com.alcadrial.renderengine.render.window.widget;

import org.lwjgl.glfw.GLFW;

import com.alcadrial.kector.vector.Vector2i;

public interface ClickableWidget extends Widget {
	
	default void onClick(int button, Vector2i mousePos)
	{
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) onLeftClick(mousePos);
		else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) onRightClick(mousePos);
	}
	
	void onLeftClick(Vector2i mousePos);
	void onRightClick(Vector2i mousePos);
}
