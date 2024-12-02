package com.alcadrial.renderengine.render;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

import com.alcadrial.renderengine.render.util.VulkanDebug;

public class Debug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static <T> T get(Supplier<T> debugRunnable, Supplier<T> releaseRunnable)
	{
		return releaseRunnable.get();
	}
	
	public static void runOnDebug(Runnable runnable)
	{
		
	}
	
	public static void printStackTrace(Throwable t)
	{
		
	}
	
	public static long getIncrementalId()
	{
		return 0;
	}
	
	public static void printState(String name, VulkanDebug vk)
	{
		
	}
	
	public static void printState()
	{
		
	}
	
	public static void indent()
	{
		
	}
	
	public static void deindent()
	{
		
	}
	
	public static void print(String name, Pointer vk)
	{
		
	}
	
	public static void print(String name, long vk)
	{
		
	}
	
	public static void print(String name, VulkanDebug vk)
	{
		
	}
	
	public static void print(String name, Collection<?> collection)
	{
		
	}
	
	public static void print(Object obj)
	{
		
	}
	
	public static String validity(Struct<?> p, int type)
	{
		return MemoryUtil.memGetInt(p.address()) == type ? "VALID" : "INVALID";
	}
	
	public static String validity(StructBuffer<?, ?> p, int type)
	{
		return p.stream().map(s -> Debug.validity(s, type)).collect(Collectors.joining(", ", "[", "]"));
	}
	
	public static String address(Pointer p)
	{
		return "0x" + Long.toHexString(p.address());
	}
	
	public static String address(long p)
	{
		return "0x" + Long.toHexString(p);
	}
	
	public static void debug()
	{
		
	}
	
	public static void debug(Object x)
	{
		
	}
	
	public static void debugIf(boolean condition)
	{
		
	}
	
	public static void debugIf(boolean condition, Object x)
	{
		
	}
	
	public static void debugIf(boolean condition, Supplier<Object> x)
	{
		
	}
	
	public static void debugAllocation(Supplier<Object> x)
	{
		
	}
	
	public static void setEnableAllocationDebug(boolean enableAllocationDebug)
	{
		
	}
	
	public static boolean isEnableAllocationDebug()
	{
		return false;
	}
	
	public static void setDebugOutput(PrintStream debugOutput)
	{
		
	}
	
	public static Set<VulkanObject> getAvailableVulkanObjects()
	{
		return Set.of();
	}
}
