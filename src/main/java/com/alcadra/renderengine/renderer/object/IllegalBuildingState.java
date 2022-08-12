package com.alcadra.renderengine.renderer.object;

import com.alcadra.renderengine.util.update.ToMove;

@ToMove("com.alcadra.renderengine.renderer.IllegalBuildingState")
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
