package com.alcadrial.renderengine.render;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.eclipse.collections.impl.utility.LazyIterate;
import org.jspecify.annotations.Nullable;
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
		RenderEngineVulkan.assertLoaded();
	}
	
	private static volatile PrintStream debugOutput;
	private static volatile Map<Object, Long> idMap;
	private static String prefix;
	private static boolean enabledDeallocationFail;
	private static boolean enabledAllocationDebug;
	private static boolean enabledCopyDebug;
	private static boolean enabledCloseDebug;
	private static boolean enabledCreateDebug;
	private static boolean enabledExtraTimePrecision;
	
	static
	{
		debugOutput = System.out;
		idMap = new ConcurrentHashMap<>();
		prefix = "";
		enabledAllocationDebug = false;
		enabledCopyDebug = false;
		enabledCloseDebug = false;
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
		return idMap.compute(obj, (_, v) -> v == null ? 0L : v + 1);
	}
	
	public static void printState(String name, @Nullable VulkanDebug vk)
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
		Vulkan vulkan = RenderEngineVulkan.getVulkan();
		
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
	
	public static void print(String name, @Nullable String s)
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
	
	public static void print(String name, @Nullable VulkanDebug vk)
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
	
	public static void print(String name, @Nullable Collection<?> collection)
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
	
	public static void print(@Nullable Object obj)
	{
		debugOutput.println(prefix + obj);
	}
	
	public static String validity(Struct<?> p, int type)
	{
		return p.address() == MemoryUtil.NULL ? "NULL POINTER" : MemoryUtil.memGetInt(p.address()) == type ? "VALID" : "INVALID";
	}
	
	public static String validity(StructBuffer<?, ?> p, int type)
	{
		return LazyIterate.collect(p, s -> Debug.validity(s, type)).makeString("[", ", ", "]");
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
		debugOutput.println();
	}
	
	public static void debug(Object x)
	{
		Vulkan vulkan = RenderEngineVulkan.getUnsafeVulkan();
		if (vulkan != null) debugOutput.println(time() + "[DEBUG|" + Thread.currentThread().getName() + "/" + vulkan.getData().threadId + "] " + x);
		else debugOutput.println(time() + "[DEBUG] " + x);
	}
	
	private static String time()
	{
		LocalDateTime now = LocalDateTime.now();
		int h = now.getHour();
		int m = now.getMinute();
		int s = now.getSecond();
		int ms = now.getNano() / 1000000;
		StringBuilder time = new StringBuilder(enabledExtraTimePrecision ? 19 : 15);
		time.append('[');
		if (h < 10) time.append('0');
		time.append(h).append(':');
		if (m < 10) time.append('0');
		time.append(m).append(':');
		if (s < 10) time.append('0');
		time.append(s).append('.');
		if (ms < 10) time.append('0');
		if (ms < 100) time.append('0');
		time.append(ms);
		if (enabledExtraTimePrecision)
		{
			time.append('.');
			int us = now.getNano() / 1000 % 1000;
			if (us < 10) time.append('0');
			if (us < 100) time.append('0');
			time.append(us);
		}
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
	
	public static void setEnabledDeallocationFail(boolean enabledDeallocationFail)
	{
		Debug.enabledDeallocationFail = enabledDeallocationFail;
	}
	
	public static boolean isEnabledDeallocationFail()
	{
		return enabledDeallocationFail;
	}
	
	public static void debugAllocation(Supplier<Object> x)
	{
		if (enabledAllocationDebug) debug(x.get());
	}
	
	public static void setEnabledAllocationDebug(boolean enabledAllocationDebug)
	{
		Debug.enabledAllocationDebug = enabledAllocationDebug;
	}
	
	public static boolean isEnabledAllocationDebug()
	{
		return enabledAllocationDebug;
	}
	
	public static void debugCopy(Supplier<Object> x)
	{
		if (enabledCopyDebug) debug(x.get());
	}
	
	public static void setEnabledCopyDebug(boolean enabledCopyDebug)
	{
		Debug.enabledCopyDebug = enabledCopyDebug;
	}
	
	public static boolean isEnabledCopyDebug()
	{
		return enabledCopyDebug;
	}
	
	public static void debugClose(Supplier<Object> x)
	{
		if (enabledCloseDebug) debug(x.get());
	}
	
	public static void setEnabledCloseDebug(boolean enabledCloseDebug)
	{
		Debug.enabledCloseDebug = enabledCloseDebug;
	}
	
	public static boolean isEnabledCloseDebug()
	{
		return enabledCloseDebug;
	}

	public static void debugCreate(Supplier<Object> x)
	{
		if (enabledCreateDebug) debug(x.get());
	}
	
	public static void setEnabledCreateDebug(boolean enabledCreateDebug)
	{
		Debug.enabledCreateDebug = enabledCreateDebug;
	}
	
	public static boolean isEnabledCreateDebug()
	{
		return enabledCreateDebug;
	}
	
	public static void setEnabledExtraTimePrecision(boolean enabledExtraTimePrecision)
	{
		Debug.enabledExtraTimePrecision = enabledExtraTimePrecision;
	}
	
	public static boolean isEnabledExtraTimePrecision()
	{
		return enabledExtraTimePrecision;
	}
	
	public static void setEnabledAllDebug(boolean enabled)
	{
		setEnabledCreateDebug(enabled);
		setEnabledAllocationDebug(enabled);
		setEnabledCopyDebug(enabled);
		setEnabledCloseDebug(enabled);
	}
	
	public static void setDebugOutput(PrintStream debugOutput)
	{
		Debug.debugOutput = debugOutput;
	}
	
	public static List<VulkanObject> getAvailableVulkanObjects()
	{
		return VulkanObject.VULKAN_INSTANCE_IMMUTABLE_LIST;
	}
}
