package com.alcadra.renderengine.util.color;

public interface ColorRGB extends Color {
	
	@Override
	default float a()
	{
		return 1;
	}
}
