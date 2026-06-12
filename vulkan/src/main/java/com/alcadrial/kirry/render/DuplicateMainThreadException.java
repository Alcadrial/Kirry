package com.alcadrial.kirry.render;

public class DuplicateMainThreadException extends RuntimeException {
	
	public DuplicateMainThreadException()
	{
		
	}
	
	public DuplicateMainThreadException(String message)
	{
		super(message);
	}
	
	public DuplicateMainThreadException(Throwable cause)
	{
		super(cause);
	}
	
	public DuplicateMainThreadException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
