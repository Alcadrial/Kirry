package com.alcadrial.renderengine.core.callback;

import com.alcadrial.renderengine.core.Window;

public interface MouseDragStartCallback extends ExtraCallback {
	
	void invoke(Window window, int button, double x, double y);
}
