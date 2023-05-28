package com.alcadrial.renderengine.opengl.textures;

public class IllegalDataTypeException extends TextureTemplateException {
	
	public IllegalDataTypeException()
	{
		
	}
	
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
}
