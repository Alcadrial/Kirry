package com.alcadra.renderengine;

import org.lwjgl.glfw.GLFW;

public class RenderEngine {
	
	private static volatile boolean debug = false;
	private static volatile boolean initializated = false;
	
	public static boolean initialize()
	{
		return initializated = GLFW.glfwInit();
	}
	
	public static void terminate()
	{
		if (initializated)
		{
			GLFW.glfwTerminate();
			initializated = false;
		}
	}
	
	public static boolean isInitializated()
	{
		return initializated;
	}
	
	public static boolean isDebug()
	{
		return debug;
	}
	
	public static void enableDebug()
	{
		debug = true;
	}
	
	public static void disableDebug()
	{
		debug = false;
	}
}
