package com.alcadrial.renderengine.render.renderer;

public class IllegalBuildingState extends RuntimeException {
	
	public IllegalBuildingState()
	{}
	
	public IllegalBuildingState(String message)
	{
		super(message);
	}
	
	public IllegalBuildingState(Throwable cause)
	{
		super(cause);
	}
	
	public IllegalBuildingState(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public IllegalBuildingState(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
