package com.alcadra.renderengine.rendertest.object;

import com.alcadra.renderengine.rendertest.UniversalRenderTest;
import com.alcadra.renderengine.util.Util;

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
