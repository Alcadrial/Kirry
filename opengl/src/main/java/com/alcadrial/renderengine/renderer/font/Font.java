package com.alcadrial.renderengine.renderer.font;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alcadrial.renderengine.renderer.font.text.TextLine;
import com.alcadrial.renderengine.textures.Texture;
import com.alcadrial.renderengine.textures.TextureTemplate;
import com.alcadrial.renderengine.util.Image;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class Font {
	
	private String name;
	private int lineHeight, lineOffset;
	private List<Glyph> chars;
	private HashMap<Entry<Integer, Integer>, Integer> extraOffset = new HashMap<>();
	private Texture texture;
	private int spaceIndex;
	private int size;
	
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
				case "name" -> name = reader.nextString();
				case "lineHeight" -> lineHeight = reader.nextInt();
				case "lineOffset" -> lineOffset = reader.nextInt();
				case "size" -> size = reader.nextInt();
				case "characters" ->
				{
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
								case "char" -> name = reader.nextInt();
								case "x" -> x = reader.nextInt();
								case "y" -> y = reader.nextInt();
								case "width" -> width = reader.nextInt();
								case "height" -> height = reader.nextInt();
								case "xOffset" -> xOffset = reader.nextInt();
								case "yOffset" -> yOffset = reader.nextInt();
								case "advance" -> advance = reader.nextInt();
								default -> reader.skipValue();
							}
						}
						chars.add(new Glyph(name, x, y, width, height, xOffset, yOffset, advance));
						reader.endObject();
					}
					reader.endArray();
				}
				case "extra" ->
				{
					reader.beginArray();
					while (reader.hasNext())
					{
						reader.beginObject();
						int before = 0, after = 0, offset = 0;
						while (reader.hasNext())
						{
							switch (reader.nextName())
							{
								case "before" -> before = reader.nextInt();
								case "after" -> after = reader.nextInt();
								case "offset" -> offset = reader.nextInt();
								default -> reader.skipValue();
							}
						}
						extraOffset.put(Map.entry(before, after), offset);
						reader.endObject();
					}
					reader.endArray();
				}
				default -> reader.skipValue();
			}
		}
		reader.endObject();
		chars.sort(Glyph::compareTo);
		for (int i = 0; i < chars.size(); i++) if (chars.get(i).getName() == ' ')
		{
			spaceIndex = i;
			break;
		}
		texture = new Texture(TextureTemplate.RGBA_2D_TEXTURE);
		Image image = new Image(png);
		texture.setData2D(texture.getTemplate(), image.loadToRGBABuffer(), image.getBuffered().getWidth(), image.getBuffered().getHeight(), 0);
	}
	
	public Glyph getGlyph(char c)
	{
		if (c < 0 || c > chars.size()) return Glyph.INVALID_GLYPH;
		if (c > ' ' - spaceIndex)
		{
			Glyph glyph = chars.get(c + spaceIndex - ' ');
			if (glyph.getName() == c) return glyph;
		}
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
		Integer i = extraOffset.get(Map.entry(before, after));
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
	
	public int getSize()
	{
		return size;
	}
	
	public Texture getTexture()
	{
		return texture;
	}
}
