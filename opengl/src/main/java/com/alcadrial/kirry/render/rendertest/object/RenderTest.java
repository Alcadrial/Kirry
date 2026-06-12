package com.alcadrial.kirry.render.rendertest.object;

import com.alcadrial.kirry.render.window.OpenGlWindow;

public interface RenderTest {
	
	public void start();
	public void restart();
	public void loop(float partialSecond);
	
	public OpenGlWindow getWindow();
}
