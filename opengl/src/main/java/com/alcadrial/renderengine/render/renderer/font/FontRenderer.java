package com.alcadrial.renderengine.render.renderer.font;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.renderengine.core.util.Broken;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.core.util.color.ColorFormat;
import com.alcadrial.renderengine.render.buffer.FloatArrayBuffer;
import com.alcadrial.renderengine.render.renderer.AbstractShaderRenderer;
import com.alcadrial.renderengine.render.renderer.RenderingMode;
import com.alcadrial.renderengine.render.renderer.font.text.Text;
import com.alcadrial.renderengine.render.renderer.font.text.TextLine;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.uniform.ShaderUniform;
import com.alcadrial.renderengine.render.shader.uniform.extra.ColorUniform;
import com.alcadrial.renderengine.render.textures.Texture;

public class FontRenderer extends AbstractShaderRenderer<FloatArrayBuffer> {
	
	private static final ShaderUniform<Matrix4f> TRANSFORMATION = ShaderUniform.ofMatrix4f("TRANSFORMATION");
	private static final ShaderUniform<Color> COLOR = new ColorUniform("COLOR", ColorFormat.RGBA);
	private static final ShaderUniform<Float> ALFA_MASK = ShaderUniform.ofFloat("ALFA_MASK");
	private static final ShaderUniform<Integer> TEXTURE_SAMPLER = ShaderUniform.ofInt("TEXTURE_SAMPLER");
	
	private Font font;
	private boolean clockWise;
	
	private Color color;
	private float alfaMask;
	private int textureSampler;
	
	private int vertexPositionOffset;
	private int colorOffset;
	private int colorRGBOffset;
	private int uvOffset;
	
	public FontRenderer(ShaderProgram program, Font font)
	{
		this(program, font, false);
	}
	
	public FontRenderer(ShaderProgram program, Font font, boolean clockWise)
	{
		super(program, new FloatArrayBuffer());
		this.font = font;
		this.clockWise = clockWise;
		color = Color.BLACK;
		alfaMask = 1;
	}
	
	@Override
	protected void initializeVertexArray()
	{
		super.initializeVertexArray();
		vertexPositionOffset = -1;
		colorOffset = -1;
		colorRGBOffset = -1;
		uvOffset = -1;
		int offset = 0;
		for (ShaderInput input : program.getProgramInputs())
		{
			if (input.equals(ShaderInput.VERTEX_POSITION)) vertexPositionOffset = offset;
			else if (input.equals(ShaderInput.COLOR)) colorOffset = offset;
			else if (input.equals(ShaderInput.COLOR_RGB)) colorRGBOffset = offset;
			else if (input.equals(ShaderInput.UV)) uvOffset = offset;
			offset += input.getType().getSize();
		}
	}
	
	public void loadText(Text text, Vector2i position, Color color, float size, int maxLength, int maxLines, float z, boolean centered)
	{
		loadText(null, text, Vector2f.of(position), color, size, maxLength, maxLines, z, centered, null, 0);
	}
	
	public void loadText(Text text, Vector2f position, Color color, float size, int maxLength, int maxLines, float z, boolean centered)
	{
		loadText(null, text, position, color, size, maxLength, maxLines, z, centered, null, 0);
	}
	
	public void loadText(@Nullable Matrix4f matrix, Text text, Vector2i position, Color color, float size, int maxLength, int maxLines, float z, boolean centered)
	{
		loadText(matrix, text, Vector2f.of(position), color, size, maxLength, maxLines, z, centered, null, 0);
	}
	
	public void loadText(@Nullable Matrix4f matrix, Text text, Vector2f position, Color color, float size, int maxLength, int maxLines, float z, boolean centered)
	{
		loadText(matrix, text, position, color, size, maxLength, maxLines, z, centered, null, 0);
	}
	
	public void loadText(Text text, Vector2i position, Color color, float size, int maxLength, int maxLines, float z, boolean centered, @Nullable Color shadow, @Nullable float shadowDistance)
	{
		loadText(null, text, Vector2f.of(position), color, size, maxLength, maxLines, z, centered, shadow, shadowDistance);
	}
	
	public void loadText(Text text, Vector2f position, Color color, float size, int maxLength, int maxLines, float z, boolean centered, @Nullable Color shadow, @Nullable float shadowDistance)
	{
		loadText(null, text, position, color, size, maxLength, maxLines, z, centered, shadow, shadowDistance);
	}
	
	public void loadText(@Nullable Matrix4f matrix, Text text, Vector2i position, Color color, float size, int maxLength, int maxLines, float z, boolean centered, @Nullable Color shadow, @Nullable float shadowDistance)
	{
		loadText(matrix, text, Vector2f.of(position), color, size, maxLength, maxLines, z, centered, shadow, shadowDistance);
	}
	
