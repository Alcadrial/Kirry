package com.alcadrial.kirry.core.cursor;

public class CursorException extends RuntimeException {
	
	public CursorException()
	{
		
	}
	
	public CursorException(String message)
	{
		super(message);
	}
	
	public CursorException(Throwable cause)
	{
		super(cause);
	}
	
	public CursorException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
