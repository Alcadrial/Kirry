package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class OutOfSpaceException extends VulkanException {
	
	public OutOfSpaceException()
	{
		
	}
	
	public OutOfSpaceException(String message)
	{
		super(message);
	}
	
	public OutOfSpaceException(Throwable cause)
	{
		super(cause);
	}
	
	public OutOfSpaceException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
