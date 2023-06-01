package com.alcadrial.renderengine.render.renderingmanager;

import com.alcadrial.renderengine.render.renderer.RenderingMode;

public class RenderingAlreadyStartedException extends RenderingException {
	
	public RenderingAlreadyStartedException(RenderingMode mode)
	{
		super(mode);
	}
	
	public RenderingAlreadyStartedException(RenderingMode mode, String message)
	{
		super(mode, message);
	}
	
	public RenderingAlreadyStartedException(RenderingMode mode, Throwable cause)
	{
		super(mode, cause);
	}
	
	public RenderingAlreadyStartedException(RenderingMode mode, String message, Throwable cause)
	{
		super(mode, message, cause);
	}
	
	public RenderingAlreadyStartedException(RenderingMode mode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(mode, message, cause, enableSuppression, writableStackTrace);
	}
}
