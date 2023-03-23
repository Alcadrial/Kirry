package com.alcadra.renderengine.renderer.font;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.renderer.AbstractShaderRenderer;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.renderer.font.text.Text;
import com.alcadra.renderengine.renderer.font.text.TextLine;
import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.shader.uniform.ShaderUniform;
import com.alcadra.renderengine.textures.Texture;
import com.alcadra.renderengine.util.color.Color;
import com.alcadra.renderengine.util.color.VectorColorRGB;

public class FontRenderer extends AbstractShaderRenderer<FloatArrayBuffer> {
	
	private static final ShaderUniform<Matrix4f> TRANSFORMATION = ShaderUniform.ofMatrix4f("TRANSFORMATION");
	private static final ShaderUniform<Vector3f> COLOR = ShaderUniform.ofVector3f("COLOR");
	private static final ShaderUniform<Float> ALFA_MASK = ShaderUniform.ofFloat("ALFA_MASK");
	private static final ShaderUniform<Integer> TEXTURE_SAMPLER = ShaderUniform.ofInt("TEXTURE_SAMPLER");
	
	private Font font;
	private boolean clockWise;
	
	private VectorColorRGB color;
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
	protected void initializeArrayBuffer()
	{
		super.initializeArrayBuffer();
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
	
	public void loadText(Text text, Vector2i position, Color color, float size, int maxLength, float z)
	{
		loadText(text, new Vector2f(position), color, size, maxLength, z);
	}
	
	public void loadText(Text text, Vector2f position, Color color, float size, int maxLength, float z)
	{
		if (text.isEmpty()) return;
		List<List<RenderCharacter>> fontText = new ArrayList<>(text.lines());
		int textSize = 0;
		for (TextLine textLine : text)
		{
			List<RenderCharacter> fontString = new ArrayList<>(textLine.length());
			Glyph fontCharBefore = font.getGlyph(textLine.get(0));
			for (int i = 1; i < textLine.length(); i++)
			{
				Glyph fontChar = font.getGlyph(textLine.get(i));
				fontString.add(new RenderCharacter(fontCharBefore, font.getExtraOffset(fontCharBefore.getName(), fontChar.getName())));
				fontCharBefore = fontChar;
			}
			fontString.add(new RenderCharacter(fontCharBefore, 0));
			fontText.add(fontString);
			textSize += textLine.length();
		}
		Texture texture = font.getTexture();
		List<Float> vertices = new ArrayList<>(textSize * 4);
		float advanced = 0;
		int line = -font.getLineOffset();
		for (List<RenderCharacter> fontString : fontText)
		{
			for (int i = 0; i < fontString.size(); i++)
			{
				RenderCharacter formattedChar = fontString.get(i);
				float x = position.x() + (advanced + formattedChar.getXOffset() + formattedChar.getExtraAdvance()) * size;
				float y = position.y() - (line + formattedChar.getYOffset()) * size;
				float w = formattedChar.getWidth() * size;
				float h = -formattedChar.getHeight() * size;
				float u0 = (float) formattedChar.getX() / texture.getWidth();
				float v0 = (float) formattedChar.getY() / texture.getHeight();
				float u1 = (float) formattedChar.getWidth() / texture.getWidth();
				float v1 = (float) formattedChar.getHeight() / texture.getHeight();
				vertices.addAll(asList(color, x, y, z, w, h, u0, v0, u1, v1));
				advanced += formattedChar.getAdvance();
				if (advanced > maxLength)
				{
					advanced = 0;
					line += font.getLineHeight();
				}
			}
			advanced = 0;
			line += font.getLineHeight();
		}
		arrayBuffer.addAll(vertices);
	}
	
	public void loadText(Matrix4f matrix, Text text, Vector2i position, Color color, float size, int maxLength, float z)
	{
		loadText(matrix, text, new Vector2f(position), color, size, maxLength, z);
	}
	
	public void loadText(Matrix4f matrix, Text text, Vector2f position, Color color, float size, int maxLength, float z)
	{
		if (text.isEmpty()) return;
		List<List<RenderCharacter>> fontText = new ArrayList<>(text.lines());
		int textSize = 0;
		for (TextLine textLine : text)
		{
			List<RenderCharacter> fontString = new ArrayList<>(textLine.length());
			Glyph fontCharBefore = font.getGlyph(textLine.get(0));
			for (int i = 1; i < textLine.length(); i++)
			{
				Glyph fontChar = font.getGlyph(textLine.get(i));
				fontString.add(new RenderCharacter(fontCharBefore, font.getExtraOffset(fontCharBefore.getName(), fontChar.getName())));
				fontCharBefore = fontChar;
			}
			fontString.add(new RenderCharacter(fontCharBefore, 0));
			fontText.add(fontString);
			textSize += textLine.length();
		}
		Texture texture = font.getTexture();
		List<Float> vertices = new ArrayList<>(textSize * 4);
		float advanced = 0;
		int line = -font.getLineOffset();
		for (List<RenderCharacter> fontString : fontText)
		{
			for (int i = 0; i < fontString.size(); i++)
			{
				RenderCharacter formattedChar = fontString.get(i);
				float x = position.x() + (advanced + formattedChar.getXOffset() + formattedChar.getExtraAdvance()) * size;
				float y = position.y() - (line + formattedChar.getYOffset()) * size;
				float w = formattedChar.getWidth() * size;
				float h = -formattedChar.getHeight() * size;
				float u0 = (float) formattedChar.getX() / texture.getWidth();
				float v0 = (float) formattedChar.getY() / texture.getHeight();
				float u1 = (float) formattedChar.getWidth() / texture.getWidth();
				float v1 = (float) formattedChar.getHeight() / texture.getHeight();
				Vector4f vertex = matrix.multiply(new Vector4f(x, y, z, 1));
				x = vertex.x();
				y = vertex.y();
				z = vertex.z();
				vertices.addAll(asList(color, x, y, z, w, h, u0, v0, u1, v1));
				advanced += formattedChar.getAdvance();
				if (advanced > maxLength)
				{
					advanced = 0;
					line += font.getLineHeight();
				}
			}
			advanced = 0;
			line += font.getLineHeight();
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
			vertex[colorOffset + offset] = c.r();
			vertex[colorOffset + offset + 1] = c.g();
			vertex[colorOffset + offset + 2] = c.b();
			vertex[colorOffset + offset + 3] = c.a();
		}
	}
	
	private void addColorRGB(float[] vertex, Color c, int offset)
	{
		if (colorRGBOffset > -1)
		{
			vertex[colorRGBOffset + offset] = c.r();
			vertex[colorRGBOffset + offset + 1] = c.g();
			vertex[colorRGBOffset + offset + 2] = c.b();
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
		font.getTexture().bind();
		program.enable();
		program.setUniform(TRANSFORMATION, matrix);
		program.setUniform(COLOR, color);
		program.setUniform(ALFA_MASK, alfaMask);
		program.setUniform(TEXTURE_SAMPLER, textureSampler);
		draw(RenderingMode.QUADS);
		clear();
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
	
	public void setUniformColor(float r, float g, float b)
	{
		setUniformColor(Color.ofVector(r, g, b));
	}
	
	public void setUniformColor(VectorColorRGB color)
	{
		this.color = color;
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
