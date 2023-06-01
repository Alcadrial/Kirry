package com.alcadrial.renderengine.render.util;

import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL30;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.renderengine.render.RenderEngine;

public class Util {
	
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
	
	// public static String toPointString(Vector1f vector)
	// {
	// return "{x=" + vector.x() + "}";
	// }
	
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
	
	public static void printError(String s)
	{
		if (RenderEngine.isDebug())
		{
			int error = GL30.glGetError();
			while (error != GL30.GL_NO_ERROR)
			{
				System.out.println(switch (error)
				{
					case GL30.GL_INVALID_ENUM -> s + " INVALID ENUM";
					case GL30.GL_INVALID_VALUE -> s + " INVALID VALUE";
					case GL30.GL_INVALID_OPERATION -> s + " INVALID OPERATION";
					case GL30.GL_STACK_OVERFLOW -> s + " STACK OVERFLOW";
					case GL30.GL_STACK_UNDERFLOW -> s + " STACK UNDERFLOW";
					case GL30.GL_OUT_OF_MEMORY -> s + " OUT OF MEMORY";
					case GL30.GL_INVALID_FRAMEBUFFER_OPERATION -> s + " INVALID FRAMEBUFFER OPERATION";
					default -> s + " UNKNOWN";
				});
				error = GL30.glGetError();
			}
		}
	}
	
	public static void printError(Supplier<String> x)
	{
		if (RenderEngine.isDebug()) printError(x.get());
	}
	
	public static void printError()
	{
		if (RenderEngine.isDebug())
		{
			int error = GL30.glGetError();
			while (error != GL30.GL_NO_ERROR)
			{
				System.out.println(switch (error)
				{
					case GL30.GL_INVALID_ENUM -> "INVALID ENUM";
					case GL30.GL_INVALID_VALUE -> "INVALID VALUE";
					case GL30.GL_INVALID_OPERATION -> "INVALID OPERATION";
					case GL30.GL_STACK_OVERFLOW -> "STACK OVERFLOW";
					case GL30.GL_STACK_UNDERFLOW -> "STACK UNDERFLOW";
					case GL30.GL_OUT_OF_MEMORY -> "OUT OF MEMORY";
					case GL30.GL_INVALID_FRAMEBUFFER_OPERATION -> "INVALID FRAMEBUFFER OPERATION";
					default -> "UNKNOWN";
				});
				error = GL30.glGetError();
			}
		}
	}
}