	public void loadText(@Nullable Matrix4f matrix, Text text, Vector2f position, Color color, float size, int maxLength, int maxLines, float z, boolean centered, @Nullable Color shadow, @Nullable float shadowDistance)
	{
		if (text.isEmpty()) return;
		if (size < 0) size = 16;
		size /= font.getSize();
		if (maxLength < 0) maxLength = Integer.MAX_VALUE;
		if (maxLines < 0) maxLines = Integer.MAX_VALUE;
		boolean hasShadow = shadow != null && shadowDistance != 0 && shadow.a() > 0;
		
		class Row {
			
			List<RenderCharacter> chars;
			float length;
		}
		
		List<Row> fontText = new ArrayList<>(Math.min(text.rows(), maxLines));
		int textSize = 0;
		out:
		for (TextLine textLine : text)
		{
			Row row = new Row();
			row.chars = new ArrayList<>(textLine.length());
			
			if (textLine.length() > 0)
			{
				Glyph fontCharBefore = font.getGlyph(textLine.get(0));
				row.length += fontCharBefore.getAdvance();
				for (int i = 1; i < textLine.length(); i++)
				{
					Glyph fontChar = font.getGlyph(textLine.get(i));
					RenderCharacter c = new RenderCharacter(fontCharBefore, font.getExtraOffset(fontCharBefore.getName(), fontChar.getName()));
					row.chars.add(c);
					row.length += c.getAdvance();
					fontCharBefore = fontChar;
					if (row.length > maxLength)
					{
						row.length -= c.getAdvance();
						fontText.add(row);
						if (fontText.size() == maxLines)
						{
							textSize += i + 1;
							break out;
						}
						row = new Row();
						row.chars = new ArrayList<>(textLine.length());
					}
				}
				row.chars.add(new RenderCharacter(fontCharBefore, 0));
				textSize += textLine.length();
			}
			fontText.add(row);
			if (fontText.size() == maxLines) break out;
		}
		Texture texture = font.getTexture();
		List<Float> vertices = new ArrayList<>(textSize * 4);
		float advanced = 0;
		float offset = 0;
		int line = -font.getLineOffset();
		//loop:
		{
			if (matrix != null)
			{
				Vector4f vertex = Vector4f.of(0, 0, 0, 0);
				for (Row fontString : fontText)
				{
					if (centered) offset = -fontString.length / 2;
					for (int i = 0; i < fontString.chars.size(); i++)
					{
						RenderCharacter formattedChar = fontString.chars.get(i);
						float sx = (offset + advanced + formattedChar.getXOffset() + formattedChar.getExtraAdvance()) * size;
						float sy = (line + formattedChar.getYOffset()) * size;
						float x = position.x() + sx;
						float y = position.y() - sy;
						float w = formattedChar.getWidth() * size;
						float h = -formattedChar.getHeight() * size;
						float u0 = (float) formattedChar.getX() / texture.getWidth();
						float v0 = (float) formattedChar.getY() / texture.getHeight();
						float u1 = (float) formattedChar.getWidth() / texture.getWidth();
						float v1 = (float) formattedChar.getHeight() / texture.getHeight();
						Vector4f vertex0 = matrix.multiply(vertex.setAll(x, y, z, 1));
						vertices.addAll(asList(color, vertex0.x(), vertex0.y(), vertex0.z(), w, h, u0, v0, u1, v1));
						if (hasShadow)
						{
							float x0 = x + shadowDistance;
							float y0 = y - shadowDistance;
							Vector4f vertex1 = matrix.multiply(vertex.setAll(x0, y0, z, 1));
							vertices.addAll(asList(shadow, vertex1.x(), vertex1.y(), vertex1.z(), w, h, u0, v0, u1, v1));
						}
						advanced += formattedChar.getAdvance();
					}
					advanced = 0;
					line += font.getLineHeight();
				}
			}
			else
			{
				for (Row fontString : fontText)
				{
					if (centered) offset = -fontString.length / 2;
					for (int i = 0; i < fontString.chars.size(); i++)
					{
						RenderCharacter formattedChar = fontString.chars.get(i);
						float sx = (offset + advanced + formattedChar.getXOffset() + formattedChar.getExtraAdvance()) * size;
						float sy = (line + formattedChar.getYOffset()) * size;
						float x = position.x() + sx;
						float y = position.y() - sy;
						float w = formattedChar.getWidth() * size;
						float h = -formattedChar.getHeight() * size;
						float u0 = (float) formattedChar.getX() / texture.getWidth();
						float v0 = (float) formattedChar.getY() / texture.getHeight();
						float u1 = (float) formattedChar.getWidth() / texture.getWidth();
						float v1 = (float) formattedChar.getHeight() / texture.getHeight();
						vertices.addAll(asList(color, x, y, z, w, h, u0, v0, u1, v1));
						if (hasShadow)
						{
							float x0 = x + shadowDistance;
							float y0 = y - shadowDistance;
							vertices.addAll(asList(shadow, x0, y0, z, w, h, u0, v0, u1, v1));
						}
						advanced += formattedChar.getAdvance();
					}
					advanced = 0;
					line += font.getLineHeight();
				}
			}
		}
		arrayBuffer.addAll(vertices);
	}
	
