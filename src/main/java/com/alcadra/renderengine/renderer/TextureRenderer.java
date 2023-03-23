package com.alcadra.renderengine.renderer;

import java.awt.IllegalComponentStateException;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.complex.Quaternion;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.util.Image;
import com.alcadra.renderengine.util.update.NoReplacement;
import com.alcadra.renderengine.util.update.UseInstead;

@Deprecated(since = "2.0-migration", forRemoval = true)
@UseInstead(name = "UniversalRenderer")
@NoReplacement
public class TextureRenderer extends Renderer {
	
	private ColorTexture texture;
	
	public TextureRenderer(Shader shader)
	{
		super(shader);
	}
	
	@Override
	protected void initializeArrayBuffer()
	{
		if (!(shader.useUV() || shader.hasUniformUV())) throw new IllegalComponentStateException("Shader cannot use a texture for render in a TextureRenderer!");
		super.initializeArrayBuffer();
	}
	
	public void loadImage(Image image)
	{
		texture = new ColorTexture(image);
	}
	
	public void loadTexture(ColorTexture texture)
	{
		this.texture = texture;
	}
	
	@Override
	public Renderer vertex(float x, float y, float z)
	{
		super.vertex(x, y, z);
		return this;
	}
	
	public Renderer loadQuad(float startX, float startY, float endX, float endY, float z, float minU, float minV, float maxU, float maxV)
	{
		uv(minU, minV).loadPoint(startX, endY, z);
		uv(maxU, minV).loadPoint(endX, endY, z);
		uv(maxU, maxV).loadPoint(endX, startY, z);
		return uv(minU, maxV).loadPoint(startX, startY, z);
	}
	
	public Renderer loadQuad(Vector3f start, Vector3f end, Vector3f normal, Vector2f minUV, Vector2f maxUV)
	{
		Vector3f size = end.subtract(start).multiply(0.5F);
		Vector3f h = start.add(new Quaternion(normal, 90).rotate(size).yzw());
		Vector3f w = end.subtract(h).add(start);
		
		uv(minUV).loadPoint(h.x(), h.y(), h.z());
		uv(maxUV.x(), minUV.y()).loadPoint(end.x(), end.y(), end.z());
		uv(maxUV).loadPoint(w.x(), w.y(), w.z());
		return uv(minUV.x(), maxUV.y()).loadPoint(start.x(), start.y(), start.z());
	}
	
	@Override
	public void drawVertices(int mode, Matrix4f matrix)
	{
		texture.bind();
		super.drawVertices(mode, matrix);
	}
	
	@Deprecated(since = "1.1", forRemoval = true)
	@Override
	public void draw(int mode, Matrix4f matrix)
	{
		texture.bind();
		super.draw(mode, matrix);
	}
	
	@Override
	public void drawPoints(int mode, Matrix4f matrix)
	{
		texture.bind();
		super.drawPoints(mode, matrix);
	}
}
