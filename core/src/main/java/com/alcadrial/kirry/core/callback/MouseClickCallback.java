package com.alcadrial.kirry.core.callback;

import com.alcadrial.kirry.core.Window;

public interface MouseClickCallback extends ExtraCallback {
	
	void invoke(Window window, int button, int count, int mods);
}
