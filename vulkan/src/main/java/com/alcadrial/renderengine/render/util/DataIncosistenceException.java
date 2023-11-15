package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.RenderEngine;

public class DataIncosistenceException extends RuntimeException {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public DataIncosistenceException()
	{
		
	}
	
	public DataIncosistenceException(String message)
	{
		super(message);
	}
	
	public DataIncosistenceException(Throwable cause)
	{
		super(cause);
	}
	
	public DataIncosistenceException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
