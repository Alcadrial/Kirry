package com.alcadrial.renderengine.textures;

public class IllegalInternalFormatException extends TextureTemplateException {
	
	public IllegalInternalFormatException()
	{
		
	}
	
	public IllegalInternalFormatException(Throwable cause)
	{
		super(cause);
	}
	
	public IllegalInternalFormatException(String message)
	{
		super(message);
	}
	
	public IllegalInternalFormatException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public IllegalInternalFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
