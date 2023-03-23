package com.alcadra.renderengine.rendertest;

import com.alcadra.renderengine.window.Window;

public interface RenderTest {
	
	public void start();
	public void restart();
	public void loop(float partialTicks);
	
	public Window getWindow();
}
