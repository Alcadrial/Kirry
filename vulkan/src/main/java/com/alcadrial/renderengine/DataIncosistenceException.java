package com.alcadrial.renderengine;

public class DataIncosistenceException extends RuntimeException {
	
	public DataIncosistenceException()
	{}
	
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
	
	public DataIncosistenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
