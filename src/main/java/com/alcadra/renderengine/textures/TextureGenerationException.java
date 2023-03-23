package com.alcadra.renderengine.textures;

public class TextureGenerationException extends RuntimeException {
	
	public TextureGenerationException()
	{
		
	}
	
	public TextureGenerationException(String message)
	{
		super(message);
	}
	
	public TextureGenerationException(Throwable cause)
	{
		super(cause);
	}
	
	public TextureGenerationException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public TextureGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
