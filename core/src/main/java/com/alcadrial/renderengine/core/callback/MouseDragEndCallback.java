package com.alcadrial.renderengine.core.callback;

import com.alcadrial.renderengine.core.Window;

public interface MouseDragEndCallback extends ExtraCallback {
	
	void invoke(Window window, int button, double x, double y);
}
