package com.alcadrial.kirry.render.util.exception;

public class InitException extends VulkanException {
	
	public InitException()
	{}
	
	public InitException(int vkError, String message, Throwable cause)
	{
		super(vkError, message, cause);
	}
	
	public InitException(int vkError, String message)
	{
		super(vkError, message);
	}
	
	public InitException(int vkError, Throwable cause)
	{
		super(vkError, cause);
	}
	
	public InitException(int vkError)
	{
		super(vkError);
	}
	
	public InitException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public InitException(String message)
	{
		super(message);
	}
	
	public InitException(Throwable cause)
	{
		super(cause);
	}
}
