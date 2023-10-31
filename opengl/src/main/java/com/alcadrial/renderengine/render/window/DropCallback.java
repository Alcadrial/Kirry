package com.alcadrial.renderengine.render.window;

public interface DropCallback extends ExtraCallback {
	
	void invoke(Window window, String... names);
}
