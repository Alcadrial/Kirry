package com.alcadrial.renderengine.render.util;


public class OutOfSpaceException extends RuntimeException {
	
	public OutOfSpaceException()
	{
		
	}
	
	public OutOfSpaceException(String message)
	{
		super(message);
	}
	
	public OutOfSpaceException(Throwable cause)
	{
		super(cause);
	}
	
	public OutOfSpaceException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
