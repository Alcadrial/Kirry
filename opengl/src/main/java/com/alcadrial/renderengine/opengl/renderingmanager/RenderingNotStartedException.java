package com.alcadrial.renderengine.opengl.renderingmanager;

import com.alcadrial.renderengine.opengl.renderer.RenderingMode;

public class RenderingNotStartedException extends RenderingException {
	
	public RenderingNotStartedException(RenderingMode mode)
	{
		super(mode);
	}
	
	public RenderingNotStartedException(RenderingMode mode, String message)
	{
		super(mode, message);
	}
	
	public RenderingNotStartedException(RenderingMode mode, Throwable cause)
	{
		super(mode, cause);
	}
	
	public RenderingNotStartedException(RenderingMode mode, String message, Throwable cause)
	{
		super(mode, message, cause);
	}
	
	public RenderingNotStartedException(RenderingMode mode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(mode, message, cause, enableSuppression, writableStackTrace);
	}
}
