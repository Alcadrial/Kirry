package com.alcadrial.kirry.render.util;

import com.alcadrial.kirry.render.util.exception.VulkanException;

public class OverflowException extends VulkanException {
	
	public OverflowException()
	{}
	
	public OverflowException(int vkError, String message, Throwable cause)
	{
		super(vkError, message, cause);
	}
	
	public OverflowException(int vkError, String message)
	{
		super(vkError, message);
	}
	
	public OverflowException(int vkError, Throwable cause)
	{
		super(vkError, cause);
	}
	
	public OverflowException(int vkError)
	{
		super(vkError);
	}
	
	public OverflowException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public OverflowException(String message)
	{
		super(message);
	}
	
	public OverflowException(Throwable cause)
	{
		super(cause);
	}
}
