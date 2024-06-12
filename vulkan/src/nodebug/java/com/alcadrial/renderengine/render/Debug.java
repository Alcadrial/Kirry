package com.alcadrial.renderengine.render;

import java.io.PrintStream;
import java.util.Collection;

import org.lwjgl.system.Pointer;
import org.lwjgl.system.windows.Kernel32;

import com.alcadrial.renderengine.render.Vulkan.ThreadData;
import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.VulkanResource;

public class Debug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private static volatile PrintStream debugOutput = System.out;
	
	public static void printStackTrace(Throwable t)
	{
		t.printStackTrace(debugOutput);
	}
	
	private static String prefix = "";
	
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
		print("Thread: \"" + Thread.currentThread() + "\", id: " + Kernel32.GetCurrentThreadId() + " {");
		indent();
		ThreadData data = vulkan.getData();
		print("Physical Device", data.physicalDevice);
		print("Logical Device", data.device);
		print("Window", data.window);
		deindent();
		print("}");
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
	
	public static void print(String name, long vk)
	{
		print(name + ": " + address(vk));
	}
	
	public static void print(String name, VulkanResource<?> vk)
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
		debugOutput.println(x);
	}
	
	public static void debugIf(boolean condition)
	{
		if (condition) debug();
	}
	
	public static void debugIf(boolean condition, Object x)
	{
		if (condition) debug(x);
	}
	
	public static void onDebug(Runnable action)
	{
		action.run();
	}
	
	public static void setDebugOutput(PrintStream debugOutput)
	{
		Debug.debugOutput = debugOutput;
	}
}
