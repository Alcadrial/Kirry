package com.alcadrial.renderengine.render.rendertest.object;

import com.alcadrial.renderengine.render.window.Window;

public interface RenderTest {
	
	public void start();
	public void restart();
	public void loop(float partialTicks);
	
	public Window getWindow();
}
