package com.alcadrial.renderengine.render;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30;

import com.alcadrial.renderengine.render.util.color.Color;
import com.alcadrial.renderengine.render.util.wrapper.CullFaceMode;

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
	
	public static void enableDepthTest()
	{
		GL30.glEnable(GL30.GL_DEPTH_TEST);
	}
	
	public static void disableDepthTest()
	{
		GL30.glDisable(GL30.GL_DEPTH_TEST);
	}
	
	public static void enableBlend()
	{
		GL30.glEnable(GL30.GL_BLEND);
	}
	
	public static void disableBlend()
	{
		GL30.glDisable(GL30.GL_BLEND);
	}
	
	public static void enableCullFace()
	{
		GL30.glEnable(GL30.GL_CULL_FACE);
	}
	
	public static void disableCullFace()
	{
		GL30.glDisable(GL30.GL_CULL_FACE);
	}
	
	public static void setBlendFactors(int sourceFactor, int destinationFactor)
	{
		GL30.glBlendFunc(sourceFactor, destinationFactor);
	}
	
	public static void setCullFace(CullFaceMode mode)
	{
		GL30.glCullFace(mode.getId());
	}
	
	public static void setFrontFaceDirection(boolean clockWise)
	{
		GL30.glFrontFace(clockWise ? GL30.GL_CW : GL30.GL_CCW);
	}
	
	public static void setClearColor(Color color)
	{
		setClearColor(color.r(), color.g(), color.b(), color.a());
	}
	
	public static void setClearColor(float r, float g, float b, float a)
	{
		GL30.glClearColor(r, g, b, a);
	}
}
