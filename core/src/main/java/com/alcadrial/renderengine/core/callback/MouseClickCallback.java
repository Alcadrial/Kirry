package com.alcadrial.renderengine.core.callback;

import com.alcadrial.renderengine.core.Window;

public interface MouseClickCallback extends ExtraCallback {
	
	void invoke(Window window, int button, int count, int mods);
}
