package com.alcadra.renderengine.textures;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL32;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.renderengine.util.Image;

public class Texture {
	
	public static void setActive(int x)
	{
		GL32.glActiveTexture(GL32.GL_TEXTURE0 + x);
	}
	
	private final int id, target, internalFormat, textureFormat, dataType;
	private Vector2i size;
	
	protected Texture(int width, int height, int target, int internalFormat, int textureFormat, int dataType, int[] pixels)
	{
		this(width, height, target, internalFormat, textureFormat, dataType);
		bind();
		disableAntiAlias();
		setData(pixels);
	}
	
	protected Texture(int width, int height, int target, int internalFormat, int textureFormat, int dataType, int[] pixels, int detailLevel)
	{
		this(width, height, target, internalFormat, textureFormat, dataType);
		bind();
		disableAntiAlias();
		setData(pixels);
	}
	
	protected Texture(int width, int height, int target, int internalFormat, int textureFormat, int dataType)
	{
		this(width, height, GL32.glGenTextures(), target, internalFormat, textureFormat, dataType);
	}
	
	protected Texture(int width, int height, int id, int target, int internalFormat, int textureFormat, int dataType)
	{
		size = new Vector2i(width, height);
		this.id = id;
		this.target = target;
		this.internalFormat = internalFormat;
		this.textureFormat = textureFormat;
		this.dataType = dataType;
	}
	
	public void bind()
	{
		GL32.glBindTexture(getTarget(), id);
	}
	
	public void enableAntiAlias()
	{
		GL32.glTexParameteri(target, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_LINEAR);
		GL32.glTexParameteri(target, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_LINEAR);
	}
	
	public void disableAntiAlias()
	{
		GL32.glTexParameteri(target, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
		GL32.glTexParameteri(target, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
	}
	
	public void resize(Vector2i size)
	{
		int[] pixels = getDataArray();
		int[] newPixels = new int[size.x() * size.y()];
		for (int i = 0; i < getHeight(); i++) for (int j = 0; j < getWidth(); j++) newPixels[i * size.x() + j] = pixels[i * getWidth() + j];
		this.size = size;
		setData(newPixels);
	}
	
	public int getImagePixel(Vector2i position)
	{
		int[] pixel = new int[1];
		GL32.glReadPixels(position.x(), position.y(), 1, 1, textureFormat, dataType, pixel);
		return pixel[0];
	}
	
	public Image getImage()
	{
		if (!nonNull()) return null;
		return new Image(getDataArrayARGB(), getWidth(), getHeight());
	}
	
	public int[] getDataArray()
	{
		if (!nonNull()) return new int[0];
		int[] pixels = new int[getLength()];
		bind();
		GL32.glGetTexImage(getTarget(), 0, textureFormat, dataType, pixels);
		return pixels;
	}
	
	public int[] getDataArrayARGB()
	{
		if (!nonNull()) return new int[0];
		int[] pixels = new int[getLength()];
		bind();
		GL32.glGetTexImage(getTarget(), 0, textureFormat, dataType, pixels);
		for (int i = 0; i < pixels.length; i++) pixels[i] = pixels[i] >>> 24 | pixels[i] << 8;
		return pixels;
	}
	
	public IntBuffer getDataBuffer()
	{
		return IntBuffer.wrap(getDataArray());
	}
	
	public void setData(int[] pixels)
	{
		GL32.glTexImage2D(target, 0, internalFormat, getWidth(), getHeight(), 0, textureFormat, dataType, pixels);
	}
	
	public void setData(int[] pixels, Vector2i size)
	{
		this.size = size;
		GL32.glTexImage2D(target, 0, internalFormat, getWidth(), getHeight(), 0, textureFormat, dataType, pixels);
	}
	
	public boolean nonNull()
	{
		return id != 0;
	}
	
	public Vector2i getSize()
	{
		return size;
	}
	
	public int getLength()
	{
		return getWidth() * getHeight();
	}
	
	public int getWidth()
	{
		return size.x();
	}
	
	public int getHeight()
	{
		return size.y();
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getTarget()
	{
		return target;
	}
	
	@Override
	public String toString()
	{
		return "Texture{id=" + id + "}";
	}
}
