package com.alcadrial.renderengine.core.util;

public class DataIncosistenceException extends RuntimeException {

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