	private List<Float> asList(Color color, float x, float y, float z, float w, float h, float u0, float v0, float u1, float v1)
	{
		float[] vertex = new float[vertexSize()];
		int size = vertexSize() / 4;
		
		int offset = 0;
		
		if (clockWise)
		{
			addVertexPosition(vertex, x, y + h, z, offset);
			addColor(vertex, color, offset);
			addColorRGB(vertex, color, offset);
			addUV(vertex, u0, v0 + v1, offset);
			
			offset += size;
			addVertexPosition(vertex, x + w, y + h, z, offset);
			addColor(vertex, color, offset);
			addColorRGB(vertex, color, offset);
			addUV(vertex, u0 + u1, v0 + v1, offset);
			
			offset += size;
			addVertexPosition(vertex, x + w, y, z, offset);
			addColor(vertex, color, offset);
			addColorRGB(vertex, color, offset);
			addUV(vertex, u0 + u1, v0, offset);
			
			offset += size;
			addVertexPosition(vertex, x, y, z, offset);
			addColor(vertex, color, offset);
			addColorRGB(vertex, color, offset);
			addUV(vertex, u0, v0, offset);
		}
		else
		{
			addVertexPosition(vertex, x + w, y + h, z, offset);
			addColor(vertex, color, offset);
			addColorRGB(vertex, color, offset);
			addUV(vertex, u0 + u1, v0 + v1, offset);
			
			offset += size;
			addVertexPosition(vertex, x, y + h, z, offset);
			addColor(vertex, color, offset);
			addColorRGB(vertex, color, offset);
			addUV(vertex, u0, v0 + v1, offset);
			
			offset += size;
			addVertexPosition(vertex, x, y, z, offset);
			addColor(vertex, color, offset);
			addColorRGB(vertex, color, offset);
			addUV(vertex, u0, v0, offset);
			
			offset += size;
			addVertexPosition(vertex, x + w, y, z, offset);
			addColor(vertex, color, offset);
			addColorRGB(vertex, color, offset);
			addUV(vertex, u0 + u1, v0, offset);
		}
		
		return new AbstractList<>() {
			
			@Override
			public Float get(int index)
			{
				return vertex[index];
			}
			
			@Override
			public int size()
			{
				return vertex.length;
			}
		};
	}
	
	private void addVertexPosition(float[] vertex, float x, float y, float z, int offset)
	{
		if (vertexPositionOffset > -1)
		{
			vertex[vertexPositionOffset + offset] = x;
			vertex[vertexPositionOffset + offset + 1] = y;
			vertex[vertexPositionOffset + offset + 2] = z;
		}
	}
	
	private void addColor(float[] vertex, Color c, int offset)
	{
		if (colorOffset > -1)
		{
			vertex[colorOffset + offset] = c.r() / 255;
			vertex[colorOffset + offset + 1] = c.g() / 255;
			vertex[colorOffset + offset + 2] = c.b() / 255;
			vertex[colorOffset + offset + 3] = c.a() / 255;
		}
	}
	
	private void addColorRGB(float[] vertex, Color c, int offset)
	{
		if (colorRGBOffset > -1)
		{
			vertex[colorRGBOffset + offset] = c.r() / 255;
			vertex[colorRGBOffset + offset + 1] = c.g() / 255;
			vertex[colorRGBOffset + offset + 2] = c.b() / 255;
		}
	}
	
	private void addUV(float[] vertex, float u, float v, int offset)
	{
		if (uvOffset > -1)
		{
			vertex[uvOffset + offset] = u;
			vertex[uvOffset + offset + 1] = v;
		}
	}
	
	public void draw(Matrix4f matrix)
	{
		draw(RenderingMode.QUADS, matrix);
	}
	
	@Override
	public void draw(RenderingMode mode, Matrix4f matrix, int count, int offset)
	{
		font.getTexture().bind();
		program.enable();
		program.setUniform(TRANSFORMATION, matrix);
		program.setUniform(COLOR, color);
		program.setUniform(ALFA_MASK, alfaMask);
		program.setUniform(TEXTURE_SAMPLER, textureSampler);
		draw(mode, count * 4 * vertexSize(), offset * 4);
	}
	
	/**
	 * Draw the texture containing the font using the uniform color
	 * 
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param width
	 * @param height
	 */
	@Broken
	public void drawFontTexture(Matrix4f matrix, float x, float y, float z, float width, float height)
	{
		arrayBuffer.addAll(asList(color, x, y, z, width, height, 0, 0, 1, 1));
		draw(matrix);
	}
	
	public Font getFont()
	{
		return font;
	}
	
	public boolean isClockWise()
	{
		return clockWise;
	}
	
	public void setFont(Font font)
	{
		this.font = font;
	}
	
	public void setClockWise(boolean clockWise)
	{
		this.clockWise = clockWise;
	}
	
	public void setUniformColor(Color color)
	{
		this.color = color;
	}
	
	public void setUniformColor(float r, float g, float b)
	{
		color = Color.of(r, g, b);
	}
	
	public void setUniformAlfaMask(float alfaMask)
	{
		this.alfaMask = alfaMask > 255 ? 1 : alfaMask / 255;
	}
	
	public void setUniformTextureSampler(int textureSampler)
	{
		this.textureSampler = textureSampler;
	}
	
	@Override
	public String toString()
	{
		return "FontRenderer{" + arrayBuffer + "}";
	}
}
