package com.alcadrial.kirry.core.callback;

import com.alcadrial.kirry.core.Window;

public interface DropCallback extends ExtraCallback {
	
	void invoke(Window window, String... names);
}
