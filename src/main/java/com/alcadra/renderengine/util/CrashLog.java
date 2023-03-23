package com.alcadra.renderengine.util;

public class CrashLog {
	
	private static final Logger LOGGER = new Logger();
	private StackTraceElement[] stackTrace;
	private String cause = "";
	
	public CrashLog(String cause)
	{
		this.cause = cause;
	}
	
	public CrashLog(String cause, StackTraceElement[] stackTrace)
	{
		this.cause = cause;
		this.stackTrace = stackTrace;
	}
	
	public CrashLog(Exception exception)
	{
		cause = exception.getMessage();
		stackTrace = exception.getStackTrace();
	}
	
	public void setCause(String cause)
	{
		this.cause = cause;
	}
	
	public void printCrashLog()
	{
		LOGGER.error(cause);
		for (StackTraceElement element : stackTrace) System.out.println(element.toString());
	}
	
	public void crash()
	{
		printCrashLog();
		System.exit(0);
	}
}
