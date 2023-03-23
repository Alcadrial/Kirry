package com.alcadra.renderengine.renderingmanager;

public class RenderingNotStartedException extends RenderingException {
	
	public RenderingNotStartedException()
	{}
	
	public RenderingNotStartedException(String message)
	{
		super(message);
	}
	
	public RenderingNotStartedException(Throwable cause)
	{
		super(cause);
	}
	
	public RenderingNotStartedException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public RenderingNotStartedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
