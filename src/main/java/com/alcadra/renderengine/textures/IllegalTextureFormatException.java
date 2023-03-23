package com.alcadra.renderengine.textures;

public class IllegalTextureFormatException extends TextureTemplateException {
	
	public IllegalTextureFormatException()
	{
		
	}
	
	public IllegalTextureFormatException(String message)
	{
		super(message);
	}
	
	public IllegalTextureFormatException(Throwable cause)
	{
		super(cause);
	}
	
	public IllegalTextureFormatException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public IllegalTextureFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
