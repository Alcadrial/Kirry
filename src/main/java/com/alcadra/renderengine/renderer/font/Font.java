package com.alcadra.renderengine.renderer.font;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alcadra.renderengine.renderer.font.text.Glyph;
import com.alcadra.renderengine.renderer.font.text.NonStyledTextLine;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.util.Image;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class Font {
	
	private static final Gson GSON = new Gson();
	private String name;
	private int lineHeight, lineOffset;
	private Map<GlyphStyle, List<Glyph>> chars;
	private HashMap<Entry<Integer, Integer>, Integer> extraOffset = new HashMap<>();
	private ColorTexture texture;
	
	public Font(File json, File png) throws IOException
	{
		addFont(json, png);
	}
	
	public void addFont(File json, File png) throws IOException
	{
		if (!json.getPath().endsWith(".json")) throw new IOException("json file is not a json one");
		if (!png.getPath().endsWith(".png")) throw new IOException("png image is not a png one");
		
		try (JsonReader reader = GSON.newJsonReader(new FileReader(json)))
		{
			reader.beginObject();
			if (!reader.nextName().equals("name")) throw new InvalidJsonStructureException("'name' is not first!");
			String n = reader.nextString();
			if (name == null) name = n;
			else if (!name.equals(n)) return;
			int state = 0;
			GlyphStyle style = GlyphStyle.NORMAL;
			List<Glyph> chars = new ArrayList<>();
			while (reader.hasNext())
			{
				switch (reader.nextName())
				{
					case "bold":
						if (reader.nextBoolean())
						{
							if (style == GlyphStyle.ITALICS) style = GlyphStyle.BOLD_ITALICS;
							else style = GlyphStyle.BOLD;
						}
						state++;
						break;
					case "italic":
						if (reader.nextBoolean())
						{
							if (style == GlyphStyle.BOLD) style = GlyphStyle.BOLD_ITALICS;
							else style = GlyphStyle.ITALICS;
						}
						state++;
						break;
					case "lineHeight":
						lineHeight = reader.nextInt();
						break;
					case "lineOffset":
						lineOffset = reader.nextInt();
						break;
					case "characters":
						if (state < 2) throw new InvalidJsonStructureException("'characters' is before 'bold' and 'italics'!");
						reader.beginArray();
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
							extraOffset.put(Map.entry(before, after), offset);
							reader.endObject();
						}
						reader.endArray();
						break;
					default:
						reader.skipValue();
				}
			}
			reader.endObject();
			chars.sort((g, p) -> g.name() - p.name());
			this.chars.put(style, chars);
		}
		texture = new ColorTexture(new Image(png));
	}
	
	public Glyph getGlyph(GlyphStyle style, char c)
	{
		List<Glyph> list = chars.get(style);
		for (int i = 0, j = chars.size() - 1; i <= j;)
		{
			int k = j + i >>> 1;
			Glyph glyph = list.get(k);
			if (glyph.name() == c) return glyph;
			if (glyph.name() < c) i = k + 1;
			else j = k - 1;
		}
		return Glyph.INVALID_GLYPH;
	}
	
	public int getExtraOffset(int before, int after)
	{
		Integer i = extraOffset.get(Map.entry(before, after));
		return i == null ? 0 : i;
	}
	
	public float getLength(NonStyledTextLine textLine)
	{
		float z = 0;
		for (Character character : textLine) z += getGlyph(GlyphStyle.NORMAL, character).advance();
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
