package com.alcadrial.renderengine.opengl.window.widget;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.opengl.window.Window;

public abstract class Button extends AbstractWidget implements ClickableWidget {
	
	public Button(Vector3i pos, Vector2i size, Window window)
	{
		super(window, pos, size);
	}
}