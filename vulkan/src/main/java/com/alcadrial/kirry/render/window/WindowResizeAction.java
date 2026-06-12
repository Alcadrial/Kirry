package com.alcadrial.kirry.render.window;

@FunctionalInterface
public interface WindowResizeAction {
	
	void run(int width, int height);
}
