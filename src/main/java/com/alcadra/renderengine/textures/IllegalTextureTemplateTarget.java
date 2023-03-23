package com.alcadra.renderengine.textures;

public class IllegalTextureTemplateTarget extends RuntimeException {
	
	public IllegalTextureTemplateTarget(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public IllegalTextureTemplateTarget(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public IllegalTextureTemplateTarget(String message)
	{
		super(message);
	}
	
	public IllegalTextureTemplateTarget(Throwable cause)
	{
		super(cause);
	}
	
	public IllegalTextureTemplateTarget()
	{
		
	}
}
