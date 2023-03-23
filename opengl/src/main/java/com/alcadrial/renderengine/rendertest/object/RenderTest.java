package com.alcadrial.renderengine.rendertest.object;

import com.alcadrial.renderengine.window.Window;

public interface RenderTest {
	
	public void start();
	public void restart();
	public void loop(float partialTicks);
	
	public Window getWindow();
}
