package com.alcadra.renderengine.window.widget;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.renderengine.util.update.UpdatedTo;
import com.alcadra.renderengine.window.Window;

@Deprecated(since = "2.0-migration", forRemoval = true)
@UpdatedTo("ButtonUpdate")
public abstract class Button extends Widget implements ClickableWidget {
	
	public Button(Vector3i pos, Vector2i size, Window window)
	{
		super(window, pos, size);
	}
}