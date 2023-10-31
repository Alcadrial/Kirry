package com.alcadrial.renderengine.core.util.color;

import com.alcadrial.kector.Math;

public interface Color {
	
	static ColorRGB of(float r, float g, float b)
	{
		return new ImmutableColorRGB(r, g, b);
	}
	
	static Color of(float r, float g, float b, float a)
	{
		return new ImmutableColor(r, g, b, a);
	}
	
	static ColorRGB mix(ColorRGB c0, ColorRGB c1)
	{
		return mix(c0, c1, 0.5F);
	}
	
	static ColorRGB mix(ColorRGB c0, ColorRGB c1, float f)
	{
		return of(Math.lerp(c0.r(), c1.r(), f), Math.lerp(c0.g(), c1.g(), f), Math.lerp(c0.b(), c1.b(), f));
	}
	
	static Color mix(Color c0, Color c1)
	{
		return mix(c0, c1, 0.5F);
	}
	
	static Color mix(Color c0, Color c1, float f)
	{
		return of(Math.lerp(c0.r(), c1.r(), f), Math.lerp(c0.g(), c1.g(), f), Math.lerp(c0.b(), c1.b(), f), Math.lerp(c0.a(), c1.a(), f));
	}
	
	ColorRGB VERY_LIGHT_RED = of(255, 191, 191);
	ColorRGB VERY_LIGHT_GREEN = of(191, 255, 191);
	ColorRGB VERY_LIGHT_BLUE = of(191, 191, 255);
	ColorRGB VERY_LIGHT_YELLOW = of(255, 255, 128);
	ColorRGB VERY_LIGHT_CYAN = of(128, 255, 255);
	ColorRGB VERY_LIGHT_MAGENTA = of(255, 128, 255);
	
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
	ColorRGB VERY_LIGHT_GRAY = of(213, 213, 213);
	ColorRGB LIGHT_GRAY = of(170, 170, 170);
	ColorRGB GRAY = of(85, 85, 85);
	ColorRGB DARK_GRAY = of(43, 43, 43);
	ColorRGB VERY_DARK_GRAY = of(21, 21, 21);
	ColorRGB BLACK = of(0, 0, 0);
	
	float r();
	float g();
	float b();
	float a();
}
