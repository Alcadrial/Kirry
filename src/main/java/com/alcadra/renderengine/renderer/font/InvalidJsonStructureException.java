package com.alcadra.renderengine.renderer.font;

import java.io.IOException;

public class InvalidJsonStructureException extends IOException {
	
	public InvalidJsonStructureException(String message)
	{
		super(message);
	}
	
	public InvalidJsonStructureException(Throwable cause)
	{
		super(cause);
	}
	
	public InvalidJsonStructureException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public InvalidJsonStructureException()
	{
		
	}
}
