package com.alcadra.renderengine.renderer.font.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.alcadra.renderengine.util.Nonnull;

public class Text implements Iterable<TextLine> {
	
	private List<TextLine> lines;
	private boolean isBlank;
	private int length;
	
	public Text(String line)
	{
		this(new TextLine(line));
	}
	
	public Text(TextLine... lines)
	{
		this.lines = Arrays.asList(lines);
		length = this.lines.parallelStream().collect(() -> 0, (i, l) -> i += l.length(), (i, j) -> i += j);
		isBlank = this.lines.parallelStream().allMatch(TextLine::isBlank);
	}
	
	public Text()
	{
		lines = new ArrayList<>();
		length = 0;
		isBlank = true;
	}
	
	public void addLine(@Nonnull TextLine line)
	{
		try
		{
			lines.add(line);
		}
		catch (UnsupportedOperationException e)
		{
			lines = new ArrayList<>(lines);
			lines.add(line);
		}
		length += line.length();
		isBlank |= line.isBlank();
	}
	
	public void addNewLine()
	{
		addLine(new TextLine());
	}
	
	public TextLine setLine(int index, @Nonnull TextLine line)
	{
		TextLine oldLine = lines.set(index, line);
		length += line.length() - oldLine.length();
		if (line.isBlank()) isBlank = true;
		else if (oldLine.isBlank()) isBlank = lines.parallelStream().allMatch(TextLine::isBlank);
		return oldLine;
	}
	
	public TextLine getLine(int index)
	{
		return lines.get(index);
	}
	
	public boolean isEmpty()
	{
		return rows() == 0 || lines.parallelStream().allMatch(TextLine::isEmpty);
	}
	
	public boolean isBlank()
	{
		return isBlank;
	}
	
	public int rows()
	{
		return lines.size();
	}
	
	public int length()
	{
		return length;
	}
	
	@Override
	public Iterator<TextLine> iterator()
	{
		return lines.iterator();
	}
	
	@Override
	public Spliterator<TextLine> spliterator()
	{
		return Spliterators.spliterator(lines, 0);
	}
	
	public Stream<TextLine> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}
	
	public Stream<TextLine> parallelStream()
	{
		return StreamSupport.stream(spliterator(), true);
	}
	
	@Override
	public String toString()
	{
		return stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]"));
	}
}
