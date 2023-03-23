package com.alcadra.renderengine.window.widget;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.renderengine.window.Window;

public abstract class Button extends AbstractWidget implements ClickableWidget {
	
	public Button(Vector3i pos, Vector2i size, Window window)
	{
		super(window, pos, size);
	}
}