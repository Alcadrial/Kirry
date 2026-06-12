package com.alcadrial.kirry.render.renderingmanager;

import com.alcadrial.kirry.render.renderer.RenderingMode;

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
