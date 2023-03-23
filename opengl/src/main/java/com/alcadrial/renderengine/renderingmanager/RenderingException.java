package com.alcadrial.renderengine.renderingmanager;

public class RenderingException extends RuntimeException {
	
	public RenderingException()
	{}
	
	public RenderingException(String message)
	{
		super(message);
	}
	
	public RenderingException(Throwable cause)
	{
		super(cause);
	}
	
	public RenderingException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public RenderingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
