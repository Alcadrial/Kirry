package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class OverflowException extends VulkanException {
	
	public OverflowException()
	{
		
	}
	
	public OverflowException(String message)
	{
		super(message);
	}
	
	public OverflowException(Throwable cause)
	{
		super(cause);
	}
	
	public OverflowException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
