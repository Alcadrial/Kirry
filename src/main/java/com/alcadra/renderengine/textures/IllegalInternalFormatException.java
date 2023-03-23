package com.alcadra.renderengine.textures;

public class IllegalInternalFormatException extends RuntimeException {
	
	public IllegalInternalFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public IllegalInternalFormatException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public IllegalInternalFormatException(String message)
	{
		super(message);
	}
	
	public IllegalInternalFormatException(Throwable cause)
	{
		super(cause);
	}
	
	public IllegalInternalFormatException()
	{
		
	}
}
