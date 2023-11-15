package com.alcadrial.renderengine.render;

public class InvalidSurfaceException extends Exception {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public InvalidSurfaceException()
	{
		
	}
	
	public InvalidSurfaceException(String message)
	{
		super(message);
	}
	
	public InvalidSurfaceException(Throwable cause)
	{
		super(cause);
	}
	
	public InvalidSurfaceException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
