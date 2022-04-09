package com.alcadra.renderengine.renderer.font.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Text implements Iterable<TextLine> {
	
	private List<TextLine> lines;
	
	public Text(String line)
	{
		this(new TextLine(line));
	}
	
	public Text(TextLine... lines)
	{
		this.lines = Arrays.asList(lines);
	}
	
	public Text()
	{
		lines = new ArrayList<>();
	}
	
	public void addLine(TextLine line)
	{
		if (line == null) return;
		try
		{
			lines.add(line);
		}
		catch (UnsupportedOperationException e)
		{
			lines = new ArrayList<>(lines);
			lines.add(line);
		}
	}
	
	public TextLine setLine(int index, TextLine line)
	{
		if (line == null) return line;
		return lines.set(index, line);
	}
	
	public TextLine getLine(int index)
	{
		return lines.get(index);
	}
	
	public boolean isEmpty()
	{
		return lines() == 0 || !lines.parallelStream().anyMatch(line -> !line.isBlank());
	}
	
	public int lines()
	{
		return lines.size();
	}
	
	@Override
	public Iterator<TextLine> iterator()
	{
		return lines.iterator();
	}
}
