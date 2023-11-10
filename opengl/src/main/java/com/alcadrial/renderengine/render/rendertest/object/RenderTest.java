package com.alcadrial.renderengine.render.rendertest.object;

import com.alcadrial.renderengine.render.window.OpenGlWindow;

public interface RenderTest {
	
	public void start();
	public void restart();
	public void loop(float partialSecond);
	
	public OpenGlWindow getWindow();
}
