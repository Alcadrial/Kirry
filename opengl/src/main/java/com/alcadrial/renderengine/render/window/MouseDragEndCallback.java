package com.alcadrial.renderengine.render.window;

public interface MouseDragEndCallback extends ExtraCallback {
	
	void invoke(Window window, int button, double x, double y);
}
