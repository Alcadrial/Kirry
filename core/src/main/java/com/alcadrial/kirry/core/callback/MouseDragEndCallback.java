package com.alcadrial.kirry.core.callback;

import com.alcadrial.kirry.core.Window;

public interface MouseDragEndCallback extends ExtraCallback {
	
	void invoke(Window window, int button, double x, double y);
}
