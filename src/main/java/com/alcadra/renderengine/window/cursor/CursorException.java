package com.alcadra.renderengine.window.cursor;

public class CursorException extends RuntimeException {
	
	public CursorException()
	{}
	
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
	
	public CursorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
