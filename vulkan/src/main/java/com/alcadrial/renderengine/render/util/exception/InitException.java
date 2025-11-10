package com.alcadrial.renderengine.render.util.exception;

public class InitException extends VulkanException {
	
	public InitException()
	{}
	
	public InitException(String message)
	{
		super(message);
	}
	
	public InitException(Throwable cause)
	{
		super(cause);
	}
	
	public InitException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
