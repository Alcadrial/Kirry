package com.alcadra.renderengine.util.color;

public interface Color {
	
	VectorColorRGB LIGHT_RED = new VectorColorRGB(255, 0, 0);
	VectorColorRGB LIGHT_GREEN = new VectorColorRGB(0, 255, 0);
	VectorColorRGB LIGHT_BLUE = new VectorColorRGB(0, 0, 255);
	VectorColorRGB LIGHT_YELLOW = new VectorColorRGB(255, 255, 0);
	VectorColorRGB LIGHT_CYAN = new VectorColorRGB(0, 255, 255);
	VectorColorRGB LIGHT_MAGENTA = new VectorColorRGB(255, 0, 255);
	
	VectorColorRGB RED = new VectorColorRGB(191, 0, 0);
	VectorColorRGB GREEN = new VectorColorRGB(0, 191, 0);
	VectorColorRGB BLUE = new VectorColorRGB(0, 0, 191);
	VectorColorRGB YELLOW = new VectorColorRGB(191, 191, 0);
	VectorColorRGB CYAN = new VectorColorRGB(0, 191, 191);
	VectorColorRGB MAGENTA = new VectorColorRGB(191, 0, 191);
	
	VectorColorRGB DARK_RED = new VectorColorRGB(127, 0, 0);
	VectorColorRGB DARK_GREEN = new VectorColorRGB(0, 127, 0);
	VectorColorRGB DARK_BLUE = new VectorColorRGB(0, 0, 127);
	VectorColorRGB DARK_YELLOW = new VectorColorRGB(127, 127, 0);
	VectorColorRGB DARK_CYAN = new VectorColorRGB(0, 127, 127);
	VectorColorRGB DARK_MAGENTA = new VectorColorRGB(127, 0, 127);
	
	VectorColorRGB VERY_DARK_RED = new VectorColorRGB(63, 0, 0);
	VectorColorRGB VERY_DARK_GREEN = new VectorColorRGB(0, 63, 0);
	VectorColorRGB VERY_DARK_BLUE = new VectorColorRGB(0, 0, 63);
	VectorColorRGB VERY_DARK_YELLOW = new VectorColorRGB(63, 63, 0);
	VectorColorRGB VERY_DARK_CYAN = new VectorColorRGB(0, 63, 63);
	VectorColorRGB VERY_DARK_MAGENTA = new VectorColorRGB(63, 0, 63);
	
	VectorColorRGB BLACK = new VectorColorRGB(0, 0, 0);
	VectorColorRGB LIGHT_GRAY = new VectorColorRGB(63, 63, 63);
	VectorColorRGB GRAY = new VectorColorRGB(127, 127, 127);
	VectorColorRGB DARK_GRAY = new VectorColorRGB(191, 191, 191);
	VectorColorRGB WHITE = new VectorColorRGB(255, 255, 255);
	
	float r();
	float g();
	float b();
	float a();
}
