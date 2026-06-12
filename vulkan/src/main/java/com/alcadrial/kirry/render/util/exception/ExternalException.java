package com.alcadrial.kirry.render.util.exception;

public class ExternalException extends VulkanException {
	
	public ExternalException()
	{}
	
	public ExternalException(int vkError, String message, Throwable cause)
	{
		super(vkError, message, cause);
	}
	
	public ExternalException(int vkError, String message)
	{
		super(vkError, message);
	}
	
	public ExternalException(int vkError, Throwable cause)
	{
		super(vkError, cause);
	}
	
	public ExternalException(int vkError)
	{
		super(vkError);
	}
	
	public ExternalException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public ExternalException(String message)
	{
		super(message);
	}
	
	public ExternalException(Throwable cause)
	{
		super(cause);
	}
}
