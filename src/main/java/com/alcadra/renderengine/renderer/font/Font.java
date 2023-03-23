package com.alcadra.renderengine.renderer.font;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.alcadra.renderengine.renderer.font.text.TextLine;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.util.Image;
import com.google.common.collect.HashBasedTable;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class Font {
	
	private String name;
	private int lineHeight, lineOffset;
	private List<Glyph> chars;
	private HashBasedTable<Integer, Integer, Integer> extraOffset = HashBasedTable.create();
	private ColorTexture texture;
	
	public Font(File json, File png) throws IOException
	{
		if (!json.getPath().endsWith(".json")) throw new IOException("json file is not a json one");
		if (!png.getPath().endsWith(".png")) throw new IOException("png image is not a png one");
		
		JsonReader reader = new Gson().newJsonReader(new FileReader(json));
		reader.beginObject();
		while (reader.hasNext())
		{
			switch (reader.nextName())
			{
				case "name":
					name = reader.nextString();
					break;
				case "lineHeight":
					lineHeight = reader.nextInt();
					break;
				case "lineOffset":
					lineOffset = reader.nextInt();
					break;
				case "characters":
					reader.beginArray();
					chars = new ArrayList<>();
					while (reader.hasNext())
					{
						reader.beginObject();
						int name = 0, x = 0, y = 0, width = 0, height = 0, xOffset = 0, yOffset = 0, advance = 0;
						while (reader.hasNext())
						{
							switch (reader.nextName())
							{
								case "char":
									name = reader.nextInt();
									break;
								case "x":
									x = reader.nextInt();
									break;
								case "y":
									y = reader.nextInt();
									break;
								case "width":
									width = reader.nextInt();
									break;
								case "height":
									height = reader.nextInt();
									break;
								case "xOffset":
									xOffset = reader.nextInt();
									break;
								case "yOffset":
									yOffset = reader.nextInt();
									break;
								case "advance":
									advance = reader.nextInt();
									break;
								default:
									reader.skipValue();
									break;
							}
						}
						chars.add(new Glyph(name, x, y, width, height, xOffset, yOffset, advance));
						reader.endObject();
					}
					reader.endArray();
					break;
				case "extra":
					reader.beginArray();
					while (reader.hasNext())
					{
						reader.beginObject();
						int before = 0, after = 0, offset = 0;
						while (reader.hasNext())
						{
							switch (reader.nextName())
							{
								case "before":
									before = reader.nextInt();
									break;
								case "after":
									after = reader.nextInt();
									break;
								case "offset":
									offset = reader.nextInt();
									break;
							}
						}
						extraOffset.put(before, after, offset);
						reader.endObject();
					}
					reader.endArray();
					break;
				default:
					reader.skipValue();
			}
		}
		reader.endObject();
		chars.sort((g, p) -> g.getName() - p.getName());
		texture = new ColorTexture(new Image(png));
	}
	
	public Glyph getGlyph(char c)
	{
		for (int i = 0, j = chars.size() - 1; i <= j;)
		{
			int k = j + i >>> 1;
			Glyph glyph = chars.get(k);
			if (glyph.getName() == c) return glyph;
			if (glyph.getName() < c) i = k + 1;
			else j = k - 1;
		}
		return Glyph.INVALID_GLYPH;
	}
	
	public int getExtraOffset(int before, int after)
	{
		Integer i = extraOffset.get(before, after);
		return i == null ? 0 : i;
	}
	
	public float getLength(TextLine textLine)
	{
		float z = 0;
		for (Character character : textLine) z += getGlyph(character).getAdvance();
		return z;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getLineHeight()
	{
		return lineHeight;
	}
	
	public int getLineOffset()
	{
		return lineOffset;
	}
	
	public ColorTexture getTexture()
	{
		return texture;
	}
}
