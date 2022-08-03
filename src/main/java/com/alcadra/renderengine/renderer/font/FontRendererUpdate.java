package com.alcadra.renderengine.renderer.font;

import org.lwjgl.opengl.GL30;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.renderer.BaseRendererUpdate;
import com.alcadra.renderengine.renderer.font.text.Text;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.textures.Texture;
import com.alcadra.renderengine.util.NotYetImplemented;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@NotYetImplemented
@UpdatedFrom("FontRenderer")
public class FontRendererUpdate extends BaseRendererUpdate<FloatArrayBuffer> {
	
	private Font font;
	
	public FontRendererUpdate(ShaderProgram program)
	{
		super(program, new FloatArrayBuffer());
		initializeArrayBuffer();
	}
	
	@Override
	protected void initializeArrayBuffer()
	{
		
	}
	
	@Override
	protected void disableAttributes()
	{
		
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
		
	}
	
	public void draw(Matrix4f matrix)
	{
		Texture.setActive(0);
		font.getTexture().bind();
		program.enable();
		draw(GL30.GL_QUADS);
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
