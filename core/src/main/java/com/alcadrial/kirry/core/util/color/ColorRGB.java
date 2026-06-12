package com.alcadrial.kirry.core.util.color;

public interface ColorRGB extends Color {
	
	@Override
	default float a()
	{
		return 255;
	}
}
