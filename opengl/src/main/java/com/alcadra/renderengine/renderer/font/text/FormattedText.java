package com.alcadra.renderengine.renderer.font.text;

import java.util.Iterator;

public class FormattedText implements Iterable<FormattedTextLine> {
	
	private FormattedTextLine[] lines;
	
	public FormattedText(FormattedTextLine[] lines)
	{
		this.lines = lines != null ? lines : new FormattedTextLine[0];
	}
	
	public FormattedTextLine getLine(int index)
	{
		return lines[index];
	}
	
	public boolean isEmpty()
	{
		if (lines() > 0) for (FormattedTextLine formattedTextLine : lines) if (!formattedTextLine.isBlank()) return false;
		return true;
	}
	
	public int lines()
	{
		return lines.length;
	}
	
	@Override
	public Iterator<FormattedTextLine> iterator()
	{
		return new Iterator<>() {
			
			private int i = 0;
			
			@Override
			public boolean hasNext()
			{
				return i < lines();
			}
			
			@Override
			public FormattedTextLine next()
			{
				return lines[i++];
			}
		};
	}
}
