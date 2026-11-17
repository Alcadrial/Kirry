package com.alcadrial.kirry.render.window.event;

@FunctionalInterface
public interface KeyboardEvent extends Event {
	
	void run(int key, int scancode, int action, int mods);
}
