package com.alcadrial.renderengine.render.renderer.object;

public class InsufficientVertexDataException extends RuntimeException {
	
	public InsufficientVertexDataException()
	{}
	
	public InsufficientVertexDataException(String message)
	{
		super(message);
	}
	
	public InsufficientVertexDataException(Throwable cause)
	{
		super(cause);
	}
	
	public InsufficientVertexDataException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public InsufficientVertexDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
