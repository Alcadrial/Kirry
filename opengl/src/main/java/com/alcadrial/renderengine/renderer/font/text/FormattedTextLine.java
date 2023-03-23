package com.alcadrial.renderengine.renderer.font.text;

import java.util.Iterator;

import com.alcadrial.renderengine.renderer.font.RenderCharacter;

public class FormattedTextLine implements Iterable<RenderCharacter> {
	
	private RenderCharacter[] line;
	
	public FormattedTextLine(RenderCharacter[] line)
	{
		this.line = line != null ? line : new RenderCharacter[0];
	}
	
	public boolean isBlank()
	{
		return length() == 0;
	}
	
	public RenderCharacter get(int index)
	{
		return line[index];
	}
	
	public int length()
	{
		return line.length;
	}
	
	@Override
	public Iterator<RenderCharacter> iterator()
	{
		return new Iterator<>() {
			
			private int i = 0;
			
			@Override
			public boolean hasNext()
			{
				return i < length();
			}
			
			@Override
			public RenderCharacter next()
			{
				return line[i++];
			}
		};
	}
	
	@Override
	public String toString()
	{
		String s = "";
		for (RenderCharacter renderCharacter : line) s += (char) renderCharacter.getName();
		return s;
	}
}
