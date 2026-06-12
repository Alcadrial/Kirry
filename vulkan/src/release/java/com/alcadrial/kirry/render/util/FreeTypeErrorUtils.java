package com.alcadrial.kirry.render.util;

import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.lwjgl.util.freetype.FreeType;

public class FreeTypeErrorUtils {
	
	public static <O> void match(O obj, O other)
	{}
	
	public static <O> void match(O obj, O other, String error)
	{}
	
	public static <O> @NonNull O nonNull(O obj)
	{
		return obj;
	}
	
	public static <O> @NonNull O nonNull(O obj, String error)
	{
		return obj;
	}
	
	public static int nonNull(int id)
	{
		return id;
	}
	
	public static int nonNull(int id, String error)
	{
		return id;
	}
	
	public static long nonNull(long pointer)
	{
		return pointer;
	}
	
	public static long nonNull(long pointer, String error)
	{
		return pointer;
	}
	
	public static void assertNoError(int error, Supplier<String> details)
	{}
	
	public static void assertNoError(int error, String details)
	{}
	
	public static void assertNoError(int error)
	{}
	
	public static String getErrorString(int error)
	{
		String message = FreeType.FT_Error_String(error);
		if (message == null) message = "Unknown";
		return message;
	}
}
