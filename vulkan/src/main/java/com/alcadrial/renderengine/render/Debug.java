package com.alcadrial.renderengine.render;

import java.io.PrintStream;

public class Debug {
	
	private static volatile PrintStream debugOutput = System.out;
	
	public static void println()
	{
		debugOutput.println();
	}
	
	public static void println(Object x)
	{
		debugOutput.println(x);
	}
	
	public static void printlnIf(boolean condition)
	{
		if (condition) println();
	}
	
	public static void printlnIf(boolean condition, Object x)
	{
		if (condition) println(x);
	}
	
	public static void setDebugOutput(PrintStream debugOutput)
	{
		Debug.debugOutput = debugOutput;
	}
}
