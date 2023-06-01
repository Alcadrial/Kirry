package com.alcadrial.renderengine.render.util.color;

public interface ColorRGB extends Color {
	
	@Override
	default float a()
	{
		return 255;
	}
}
