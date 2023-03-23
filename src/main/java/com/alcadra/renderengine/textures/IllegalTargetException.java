package com.alcadra.renderengine.textures;

public class IllegalTargetException extends TextureTemplateException {
	
	public IllegalTargetException()
	{
		
	}
	
	public IllegalTargetException(String message)
	{
		super(message);
	}
	
	public IllegalTargetException(Throwable cause)
	{
		super(cause);
	}
	
	public IllegalTargetException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public IllegalTargetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
