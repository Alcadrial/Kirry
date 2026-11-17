package com.alcadrial.kirry.render.font;

import com.alcadrial.kector.vector.Vector3dr;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kector.vector.Vector3lr;
import com.alcadrial.kirry.render.buffer.writer.BufferWriter;
import com.alcadrial.kirry.render.font.Font.GlyphData;
import com.alcadrial.kirry.render.util.exception.FreeTypeException;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class DefaultStringFormatter implements StringFormatter {
	
	private Vector3f origin;
	
	private float maxWidth;
	private float maxHeight;
	private float scale;
	
	public DefaultStringFormatter()
	{
		origin = Vector3f.of();
		
		maxWidth = -1;
		maxHeight = -1;
		scale = 1;
	}
	
	@Override
	public int loadText(Font font, String text, BufferWriter dst) throws VulkanException
	{
		int vertexCount = 0;
		Vector3f pos = Vector3f.of(origin).setY(origin.y() + font.getHeight());
		for (char c : text.toCharArray())
		{
			try
			{
				if (c == '\t')
				{
					GlyphData glyphData = font.getData(' ');
					pos.add(glyphData.getAdvanceX() * 4, 0);
					continue;
				}
				if (c == '\r')
				{
					pos.x(origin.x());
					continue;
				}
				if (c == '\n')
				{
					pos.x(origin.x());
					pos.y(pos.y() + font.getHeight());
					continue;
				}
				GlyphData glyphData = font.getData(c);
				if (!glyphData.isEmpty())
				{
					dst.put(pos);
					dst.put(glyphData.getIndex());
					vertexCount++;
				}
				pos.add(glyphData.getAdvanceX(), 0);
				if (maxWidth != -1)
				{
					if (pos.x() + glyphData.getHorizontalBearingX() + glyphData.getWidth() >= maxWidth / scale)
					{
						pos.x(origin.x());
						pos.y(pos.y() + font.getHeight());
					}
				}
				if (maxHeight != -1 && pos.y() >= maxHeight / scale)
				{
					break;
				}
			}
			catch (FreeTypeException _)
			{}
		}
		return vertexCount;
	}
	
	public void setMaxWidth(float maxWidth)
	{
		this.maxWidth = maxWidth;
	}
	
	public void setMaxHeight(float maxHeight)
	{
		this.maxHeight = maxHeight;
	}
	
	public void setScale(float scale)
	{
		this.scale = scale;
	}
	
	public void setOrigin(Vector3fr origin)
	{
		this.origin.setAll(origin);
	}
	
	public void setOrigin(Vector3ir origin)
	{
		this.origin.setAll(origin);
	}
	
	public void setOrigin(Vector3lr origin)
	{
		this.origin.setAll(origin);
	}
	
	public void setOrigin(Vector3dr origin)
	{
		this.origin.setAll(origin);
	}
}
