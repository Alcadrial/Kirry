package com.alcadrial.kirry.render.renderer.font;

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
		return character.getName();
	}
	
	public int getX()
	{
		return character.getX();
	}
	
	public int getY()
	{
		return character.getY();
	}
	
	public int getWidth()
	{
		return character.getWidth();
	}
	
	public int getHeight()
	{
		return character.getHeight();
	}
	
	public int getXOffset()
	{
		return character.getXOffset();
	}
	
	public int getYOffset()
	{
		return character.getYOffset();
	}
	
	public int getAdvance()
	{
		return character.getAdvance();
	}
}