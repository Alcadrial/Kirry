package com.alcadrial.renderengine.render.util;

import org.lwjgl.vulkan.VK10;

public class VulkanErrorUtils {
	
	public static <O> void match(O obj, O other)
	{
		if (!obj.equals(other)) throw new DataIncosistenceException();
	}
	
	public static <O> void match(O obj, O other, String error)
	{
		if (!obj.equals(other)) throw new DataIncosistenceException(error);
	}
	
	public static <O> O nonNull(O obj)
	{
		if (obj == null) throw new NullPointerException();
		return obj;
	}
	
	public static <O> O nonNull(O obj, String error)
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
	
	public static void printError(int error)
	{
		if (error < 0) System.out.println(getErrorString(error));
	}
	
	public static void assertNoError(int error, String details)
	{
		if (error < 0) throw new AssertionError(details + ": " + getErrorString(error));
	}
	
	public static void assertNoError(int error)
	{
		if (error < 0) throw new AssertionError(getErrorString(error));
	}
	
	public static String getErrorString(int error)
	{
		return switch (error)
		{
			case VK10.VK_ERROR_OUT_OF_HOST_MEMORY -> "ERROR OUT OF HOST MEMORY";
			case VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY -> "ERROR OUT OF DEVICE MEMORY";
			case VK10.VK_ERROR_INITIALIZATION_FAILED -> "ERROR INITIALIZATION FAILED";
			case VK10.VK_ERROR_DEVICE_LOST -> "ERROR DEVICE LOST";
			case VK10.VK_ERROR_MEMORY_MAP_FAILED -> "ERROR MEMORY MAP FAILED";
			case VK10.VK_ERROR_LAYER_NOT_PRESENT -> "ERROR LAYER NOT PRESENT";
			case VK10.VK_ERROR_EXTENSION_NOT_PRESENT -> "ERROR EXTENSION NOT PRESENT";
			case VK10.VK_ERROR_FEATURE_NOT_PRESENT -> "ERROR FEATURE NOT PRESENT";
			case VK10.VK_ERROR_INCOMPATIBLE_DRIVER -> "ERROR INCOMPATIBLE DRIVER";
			case VK10.VK_ERROR_TOO_MANY_OBJECTS -> "ERROR TOO MANY OBJECTS";
			case VK10.VK_ERROR_FORMAT_NOT_SUPPORTED -> "ERROR FORMAT NOT SUPPORTED";
			case VK10.VK_ERROR_FRAGMENTED_POOL -> "ERROR FRAGMENTED POOL";
			default -> "UNKNOWN ERROR";
		};
	}
}
