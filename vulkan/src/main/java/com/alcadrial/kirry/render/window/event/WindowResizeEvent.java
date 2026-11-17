package com.alcadrial.kirry.render.window.event;

@FunctionalInterface
public interface WindowResizeEvent extends Event {
	
	void run(int newWidth, int newHeight);
}
