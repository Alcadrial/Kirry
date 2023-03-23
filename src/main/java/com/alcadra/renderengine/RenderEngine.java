package com.alcadra.renderengine;

import org.lwjgl.glfw.GLFW;

public class RenderEngine {
	
	public static boolean initialize()
	{
		return GLFW.glfwInit();
	}
	
	public static void terminate()
	{
		GLFW.glfwTerminate();
	}
}
