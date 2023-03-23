package com.alcadra.renderengine.renderer.font.text;

import java.util.Iterator;

import com.alcadra.renderengine.renderer.font.StyledCharacter;
import com.alcadra.renderengine.renderer.font.TextLine;

public class NonStyledTextLine implements TextLine {
	
	private String line;
	
	public NonStyledTextLine()
	{
		line = "";
	}
	
	public NonStyledTextLine(String line)
	{
		this.line = String.valueOf(line);
	}
	
	public NonStyledTextLine(Object obj)
	{
		line = String.valueOf(obj);
	}
	
	public boolean isBlank()
	{
		return line.isBlank();
	}
	
	public boolean isEmpty()
	{
		return line.isEmpty();
	}
	
	public char get(int index)
	{
		return line.charAt(index);
	}
	
	public int length()
	{
		return line.length();
	}
	
	@Override
	public Iterator<StyledCharacter> iterator()
	{
		return new Iterator<>() {
			
			private int i = 0;
			
			@Override
			public boolean hasNext()
			{
				return i < line.length();
			}
			
			@Override
			public StyledCharacter next()
			{
				return line.charAt(i++);
			}
		};
	}
	
	@Override
	public String toString()
	{
		return line;
	}
}
