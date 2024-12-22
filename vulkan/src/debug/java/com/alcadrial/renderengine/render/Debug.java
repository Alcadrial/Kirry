package com.alcadrial.renderengine.render;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

import com.alcadrial.renderengine.render.Vulkan.ThreadData;
import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanSupplier;

public class Debug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private static volatile PrintStream debugOutput;
	private static volatile Map<Object, Long> idMap;
	private static String prefix;
	private static boolean enableAllocationDebug;
	private static boolean enableCopyDebug;
	
	static
	{
		debugOutput = System.out;
		idMap = new ConcurrentHashMap<>();
		prefix = "";
		enableAllocationDebug = false;
		enableCopyDebug = false;
	}
	
	public static <T> T get(VulkanSupplier<T> debugSupplier, VulkanSupplier<T> releaseSupplier) throws VulkanException
	{
		return debugSupplier.get();
	}
	
	public static void runOnDebug(Runnable runnable)
	{
		runnable.run();
	}
	
	public static void printStackTrace(Throwable t)
	{
		t.printStackTrace(debugOutput);
	}
	
	public static long getIncrementalId(Object obj)
	{
		return idMap.compute(obj, (k, v) -> v == null ? 0L : v + 1);
	}
	
	public static void printState(String name, VulkanDebug vk)
	{
		if (vk == null)
		{
			print(name + ": null");
			return;
		}
		print(name + " {");
		indent();
		vk.debug();
		deindent();
		print("}");
	}
	
	public static void printState()
	{
		Vulkan vulkan = RenderEngine.getVulkan();
		
		print("Current vulkan state:");
		print("{");
		indent();
		for (Entry<Thread, ThreadData> e : vulkan.getAllData().entrySet())
		{
			ThreadData data = e.getValue();
			print("Thread: \"" + e.getKey().getName() + "\", id: " + data.threadId + " {");
			indent();
			print("Physical Device", data.physicalDevice);
			print("Logical Device", data.device);
			print("Window", data.window);
			deindent();
			print("}");
		}
		deindent();
		print("}");
	}
	
	public static void indent()
	{
		prefix += "\t";
	}
	
	public static void deindent()
	{
		prefix = prefix.substring(0, prefix.length() - 1);
	}
	
	public static void print(String name, String s)
	{
		print(name + ": " + s);
	}
	
	public static void print(String name, Pointer vk)
	{
		print(name + ": " + address(vk));
	}
	
	public static void print(String name, long vk)
	{
		print(name + ": " + address(vk));
	}
	
	public static void print(String name, VulkanDebug vk)
	{
		if (vk == null)
		{
			print(name + ": null");
			return;
		}
		print(name + " {");
		indent();
		vk.debug();
		deindent();
		print("}");
	}
	
	public static void print(String name, Collection<?> collection)
	{
		if (collection == null)
		{
			print(name + ": null");
			return;
		}
		print(name + " {");
		indent();
		for (Object obj : collection) print(obj);
		deindent();
		print("}");
	}
	
	public static void print(Object obj)
	{
		debugOutput.println(prefix + obj);
	}
	
	public static String validity(Struct<?> p, int type)
	{
		return p.address() == MemoryUtil.NULL ? "NULL POINTER" : MemoryUtil.memGetInt(p.address()) == type ? "VALID" : "INVALID";
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
		debugOutput.println();
	}
	
	public static void debug(Object x)
	{
		debugOutput.print(time());
		debugOutput.print("[DEBUG] ");
		debugOutput.println(x);
	}
	
	private static String time()
	{
		LocalDateTime now = LocalDateTime.now();
		int h = now.getHour();
		int m = now.getMinute();
		int s = now.getSecond();
		int n = now.getNano() / 1000000;
		StringBuilder time = new StringBuilder(15);
		time.append('[');
		if (h < 10) time.append('0');
		time.append(h).append(':');
		if (m < 10) time.append('0');
		time.append(m).append(':');
		if (s < 10) time.append('0');
		time.append(s).append('.');
		if (n < 10) time.append('0');
		if (n < 100) time.append('0');
		time.append(n);
		time.append(']');
		time.append(' ');
		return time.toString();
	}
	
	public static void debugIf(boolean condition)
	{
		if (condition) debug();
	}
	
	public static void debugIf(boolean condition, Object x)
	{
		if (condition) debug(x);
	}
	
	public static void debugIf(boolean condition, Supplier<Object> x)
	{
		if (condition) debug(x.get());
	}
	
	public static void debugAllocation(Supplier<Object> x)
	{
		if (enableAllocationDebug) debug(x.get());
	}
	
	public static void setEnableAllocationDebug(boolean enableAllocationDebug)
	{
		Debug.enableAllocationDebug = enableAllocationDebug;
	}
	
	public static boolean isEnableAllocationDebug()
	{
		return enableAllocationDebug;
	}
	
	public static void setEnableCopyDebug(boolean enableCopyDebug)
	{
		Debug.enableCopyDebug = enableCopyDebug;
	}
	
	public static boolean isEnableCopyDebug()
	{
		return enableCopyDebug;
	}
	
	public static void setDebugOutput(PrintStream debugOutput)
	{
		Debug.debugOutput = debugOutput;
	}
	
	public static Set<VulkanObject> getAvailableVulkanObjects()
	{
		return VulkanObject.VULKAN_INSTANCE_IMMUTABLE_SET;
	}
}
