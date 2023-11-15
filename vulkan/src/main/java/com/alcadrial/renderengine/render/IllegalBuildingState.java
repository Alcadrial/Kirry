package com.alcadrial.renderengine.render;

public class IllegalBuildingState extends RuntimeException {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public IllegalBuildingState()
	{
		
	}
	
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
}
