package com.alcadra.renderengine.textures;

public class IllegalAttachmentException extends RuntimeException {
	
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
	
	public IllegalAttachmentException()
	{
		
	}
}
