package com.alcadra.renderengine.renderer.font;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL30;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.renderer.BaseRenderer;
import com.alcadra.renderengine.renderer.VertexArray;
import com.alcadra.renderengine.renderer.font.text.Text;
import com.alcadra.renderengine.renderer.font.text.TextLine;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.textures.Texture;

public class FontRenderer extends BaseRenderer {
	
	private Font font;
	
	public FontRenderer(Shader fontShader, Shader textureShader)
	{
		super(fontShader, new VertexArray(), new FloatArrayBuffer(), null);
		getVertexArray().bindBuffer(getArrayBuffer());
		GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 32, 0);
		GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 32, 12);
		GL30.glVertexAttribPointer(2, 3, GL30.GL_FLOAT, false, 32, 20);
		GL30.glEnableVertexAttribArray(0);
		GL30.glEnableVertexAttribArray(1);
		GL30.glEnableVertexAttribArray(2);
		VertexArray.unbind();
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
				vertices.addAll(Arrays.asList(x, y, z, u0, v0, color.x(), color.y(), color.z(), x, y + h, z, u0, v0 + v1, color.x(), color.y(), color.z(), x + w, y + h, z, u0 + u1, v0 + v1, color.x(), color.y(), color.z(), x + w, y, z, u0 + u1, v0, color.x(), color.y(), color.z()));
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
		((FloatArrayBuffer) getArrayBuffer()).addAll(vertices);
	}
	
	public void draw(Matrix4f matrix)
	{
		Texture.setActive(0);
		font.getTexture().bind();
		shader.enable();
		shader.setUniform(0, "atlas");
		shader.setUniform(matrix, "transformation");
		draw(GL30.GL_QUADS);
		Shader.disableShaders();
		getArrayBuffer().clear();
	}
	
	public Font getFont()
	{
		return font;
	}
	
	public void setFont(Font font)
	{
		this.font = font;
	}
}
