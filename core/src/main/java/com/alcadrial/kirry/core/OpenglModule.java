package com.alcadrial.kirry.core;

public final class OpenglModule implements Module {
	
	private boolean isPresent;
	
	public OpenglModule()
	{
		try
		{
			Class.forName("com.alcadrial.kirry.OpenGlMarker", false, getClass().getClassLoader());
			isPresent = true;
		}
		catch (ClassNotFoundException e)
		{
			isPresent = false;
		}
	}
	
	@Override
	public boolean isPresent()
	{
		return isPresent;
	}
}
