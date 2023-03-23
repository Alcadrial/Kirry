package com.alcadrial.renderengine.renderingmanager;

public class RenderingAlreadyStartedException extends RenderingException {
	
	public RenderingAlreadyStartedException()
	{}
	
	public RenderingAlreadyStartedException(String message)
	{
		super(message);
	}
	
	public RenderingAlreadyStartedException(Throwable cause)
	{
		super(cause);
	}
	
	public RenderingAlreadyStartedException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public RenderingAlreadyStartedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
