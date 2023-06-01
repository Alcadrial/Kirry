package com.alcadrial.renderengine.render.renderer.font;

public class Glyph implements Comparable<Glyph> {
	
	public static final Glyph INVALID_GLYPH = new Glyph(0, 0, 0, 0, 0, 0, 0, 0);
	
	private final int name;
	private final int x, y, width, height, xOffset, yOffset;
	private final int advance;
	
	public Glyph(int name, int x, int y, int width, int height, int xOffset, int yOffset, int advance)
	{
		this.name = name;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.advance = advance;
	}
	
	@Override
	public int compareTo(Glyph o)
	{
		return name - o.name;
	}
	
	public int getName()
	{
		return name;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getXOffset()
	{
		return xOffset;
	}
	
	public int getYOffset()
	{
		return yOffset;
	}
	
	public int getAdvance()
	{
		return advance;
	}
	
	@Override
	public String toString()
	{
		return "FontCharacter{char='" + (char) name + "', x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", xOffset=" + xOffset + ", yOffset=" + yOffset + ", advance=" + advance + "}";
	}
}
