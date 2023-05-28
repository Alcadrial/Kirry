package com.alcadrial.renderengine.opengl.util.color;

public interface ColorRGB extends Color {
	
	@Override
	default float a()
	{
		return 255;
	}
}
