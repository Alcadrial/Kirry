package com.alcadrial.renderengine.render.util;

import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.lwjgl.util.freetype.FreeType;

import com.alcadrial.renderengine.core.util.DataIncosistenceException;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.exception.FreeTypeException;

public class FreeTypeErrorUtils {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static <O> void match(O obj, O other)
	{
		if (!obj.equals(other)) throw new DataIncosistenceException();
	}
	
	public static <O> void match(O obj, O other, String error)
	{
		if (!obj.equals(other)) throw new DataIncosistenceException(error);
	}
	
	public static <O> @NonNull O nonNull(O obj)
	{
		if (obj == null) throw new NullPointerException();
		return obj;
	}
	
	public static <O> @NonNull O nonNull(O obj, String error)
	{
		if (obj == null) throw new NullPointerException(error);
		return obj;
	}
	
	public static int nonNull(int id)
	{
		if (id == 0) throw new NullPointerException();
		return id;
	}
	
	public static int nonNull(int id, String error)
	{
		if (id == 0) throw new NullPointerException(error);
		return id;
	}
	
	public static long nonNull(long pointer)
	{
		if (pointer == 0) throw new NullPointerException();
		return pointer;
	}
	
	public static long nonNull(long pointer, String error)
	{
		if (pointer == 0) throw new NullPointerException(error);
		return pointer;
	}
	
	public static void assertNoError(int error, Supplier<String> details) throws FreeTypeException
	{
		if (error < 0) throw new FreeTypeException(error, details.get() + ": " + getErrorString(error));
	}
	
	public static void assertNoError(int error, String details) throws FreeTypeException
	{
		if (error < 0) throw new FreeTypeException(error, details + ": " + getErrorString(error));
	}
	
	public static void assertNoError(int error) throws FreeTypeException
	{
		if (error < 0) throw new FreeTypeException(error, getErrorString(error));
	}
	
	public static String getErrorString(int error)
	{
		String message = FreeType.FT_Error_String(error);
		if (message == null) message = "Unknown";
		return message;
	}
}
