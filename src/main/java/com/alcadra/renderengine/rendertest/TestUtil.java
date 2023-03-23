package com.alcadra.renderengine.rendertest;

import com.alcadra.renderengine.RenderEngine;
import com.alcadra.renderengine.util.Util;

public class TestUtil extends Util {
	
	public static void printErrorAsync(String s)
	{
		Thread t = new Thread(() -> {
			RenderEngine.getActiveTest().getWindow().makeContextCurrent();
			printError(s);
		});
		t.start();
	}
	
	public static void printErrorSync(String s)
	{
		Thread t = new Thread(() -> {
			RenderEngine.getActiveTest().getWindow().makeContextCurrent();
			printError(s);
		});
		t.start();
		try
		{
			t.join();
		}
		catch (InterruptedException e)
		{}
		RenderEngine.getActiveTest().getWindow().makeContextCurrent();
	}
}
