package com.alcadra.renderengine.textures;

public class IllegalDataTypeException extends RuntimeException {
	
	public IllegalDataTypeException(String message)
	{
		super(message);
	}
	
	public IllegalDataTypeException(Throwable cause)
	{
		super(cause);
	}
	
	public IllegalDataTypeException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public IllegalDataTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public IllegalDataTypeException()
	{
		
	}
}
