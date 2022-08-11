package com.alcadra.renderengine.rendertest;

import com.alcadra.renderengine.util.Test;
import com.alcadra.renderengine.window.WindowUpdate;

@Test
public interface RenderTest {
	
	public void start();
	public void restart();
	public void loop(float partialTicks);
	
	public WindowUpdate getWindow();
}
