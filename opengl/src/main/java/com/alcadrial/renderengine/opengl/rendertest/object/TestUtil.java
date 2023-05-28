package com.alcadrial.renderengine.opengl.rendertest.object;

import com.alcadrial.renderengine.opengl.rendertest.UniversalRenderTest;
import com.alcadrial.renderengine.opengl.util.Util;

public class TestUtil extends Util {
	
	public static void printErrorAsync(String s)
	{
		Thread t = new Thread(() -> {
			UniversalRenderTest.getActiveTest().getWindow().makeContextCurrent();
			printError(s);
		});
		t.start();
	}
	
	public static void printErrorSync(String s)
	{
		Thread t = new Thread(() -> {
			UniversalRenderTest.getActiveTest().getWindow().makeContextCurrent();
			printError(s);
		});
		t.start();
		try
		{
			t.join();
		}
		catch (InterruptedException e)
		{}
		UniversalRenderTest.getActiveTest().getWindow().makeContextCurrent();
	}
}
