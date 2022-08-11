package com.alcadra.renderengine.window;

import com.alcadra.renderengine.util.update.ToMove;

@Deprecated(since = "2.0-migration", forRemoval = true)
@ToMove(".cursor")
public enum CursorInputMode {
	NORMAL,
	HIDDEN,
	DISABLED;
}
