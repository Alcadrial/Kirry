package com.alcadra.renderengine.renderer.font;

public interface Glyph {
	
	int name();
	int x();
	int y();
	int width();
	int height();
	int xOffset();
	int yOffset();
	int advance();
}
