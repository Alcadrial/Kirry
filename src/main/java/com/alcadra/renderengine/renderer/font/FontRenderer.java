package com.alcadra.renderengine.renderer.font;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL30;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.renderer.BaseRenderer;
import com.alcadra.renderengine.renderer.VertexArray;
import com.alcadra.renderengine.renderer.font.text.Text;
import com.alcadra.renderengine.renderer.font.text.TextLine;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.textures.Texture;

public class FontRenderer extends BaseRenderer<FloatArrayBuffer> {
	
	private Font font;
	private Vector3f lastColor;
	
	@Deprecated(since = "1.2", forRemoval = true)
	public FontRenderer(Shader fontShader, Shader textureShader)
	{
		super(fontShader, new VertexArray(), new FloatArrayBuffer());
		vertexArray.bindBuffer(arrayBuffer);
		vertexArray.vertexAttributePointer(0, 3, GL30.GL_FLOAT, false, 32, 0);
		vertexArray.vertexAttributePointer(1, 3, GL30.GL_FLOAT, false, 32, 12);
		vertexArray.vertexAttributePointer(2, 2, GL30.GL_FLOAT, false, 32, 24);
		vertexArray.enableVertexAttribute(0);
		vertexArray.enableVertexAttribute(1);
		vertexArray.enableVertexAttribute(2);
		VertexArray.unbind();
		lastColor = Vector3f.getOrigin();
	}
	
	public FontRenderer(Shader shader)
	{
		super(shader, new VertexArray(), new FloatArrayBuffer());
		vertexArray.bindBuffer(arrayBuffer);
		int size = shader.useColor() ? 32 : 20;
		int offset = 0, id = 0;
		vertexArray.vertexAttributePointer(id, 3, GL30.GL_FLOAT, false, size, offset);
		vertexArray.enableVertexAttribute(id++);
		offset += 12;
		if (shader.useColor())
		{
			vertexArray.vertexAttributePointer(id, 3, GL30.GL_FLOAT, false, size, offset);
			vertexArray.enableVertexAttribute(id++);
			offset += 12;
		}
		vertexArray.vertexAttributePointer(id, 2, GL30.GL_FLOAT, false, size, offset);
		vertexArray.enableVertexAttribute(id);
		VertexArray.unbind();
		lastColor = Vector3f.getOrigin();
	}
	
	public void loadText(Text text, Vector2i position, float size, int maxLength, float z)
	{
		loadText(text, position, new Vector3f(0, 0, 0), size, maxLength, z);
	}
	
	public void loadText(Text text, Vector2i position, Vector3f color, float size, int maxLength, float z)
	{
		loadText(text, new Vector2f(position), color, size, maxLength, z);
	}
	
	public void loadText(Text text, Vector2f position, Vector3f color, float size, int maxLength, float z)
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
		lastColor = color;
	}
	
	public void loadText(Matrix4f matrix, Text text, Vector2i position, float size, int maxLength, float z)
	{
		loadText(matrix, text, position, new Vector3f(0, 0, 0), size, maxLength, z);
	}
	
	public void loadText(Matrix4f matrix, Text text, Vector2i position, Vector3f color, float size, int maxLength, float z)
	{
		loadText(matrix, text, new Vector2f(position), color, size, maxLength, z);
	}
	
	public void loadText(Matrix4f matrix, Text text, Vector2f position, Vector3f color, float size, int maxLength, float z)
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
		lastColor = color;
	}
	
	private List<Float> asList(Vector3f color, float x, float y, float z, float w, float h, float u0, float v0, float u1, float v1)
	{
		if (shader.useColor()) return List.of(x, y, z, color.x(), color.y(), color.z(), u0, v0, x, y + h, z, color.x(), color.y(), color.z(), u0, v0 + v1, x + w, y + h, z, color.x(), color.y(), color.z(), u0 + u1, v0 + v1, x + w, y, z, color.x(), color.y(), color.z(), u0 + u1, v0);
		return List.of(x, y, z, u0, v0, x, y + h, z, u0, v0 + v1, x + w, y + h, z, u0 + u1, v0 + v1, x + w, y, z, u0 + u1, v0);
	}
	
	public void loadData(FontRenderer fontRenderer)
	{
		if (fontRenderer.shader.useColor() == shader.useColor())
		{
			arrayBuffer.merge(fontRenderer.arrayBuffer);
			return;
		}
		List<Float> data = fontRenderer.arrayBuffer.asList();
		if (shader.useColor())
		{
			int size = data.size() / 5;
			for (int i = 0; i < size; i++)
			{
				arrayBuffer.add(data.get(i * 5));
				arrayBuffer.add(data.get(i * 5 + 1));
				arrayBuffer.add(data.get(i * 5 + 2));
				arrayBuffer.add(lastColor.x());
				arrayBuffer.add(lastColor.y());
				arrayBuffer.add(lastColor.z());
				arrayBuffer.add(data.get(i * 5 + 3));
				arrayBuffer.add(data.get(i * 5 + 4));
			}
		}
		else
		{
			int size = data.size() / 8;
			for (int i = 0; i < size; i++)
			{
				arrayBuffer.add(data.get(i * 8));
				arrayBuffer.add(data.get(i * 8 + 1));
				arrayBuffer.add(data.get(i * 8 + 2));
				arrayBuffer.add(data.get(i * 8 + 6));
				arrayBuffer.add(data.get(i * 8 + 7));
			}
		}
	}
	
	public void draw(Matrix4f matrix)
	{
		Texture.setActive(0);
		font.getTexture().bind();
		shader.enable();
		shader.setUniform(0, "atlas");
		shader.setUniform(matrix, "transformation");
		draw(GL30.GL_QUADS, arrayBuffer.size());
		Shader.disableShaders();
		clear();
	}
	
	public Font getFont()
	{
		return font;
	}
	
	public void setFont(Font font)
	{
		this.font = font;
	}
	
	@Override
	public String toString()
	{
		return "FontRenderer{" + arrayBuffer + "}";
	}
}
