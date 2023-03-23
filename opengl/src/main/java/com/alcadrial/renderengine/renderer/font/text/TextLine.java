package com.alcadrial.renderengine.renderer.font.text;

import java.util.Iterator;

public class TextLine implements Iterable<Character> {
	
	private String line;
	
	public TextLine()
	{
		line = "";
	}
	
	public TextLine(String line)
	{
		this.line = String.valueOf(line);
	}
	
	public TextLine(Object obj)
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
