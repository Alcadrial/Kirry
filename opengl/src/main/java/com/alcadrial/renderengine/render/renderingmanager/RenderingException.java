package com.alcadrial.renderengine.render.renderingmanager;

import com.alcadrial.renderengine.render.renderer.RenderingMode;

public class RenderingException extends RuntimeException {
	
	private RenderingMode mode;
	
	public RenderingException(RenderingMode mode)
	{
		this.mode = mode;
	}
	
	public RenderingException(RenderingMode mode, String message)
	{
		super(message);
		this.mode = mode;
	}
	
	public RenderingException(RenderingMode mode, Throwable cause)
	{
		super(cause);
		this.mode = mode;
	}
	
	public RenderingException(RenderingMode mode, String message, Throwable cause)
	{
		super(message, cause);
		this.mode = mode;
	}
	
	public RenderingException(RenderingMode mode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.mode = mode;
	}
	
	/**
	 * Return the rendering mode that caused this exception to be trown
	 * 
	 * @return the rendering mode
	 */
	public RenderingMode getMode()
	{
		return mode;
	}
}
