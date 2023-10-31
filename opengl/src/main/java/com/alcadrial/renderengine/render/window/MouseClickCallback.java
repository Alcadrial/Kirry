package com.alcadrial.renderengine.render.window;

public interface MouseClickCallback extends ExtraCallback {
	
	void invoke(Window window, int button, int count, int mods);
}
