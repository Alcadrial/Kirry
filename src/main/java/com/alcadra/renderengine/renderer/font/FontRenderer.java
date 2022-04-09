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
import com.alcadra.renderengine.buffer.FrameBuffer;
import com.alcadra.renderengine.renderer.BaseRenderer;
import com.alcadra.renderengine.renderer.TextureRenderer;
import com.alcadra.renderengine.renderer.VertexArray;
import com.alcadra.renderengine.renderer.font.text.Text;
import com.alcadra.renderengine.renderer.font.text.TextLine;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.textures.Texture;

public class FontRenderer extends BaseRenderer {
	
	private final FrameBuffer frameBuffer = new FrameBuffer();
	private final TextureRenderer textureRenderer;
	
	public FontRenderer(Shader fontShader, Shader textureShader)
	{
		super(fontShader, new VertexArray(), new FloatArrayBuffer(), null);
		getVertexArray().bindBuffer(getArrayBuffer());
		GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, 16, 0);
		GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 16, 8);
		GL30.glEnableVertexAttribArray(0);
		GL30.glEnableVertexAttribArray(1);
		VertexArray.unbind();
		textureRenderer = new TextureRenderer(textureShader);
	}
	
	public void drawString(Matrix4f matrix, Font font, Text text, Vector2i position, float size, float z)
	{
		drawString(matrix, font, text, position, new Vector3f(0, 0, 0), size, z);
	}
	
	public void drawString(Matrix4f matrix, Font font, Text text, Vector2i position, Vector3f color, float size, float z)
	{
		drawString(matrix, font, text, position, color, size, 500, z);
	}
	
	public void drawString(Matrix4f matrix, Font font, Text text, Vector2i position, float size, int maxLength, float z)
	{
		drawString(matrix, font, text, position, new Vector3f(0, 0, 0), size, maxLength, z);
	}
	
	public void drawString(Matrix4f matrix, Font font, Text text, Vector2i position, Vector3f color, float size, int maxLength, float z)
	{
		drawString(matrix, font, text, new Vector2f(position), color, size, maxLength, z);
	}
	
	public void drawString(Matrix4f matrix, Font font, Text text, Vector2f position, Vector3f color, float size, int maxLength, float z)
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
				vertices.add(x);
				vertices.add(y);
				vertices.add(u0);
				vertices.add(v0);
				vertices.add(x);
				vertices.add(y + h);
				vertices.add(u0);
				vertices.add(v0 + v1);
				vertices.add(x + w);
				vertices.add(y + h);
				vertices.add(u0 + u1);
				vertices.add(v0 + v1);
				vertices.add(x + w);
				vertices.add(y);
				vertices.add(u0 + u1);
				vertices.add(v0);
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
		getArrayBuffer().clear();
		getArrayBuffer().addAll(vertices);
		Texture.setActive(0);
		texture.bind();
		shader.enable();
		shader.setUniform((Vector4f) color.multiply(1 / 255F), "color");
		shader.setUniform(z / Integer.MAX_VALUE, "z");
		shader.setUniform(0, "atlas");
		shader.setUniform(matrix, "transformation");
		frameBuffer.bind();
		draw(GL30.GL_QUADS);
		FrameBuffer.unbind();
		Shader.disableShaders();
	}
	
	public void drawTexture(Matrix4f matrix, float z)
	{
		textureRenderer.clear();
		textureRenderer.loadQuad(-1, -1, 1, 1, z, 0, 1, 1, 0);
		textureRenderer.draw(GL30.GL_QUADS, matrix);
		frameBuffer.clear();
	}
	
	public ColorTexture getTexture()
	{
		return frameBuffer.getColorTexture();
	}
	
	@Override
	protected FloatArrayBuffer getArrayBuffer()
	{
		return (FloatArrayBuffer) super.getArrayBuffer();
	}
	
	public void initializeFrameBuffer(int width, int height)
	{
		frameBuffer.initializeDepthTextureBuffer(width, height);
		frameBuffer.clear();
		textureRenderer.loadTexture(frameBuffer.getColorTexture());
		FrameBuffer.unbind();
	}
}
