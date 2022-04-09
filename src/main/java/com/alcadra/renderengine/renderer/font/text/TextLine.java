package com.alcadra.renderengine.renderer.font.text;

import java.util.Iterator;

public class TextLine implements Iterable<Character> {
	
	private String line;
	
	public TextLine(String line)
	{
		this.line = line != null ? line : "";
	}
	
	public boolean isBlank()
	{
		return line.isBlank();
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
	public Iterator<Character> iterator()
	{
		return new Iterator<>() {
			
			private int i = 0;
			
			@Override
			public boolean hasNext()
			{
				return i < line.length();
			}
			
			@Override
			public Character next()
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
