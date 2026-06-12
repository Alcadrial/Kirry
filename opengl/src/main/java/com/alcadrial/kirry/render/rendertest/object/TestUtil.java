package com.alcadrial.kirry.render.rendertest.object;

import com.alcadrial.kirry.render.rendertest.UniversalRenderTest;
import com.alcadrial.kirry.render.util.Util;

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
