package com.alcadrial.renderengine.render.test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;

import com.alcadrial.renderengine.render.util.Properties;

public class RenderTest {
	
	protected static final Object MONITOR = new Object();
	private static final String[] SEVERITIES = new String[] {"UNKNOWN", "VERBOSE", "INFO", "WARNING", "ERROR"};
	private static final String[] TYPES = new String[] {"UNKNOWN", "GENERAL", "VALIDATION", "PERFORMANCE"};
	
	public static int debugCallback(int messageSeverity, int messageType, long callbackDataId, long userDataId)
	{
		VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataId);
		
		int i = switch (messageSeverity)
		{
			case 0x0001 -> 1;
			case 0x0010 -> 2;
			case 0x0100 -> 3;
			case 0x1000 -> 4;
			default -> 0;
		};
		int j = switch (messageType)
		{
			case 0b001 -> 1;
			case 0b010 -> 2;
			case 0b100 -> 3;
			default -> 0;
		};
		
		System.out.println("[Vulkan debug/" + SEVERITIES[i] + " " + TYPES[j] + "] " + callbackData.pMessageString());
		
		return VK10.VK_FALSE;
	}
	
	public static File getFile(String path)
	{
		return new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/" + path);
	}
	
	public static long recordTime(Runnable runnable, TimeUnit unit)
	{
		long startTime;
		TimeUnit localUnit;
		if (unit == TimeUnit.MICROSECONDS || unit == TimeUnit.NANOSECONDS)
		{
			startTime = System.nanoTime();
			localUnit = TimeUnit.NANOSECONDS;
		}
		else
		{
			startTime = System.currentTimeMillis();
			localUnit = TimeUnit.MILLISECONDS;
		}
		
		runnable.run();
		
		long endTime;
		if (localUnit == TimeUnit.NANOSECONDS) endTime = System.nanoTime();
		else endTime = System.currentTimeMillis();
		
		return unit.convert(endTime - startTime, localUnit);
	}
}
