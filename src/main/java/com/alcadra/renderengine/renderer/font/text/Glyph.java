package com.alcadra.renderengine.renderer.font.text;

public record Glyph(int name, int x, int y, int width, int height, int xOffset, int yOffset, int advance) implements com.alcadra.renderengine.renderer.font.Glyph {
	
	public static final Glyph INVALID_GLYPH = new Glyph(0, 0, 0, 0, 0, 0, 0, 0);
	
	@Override
	public String toString()
	{
		return "Glyph{char='" + (char) name + "', x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", xOffset=" + xOffset + ", yOffset=" + yOffset + ", advance=" + advance + "}";
	}
}
