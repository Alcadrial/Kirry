package com.alcadra.renderengine;

import org.lwjgl.glfw.GLFW;

public class RenderEngine {
	
	public static boolean initialize()
	{
		return GLFW.glfwInit();
	}
}
