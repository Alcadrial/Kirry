package com.alcadrial.renderengine.render.window.event;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.window.ActionResult;

public interface ClickEvent extends Event {
	
	public ActionResult run(int button, int count, Vector2i mousePos);
}
