package com.alcadrial.renderengine.render.util;

import java.io.File;

import com.alcadrial.renderengine.render.RenderEngine;

public class FileUtils {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static String getExtension(File file)
	{
		String pathname = file.getAbsolutePath();
		return pathname.substring(pathname.lastIndexOf('.') + 1);
	}
	
	public static void assertExtension(File file, String extension)
	{
		if (!getExtension(file).equals(extension)) throw new AssertionError("File " + file + " is not a ." + extension + " file");
	}
}
