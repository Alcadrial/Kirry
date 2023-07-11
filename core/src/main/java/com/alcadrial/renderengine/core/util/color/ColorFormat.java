package com.alcadrial.renderengine.core.util.color;

import java.util.function.Function;

public enum ColorFormat {
	
	RGBA(4, rgba -> rgba),
	RGB(3, rgb -> rgb),
	;
	
	private int channels;
	private Function<Color, Color> mixingFunction;
	
	private ColorFormat(int channels, Function<Color, Color> mixingFunction)
	{
		this.channels = channels;
		this.mixingFunction = mixingFunction;
	}
	
	public Color mix(Color c)
	{
		return mixingFunction.apply(c);
	}
	
	public int getChannels()
	{
		return channels;
	}
}
