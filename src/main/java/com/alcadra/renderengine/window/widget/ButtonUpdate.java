package com.alcadra.renderengine.window.widget;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.renderengine.util.update.UpdatedFrom;
import com.alcadra.renderengine.window.WindowUpdate;

@UpdatedFrom("ButtonUpdate")
public abstract class ButtonUpdate extends WidgetUpdate implements ClickableWidget {
	
	public ButtonUpdate(Vector3i pos, Vector2i size, WindowUpdate window)
	{
		super(window, pos, size);
	}
}