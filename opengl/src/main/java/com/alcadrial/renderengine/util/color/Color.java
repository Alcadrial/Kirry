package com.alcadrial.renderengine.util.color;

public interface Color {
	
	public static ColorRGB of(float r, float g, float b)
	{
		return new ImmutableColorRGB(r, g, b);
	}
	
	public static Color of(float r, float g, float b, float a)
	{
		return new ImmutableColor(r, g, b, a);
	}
	
	ColorRGB LIGHT_RED = of(255, 128, 128);
	ColorRGB LIGHT_GREEN = of(128, 255, 128);
	ColorRGB LIGHT_BLUE = of(128, 128, 255);
	ColorRGB LIGHT_YELLOW = of(255, 255, 0);
	ColorRGB LIGHT_CYAN = of(0, 255, 255);
	ColorRGB LIGHT_MAGENTA = of(255, 0, 255);
	
	ColorRGB RED = of(255, 0, 0);
	ColorRGB GREEN = of(0, 255, 0);
	ColorRGB BLUE = of(0, 0, 255);
	ColorRGB YELLOW = of(128, 128, 0);
	ColorRGB CYAN = of(0, 128, 128);
	ColorRGB MAGENTA = of(128, 0, 128);
	
	ColorRGB DARK_RED = of(128, 0, 0);
	ColorRGB DARK_GREEN = of(0, 128, 0);
	ColorRGB DARK_BLUE = of(0, 0, 128);
	ColorRGB DARK_YELLOW = of(64, 64, 0);
	ColorRGB DARK_CYAN = of(0, 64, 64);
	ColorRGB DARK_MAGENTA = of(64, 0, 64);
	
	ColorRGB VERY_DARK_RED = of(64, 0, 0);
	ColorRGB VERY_DARK_GREEN = of(0, 64, 0);
	ColorRGB VERY_DARK_BLUE = of(0, 0, 64);
	ColorRGB VERY_DARK_YELLOW = of(32, 32, 0);
	ColorRGB VERY_DARK_CYAN = of(0, 32, 32);
	ColorRGB VERY_DARK_MAGENTA = of(32, 0, 32);
	
	ColorRGB WHITE = of(255, 255, 255);
	ColorRGB LIGHT_GRAY = of(191, 191, 191);
	ColorRGB GRAY = of(128, 128, 128);
	ColorRGB DARK_GRAY = of(64, 64, 64);
	ColorRGB BLACK = of(0, 0, 0);
	
	float r();
	float g();
	float b();
	float a();
}
