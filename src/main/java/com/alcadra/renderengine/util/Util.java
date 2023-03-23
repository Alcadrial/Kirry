package com.alcadra.renderengine.util;

import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL30;

import com.alcadra.math.vector.Vector1f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.RenderEngine;

public class Util {
	
	private static volatile boolean debug = false;
	
	public static boolean isDebug()
	{
		return debug;
	}
	
	public static void enableDebug()
	{
		debug = true;
	}
	
	public static String toPointString(Vector4f vector)
	{
		return "{x=" + vector.x() + ", y= " + vector.y() + ", z= " + vector.z() + ", w= " + vector.w() + "}";
	}
	
	public static String toPointString(Vector3f vector)
	{
		return "{x=" + vector.x() + ", y= " + vector.y() + ", z= " + vector.z() + "}";
	}
	
	public static String toPointString(Vector2f vector)
	{
		return "{x=" + vector.x() + ", y= " + vector.y() + "}";
	}
	
	public static String toPointString(Vector1f vector)
	{
		return "{x=" + vector.x() + "}";
	}
	
	public static String newParamerizedString(String base, String parametertrigger, List<Object> parameters)
	{
		String string = "";
		int parametersq = 0;
		while (base.indexOf(parametertrigger) != -1 && parametersq < parameters.size())
		{
			int triggerindex = base.indexOf(parametertrigger);
			string += base.substring(0, triggerindex) + String.valueOf(parameters.get(parametersq));
			base = base.substring(triggerindex + parametertrigger.length());
			parametersq++;
		}
		string += base;
		return string;
	}
	
	public static String newParamerizedString(String base, String parametertrigger, Object... parameters)
	{
		String string = "";
		int parametersq = 0;
		while (base.indexOf(parametertrigger) != -1 && parametersq < parameters.length)
		{
			int triggerindex = base.indexOf(parametertrigger);
			string += base.substring(0, triggerindex) + String.valueOf(parameters[parametersq]);
			base = base.substring(triggerindex + parametertrigger.length());
			parametersq++;
		}
		string += base;
		return string;
	}
	
	public static void printErrorAsync(String s)
	{
		Thread t = new Thread(() -> {
			RenderEngine.getActiveTest().getWindow().makeContextCurrent();
			printError(s);
		});
		t.start();
	}
	
	public static void printErrorSync(String s)
	{
		Thread t = new Thread(() -> {
			RenderEngine.getActiveTest().getWindow().makeContextCurrent();
			printError(s);
		});
		t.start();
		try
		{
			t.join();
		}
		catch (InterruptedException e)
		{}
		RenderEngine.getActiveTest().getWindow().makeContextCurrent();
	}
	
	public static void printError(String s)
	{
		if (debug)
		{
			int error = GL30.glGetError();
			while (true)
			{
				switch (error)
				{
					case GL30.GL_NO_ERROR:
						return;
					case GL30.GL_INVALID_ENUM:
						System.out.println(s + " INVALID ENUM");
						break;
					case GL30.GL_INVALID_VALUE:
						System.out.println(s + " INVALID VALUE");
						break;
					case GL30.GL_INVALID_OPERATION:
						System.out.println(s + " INVALID OPERATION");
						break;
					case GL30.GL_STACK_OVERFLOW:
						System.out.println(s + " STACK OVERFLOW");
						break;
					case GL30.GL_STACK_UNDERFLOW:
						System.out.println(s + " STACK UNDERFLOW");
						break;
					case GL30.GL_OUT_OF_MEMORY:
						System.out.println(s + " OUT OF MEMORY");
						break;
					case GL30.GL_INVALID_FRAMEBUFFER_OPERATION:
						System.out.println(s + " INVALID FRAMEBUFFER OPERATION");
						break;
				}
				error = GL30.glGetError();
			}
		}
	}
	
	public static void printError(Supplier<String> x)
	{
		if (debug) printError(x.get());
	}
	
	public static void printError()
	{
		if (debug)
		{
			int error = GL30.glGetError();
			while (true)
			{
				switch (error)
				{
					case GL30.GL_NO_ERROR:
						return;
					case GL30.GL_INVALID_ENUM:
						System.out.println("INVALID ENUM");
						break;
					case GL30.GL_INVALID_VALUE:
						System.out.println("INVALID VALUE");
						break;
					case GL30.GL_INVALID_OPERATION:
						System.out.println("INVALID OPERATION");
						break;
					case GL30.GL_INVALID_FRAMEBUFFER_OPERATION:
						System.out.println("INVALID FRAMEBUFFER OPERATION");
						break;
				}
				error = GL30.glGetError();
			}
		}
	}
}
