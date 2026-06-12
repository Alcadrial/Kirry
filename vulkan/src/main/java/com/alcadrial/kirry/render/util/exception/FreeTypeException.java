package com.alcadrial.kirry.render.util.exception;

public class FreeTypeException extends Exception {
	
	private int error;
	
	public FreeTypeException()
	{
		error = -1;
	}
	
	public FreeTypeException(String message)
	{
		super(message);
		error = -1;
	}
	
	public FreeTypeException(Throwable cause)
	{
		super(cause);
		error = -1;
	}
	
	public FreeTypeException(String message, Throwable cause)
	{
		super(message, cause);
		error = -1;
	}
	
	public FreeTypeException(int error)
	{
		this.error = error;
	}
	
	public FreeTypeException(int error, String message)
	{
		super(message);
		this.error = error;
	}
	
	public FreeTypeException(int error, Throwable cause)
	{
		super(cause);
		this.error = error;
	}
	
	public FreeTypeException(int error, String message, Throwable cause)
	{
		super(message, cause);
		this.error = error;
	}
	
	public int getFTError()
	{
		return error;
	}
}
