package com.alcadrial.renderengine.render.util.exception;

public class ExternalException extends VulkanException {
	
	public ExternalException()
	{}
	
	public ExternalException(String message)
	{
		super(message);
	}
	
	public ExternalException(Throwable cause)
	{
		super(cause);
	}
	
	public ExternalException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
