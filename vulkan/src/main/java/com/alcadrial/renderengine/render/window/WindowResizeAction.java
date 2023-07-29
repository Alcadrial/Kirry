package com.alcadrial.renderengine.render.window;

@FunctionalInterface
public interface WindowResizeAction {
	
	void run(int width, int height);
}
