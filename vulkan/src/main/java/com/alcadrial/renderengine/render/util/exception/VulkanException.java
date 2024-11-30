package com.alcadrial.renderengine.render.util.exception;

public class VulkanException extends Exception {
	
	public VulkanException()
	{
		
	}
	
	public VulkanException(String message)
	{
		super(message);
	}
	
	public VulkanException(Throwable cause)
	{
		super(cause);
	}
	
	public VulkanException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
