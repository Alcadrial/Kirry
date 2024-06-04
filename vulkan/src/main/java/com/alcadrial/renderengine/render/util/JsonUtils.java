package com.alcadrial.renderengine.render.util;

import java.io.IOException;

import com.alcadrial.renderengine.render.RenderEngine;
import com.google.gson.stream.JsonReader;

public class JsonUtils {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private JsonUtils()
	{
		
	}
	
	public static <T> T require(T t, String name) throws IOException
	{
		if (t == null) throw new IOException("Missing " + name);
		return t;
	}
	
	public static String missing(JsonReader reader, String object)
	{
		String path = reader.getPath();
		return "Missing " + object + " at " + path.substring(0, path.lastIndexOf("."));
	}
	
	public static String invalid(JsonReader reader, String object)
	{
		String path = reader.getPath();
		return "Invalid " + object + " at " + path.substring(0, path.lastIndexOf("."));
	}
	
	public static String duplicate(JsonReader reader, String object)
	{
		String path = reader.getPath();
		return "Duplicate " + object + " at " + path.substring(0, path.lastIndexOf("."));
	}
}
