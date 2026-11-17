package com.alcadrial.kirry.render.window.event;

import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kirry.render.window.ActionResult;

@FunctionalInterface
public interface ClickEvent extends Event {
	
	public ActionResult run(int button, int count, Vector2ir mousePos);
}
