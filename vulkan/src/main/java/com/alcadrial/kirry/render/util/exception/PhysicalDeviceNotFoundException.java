package com.alcadrial.kirry.render.util.exception;

public class PhysicalDeviceNotFoundException extends Exception {
	
	public PhysicalDeviceNotFoundException()
	{
		
	}
	
	public PhysicalDeviceNotFoundException(String message)
	{
		super(message);
	}
	
	public PhysicalDeviceNotFoundException(Throwable cause)
	{
		super(cause);
	}
	
	public PhysicalDeviceNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
