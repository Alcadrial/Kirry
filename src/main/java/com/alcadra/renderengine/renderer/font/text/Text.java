package com.alcadra.renderengine.renderer.font.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.alcadra.renderengine.util.Nonnull;

public class Text implements Iterable<NonStyledTextLine> {
	
	private List<NonStyledTextLine> lines;
	private boolean isBlank;
	private int length;
	
	public Text(String line)
	{
		this(new NonStyledTextLine(line));
	}
	
	public Text(NonStyledTextLine... lines)
	{
		this.lines = Arrays.asList(lines);
		length = this.lines.parallelStream().collect(() -> 0, (i, l) -> i += l.length(), (i, j) -> i += j);
		isBlank = this.lines.parallelStream().allMatch(NonStyledTextLine::isBlank);
	}
	
	public Text()
	{
		lines = new ArrayList<>();
		length = 0;
		isBlank = true;
	}
	
	public void addLine(@Nonnull NonStyledTextLine line)
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
		addLine(new NonStyledTextLine());
	}
	
	public NonStyledTextLine setLine(int index, @Nonnull NonStyledTextLine line)
	{
		NonStyledTextLine oldLine = lines.set(index, line);
		length += line.length() - oldLine.length();
		if (line.isBlank()) isBlank = true;
		else if (oldLine.isBlank()) isBlank = lines.parallelStream().allMatch(NonStyledTextLine::isBlank);
		return oldLine;
	}
	
	public NonStyledTextLine getLine(int index)
	{
		return lines.get(index);
	}
	
	public boolean isEmpty()
	{
		return rows() == 0 || lines.parallelStream().allMatch(NonStyledTextLine::isEmpty);
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
	public Iterator<NonStyledTextLine> iterator()
	{
		return lines.iterator();
	}
	
	@Override
	public Spliterator<NonStyledTextLine> spliterator()
	{
		return Spliterators.spliterator(lines, 0);
	}
	
	public Stream<NonStyledTextLine> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}
	
	public Stream<NonStyledTextLine> parallelStream()
	{
		return StreamSupport.stream(spliterator(), true);
	}
}
