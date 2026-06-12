package com.alcadrial.kirry.core.callback;

import com.alcadrial.kirry.core.Window;

public interface MouseDragStartCallback extends ExtraCallback {
	
	void invoke(Window window, int button, double x, double y);
}
