package com.alcadra.renderengine.util;

import java.util.List;

import org.lwjgl.opengl.GL30;

import com.alcadra.math.vector.Vector1f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;

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
		if (vector instanceof Vector1f) return "{x=" + vector.x() + "}";
		if (vector instanceof Vector2f) return "{x=" + vector.x() + ", y= " + vector.y() + "}";
		if (vector instanceof Vector3f) return "{x=" + vector.x() + ", y= " + vector.y() + ", z= " + vector.z() + "}";
		return "{x=" + vector.x() + ", y= " + vector.y() + ", z= " + vector.z() + ", w= " + vector.w() + "}";
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
	
	public static void printError(String s)
	{
		int error = GL30.glGetError();
		while (debug)
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
				case GL30.GL_INVALID_FRAMEBUFFER_OPERATION:
					System.out.println(s + " INVALID FRAMEBUFFER OPERATION");
					break;
			}
			error = GL30.glGetError();
		}
	}
	
	public static void printError()
	{
		printError("");
	}
}
