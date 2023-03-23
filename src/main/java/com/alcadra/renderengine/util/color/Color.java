package com.alcadra.renderengine.util.color;

public interface Color {
	
	public static ColorRGB of(float r, float g, float b)
	{
		return new ImmutableColorRGB(r, g, b);
	}
	
	public static Color of(float r, float g, float b, float a)
	{
		return new ImmutableColor(r, g, b, a);
	}
	
	ColorRGB LIGHT_RED = of(255, 0, 0);
	ColorRGB LIGHT_GREEN = of(0, 255, 0);
	ColorRGB LIGHT_BLUE = of(0, 0, 255);
	ColorRGB LIGHT_YELLOW = of(255, 255, 0);
	ColorRGB LIGHT_CYAN = of(0, 255, 255);
	ColorRGB LIGHT_MAGENTA = of(255, 0, 255);
	
	ColorRGB RED = of(191, 0, 0);
	ColorRGB GREEN = of(0, 191, 0);
	ColorRGB BLUE = of(0, 0, 191);
	ColorRGB YELLOW = of(191, 191, 0);
	ColorRGB CYAN = of(0, 191, 191);
	ColorRGB MAGENTA = of(191, 0, 191);
	
	ColorRGB DARK_RED = of(127, 0, 0);
	ColorRGB DARK_GREEN = of(0, 127, 0);
	ColorRGB DARK_BLUE = of(0, 0, 127);
	ColorRGB DARK_YELLOW = of(127, 127, 0);
	ColorRGB DARK_CYAN = of(0, 127, 127);
	ColorRGB DARK_MAGENTA = of(127, 0, 127);
	
	ColorRGB VERY_DARK_RED = of(63, 0, 0);
	ColorRGB VERY_DARK_GREEN = of(0, 63, 0);
	ColorRGB VERY_DARK_BLUE = of(0, 0, 63);
	ColorRGB VERY_DARK_YELLOW = of(63, 63, 0);
	ColorRGB VERY_DARK_CYAN = of(0, 63, 63);
	ColorRGB VERY_DARK_MAGENTA = of(63, 0, 63);
	
	ColorRGB BLACK = of(0, 0, 0);
	ColorRGB LIGHT_GRAY = of(63, 63, 63);
	ColorRGB GRAY = of(127, 127, 127);
	ColorRGB DARK_GRAY = of(191, 191, 191);
	ColorRGB WHITE = of(255, 255, 255);
	
	float r();
	float g();
	float b();
	float a();
}
