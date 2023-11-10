package com.alcadrial.renderengine.core.callback;

import com.alcadrial.renderengine.core.Window;

public interface DropCallback extends ExtraCallback {
	
	void invoke(Window window, String... names);
}
