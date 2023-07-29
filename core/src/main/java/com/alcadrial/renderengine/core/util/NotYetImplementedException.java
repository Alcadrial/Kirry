package com.alcadrial.renderengine.core.util;

public class NotYetImplementedException extends RuntimeException {
	
	public NotYetImplementedException()
	{
		
	}
	
	public NotYetImplementedException(String message)
	{
		super(message);
	}
	
	public NotYetImplementedException(Throwable cause)
	{
		super(cause);
	}
	
	public NotYetImplementedException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
