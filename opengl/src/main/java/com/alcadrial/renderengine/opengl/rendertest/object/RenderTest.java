package com.alcadrial.renderengine.opengl.rendertest.object;

import com.alcadrial.renderengine.opengl.window.Window;

public interface RenderTest {
	
	public void start();
	public void restart();
	public void loop(float partialTicks);
	
	public Window getWindow();
}
