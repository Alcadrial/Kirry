package com.alcadrial.renderengine.render;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanSupplier;

public class Debug {
	
	public static <T> T get(VulkanSupplier<T> debugRunnable, VulkanSupplier<T> releaseRunnable) throws VulkanException
	{
		return releaseRunnable.get();
	}
	
	public static void runOnDebug(Runnable runnable)
	{
		
	}
	
	public static void printStackTrace(Throwable t)
	{
		
	}
	
	public static long getIncrementalId(Object obj)
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
	
	public static void print(String name, @Nullable String s)
	{
		
	}
	
	public static void print(String name, Pointer vk)
	{
		
	}
	
	public static void print(String name, long vk)
	{
		
	}
	
	public static void print(String name, @Nullable VulkanDebug vk)
	{
		
	}
	
	public static void print(String name, @Nullable Collection<?> collection)
	{
		
	}
	
	public static void print(@Nullable Object obj)
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
	
	public static String address(@Nullable Long p)
	{
		return p == null ? "0x0" : address(p.longValue());
	}
	
	public static void debug()
	{
		
	}
	
	public static void debug(Object x)
	{
		
	}
	
	public static void error(Object x)
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
	
	public static void setEnabledDeallocationFail(boolean enabledDeallocationFail)
	{
		
	}
	
	public static boolean isEnabledDeallocationFail()
	{
		return false;
	}
	
	public static void debugAllocation(Supplier<Object> x)
	{
		
	}
	
	public static void setEnabledAllocationDebug(boolean enabledAllocationDebug)
	{
		
	}
	
	public static boolean isEnabledAllocationDebug()
	{
		return false;
	}
	
	public static void debugCopy(Supplier<Object> x)
	{
		
	}
	
	public static void setEnabledCopyDebug(boolean enabledCopyDebug)
	{
		
	}
	
	public static boolean isEnabledCopyDebug()
	{
		return false;
	}
	
	public static void debugClose(Supplier<Object> x)
	{
		
	}
	
	public static void setEnabledCloseDebug(boolean enabledCloseDebug)
	{
		
	}
	
	public static boolean isEnabledCloseDebug()
	{
		return false;
	}
	
	public static void debugCreate(Supplier<Object> x)
	{
		
	}
	
	public static void setEnabledCreateDebug(boolean enabledCreateDebug)
	{
		
	}
	
	public static boolean isEnabledCreateDebug()
	{
		return false;
	}
	
	public static void setEnabledExtraTimePrecision(boolean enabledExtraTimePrecision)
	{
		
	}
	
	public static boolean isEnabledExtraTimePrecision()
	{
		return false;
	}
	
	public static void setEnabledAllDebug(boolean enabled)
	{
		
	}
	
	public static void setDebugOutput(PrintStream debugOutput)
	{
		
	}
	
	public static List<VulkanObject> getAvailableVulkanObjects()
	{
		return List.of();
	}
}
