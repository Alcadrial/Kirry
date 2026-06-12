package com.alcadrial.kirry.render.util.exception;

public class InvalidEntityException extends RuntimeException {
	
	public InvalidEntityException()
	{}
	
	public InvalidEntityException(String message)
	{
		super(message);
	}
	
	public InvalidEntityException(Throwable cause)
	{
		super(cause);
	}
	
	public InvalidEntityException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	protected InvalidEntityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
