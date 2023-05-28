package com.alcadrial.renderengine.opengl.textures;

public class IllegalAttachmentException extends TextureTemplateException {
	
	public IllegalAttachmentException()
	{
		
	}
	
	public IllegalAttachmentException(String message)
	{
		super(message);
	}
	
	public IllegalAttachmentException(Throwable cause)
	{
		super(cause);
	}
	
	public IllegalAttachmentException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public IllegalAttachmentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
