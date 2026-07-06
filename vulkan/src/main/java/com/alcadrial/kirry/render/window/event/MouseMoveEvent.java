package com.alcadrial.kirry.render.window.event;

@FunctionalInterface
public interface MouseMoveEvent extends Event {
	
	void run(double oldX, double oldY, double newX, double newY, double dx, double dy);
}
