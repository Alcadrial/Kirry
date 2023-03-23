package com.alcadra.renderengine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.alcadra.math.vector.Vector1f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;

public class Util {
	
	public static String toPointString(Vector4f vector)
	{
		if (vector instanceof Vector1f) return "{x=" + vector.x() + "}";
		if (vector instanceof Vector2f) return "{x=" + vector.x() + ", y= " + vector.y() + "}";
		if (vector instanceof Vector3f) return "{x=" + vector.x() + ", y= " + vector.y() + ", z= " + vector.z() + "}";
		return "{x=" + vector.x() + ", y= " + vector.y() + ", z= " + vector.z() + ", w= " + vector.w() + "}";
	}
	
	public static String loadFileToString(File file) throws IOException
	{
		FileInputStream reader = new FileInputStream(file);
		String result = new String(reader.readAllBytes());
		reader.close();
		return result;
	}
	
	public static String loadFileToString(String file) throws IOException
	{
		return loadFileToString(new File(file));
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
}
