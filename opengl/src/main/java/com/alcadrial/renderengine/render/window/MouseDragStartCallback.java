package com.alcadrial.renderengine.render.window;

public interface MouseDragStartCallback extends ExtraCallback {
	
	void invoke(Window window, int button, double x, double y);
}
