package com.alcadra.renderengine.renderer.font;

import com.alcadra.renderengine.renderer.font.text.Glyph;

public class RenderCharacter {
	
	private Glyph character;
	private int extraAdvance;
	
	public RenderCharacter(Glyph character, int extraAdvance)
	{
		this.character = character;
		this.extraAdvance = extraAdvance;
	}
	
	public int getExtraAdvance()
	{
		return extraAdvance;
	}
	
	public int getName()
	{
		return character.name();
	}
	
	public int getX()
	{
		return character.x();
	}
	
	public int getY()
	{
		return character.y();
	}
	
	public int getWidth()
	{
		return character.width();
	}
	
	public int getHeight()
	{
		return character.height();
	}
	
	public int getXOffset()
	{
		return character.xOffset();
	}
	
	public int getYOffset()
	{
		return character.yOffset();
	}
	
	public int getAdvance()
	{
		return character.advance();
	}
}