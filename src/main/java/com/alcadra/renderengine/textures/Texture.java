package com.alcadra.renderengine.textures;

import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.opengl.GL45;

import com.alcadra.renderengine.framebuffer.FrameBuffer;
import com.alcadra.renderengine.util.Broken;
import com.alcadra.renderengine.util.Deletable;
import com.alcadra.renderengine.util.Optimizable;
import com.alcadra.renderengine.util.Util;

public final class Texture implements Deletable {
	
	private static final FrameBuffer FRAME_BUFFER = new FrameBuffer();
	private static final int[] POINTER = new int[1];
	
	/**
	 * Selects which texture unit subsequent texture state calls will affect. 
	 * The number of texture units an implementation supports is implementation dependent in range [0; 31].
	 *
	 * @param unit the unit to activate
	 */
	public static void setActive(int unit)
	{
		GL45.glActiveTexture(GL45.GL_TEXTURE0 + unit);
	}
	
	private final TextureTemplate template;
	private final int id;
	private int length;
	private int width;
	private int height;
	private int depth;
	private int mipmapLevels;
	
	/**
	 * Creates a texture using the given {@link TextureTemplate}.
	 * 
	 * @param template the {@link TextureTemplate} to use
	 */
	public Texture(TextureTemplate template)
	{
		this.template = template;
		id = GL45.glGenTextures();
		if (id == 0) throw new TextureGenerationException();
	}
	
	/**
	 * Binds the a texture to a texture target.
	 * <p>
	 * While a texture object is bound, GL operations on the target to which it is bound affect the bound object, 
	 * and queries of the target to which it is bound return state from the bound object.
	 * <p>
	 * Methods in this class doesn't require binding.
	 */
	public void bind()
	{
		GL45.glBindTexture(template.target(), id);
	}
	
	/**
	 * Generate a new 1D texture of length <code>width</code> using mipmap if <code>mipmapLevels</code> is greater than 0, and, 
	 * if data isn't null, with <code>width</code> ints from data.
	 * 
	 * @param dataFormat the charateristics of the given data
	 * @param data used to initialize the texture if not null
	 * @param width the width of the texture
	 * @param mipmapLevels the level-of-detail of the texture or 0
	 */
	public void setData1D(TextureTemplate dataFormat, int[] data, int width, int mipmapLevels)
	{
		if (mipmapLevels < 0) return;
		bind();
		GL45.glTexImage1D(template.target(), mipmapLevels, template.internalFormat(), width, 0, dataFormat.textureFormat(), dataFormat.dataType(), data);
		length = width;
		this.width = width;
		height = 1;
		depth = 1;
		this.mipmapLevels = mipmapLevels;
	}
	
	/**
	 * Generate a new 2D texture of length <code>width * height</code> using mipmap if <code>mipmapLevels</code> is greater than 0, and, 
	 * if data isn't null, with <code>width * height</code> ints from data.
	 * 
	 * @param dataFormat the charateristics of the given data
	 * @param data used to initialize the texture if not null
	 * @param width the width of the texture
	 * @param height the height of the texture
	 * @param mipmapLevels the level-of-detail of the texture or 0
	 */
	public void setData2D(TextureTemplate dataFormat, int[] data, int width, int height, int mipmapLevels)
	{
		if (mipmapLevels < 0) return;
		// System.out.println("internal " + id + " " + width + " " + height);
		bind();
		GL45.glTexImage2D(template.target(), mipmapLevels, template.internalFormat(), width, height, 0, dataFormat.textureFormat(), dataFormat.dataType(), data);
		Util.printError("Texture [INTERNAL] setData");
		length = width * height;
		this.width = width;
		this.height = height;
		depth = 1;
		this.mipmapLevels = mipmapLevels;
	}
	
	/**
	 * Generate a new 3D texture of length <code>width * height * depth</code> using mipmap if <code>mipmapLevels</code> is greater than 0, and, 
	 * if data isn't null, with <code>width * height * depth</code> ints from data.
	 * 
	 * @param dataFormat the charateristics of the given data
	 * @param data used to initialize the texture if not null
	 * @param width the width of the texture
	 * @param height the height of the texture
	 * @param depth the depth of the texture
	 * @param mipmapLevels the level-of-detail of the texture or 0
	 */
	public void setData3D(TextureTemplate dataFormat, int[] data, int width, int height, int depth, int mipmapLevels)
	{
		if (mipmapLevels < 0) return;
		bind();
		GL45.glTexImage3D(template.target(), mipmapLevels, template.internalFormat(), width, height, depth, 0, dataFormat.textureFormat(), dataFormat.dataType(), data);
		length = width * height * depth;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.mipmapLevels = mipmapLevels;
	}
	
	/**
	 * Sets the values of the texture from <code>x</code> to <code>x + width</code> (excluded) to the specified value.
	 * 
	 * @param value the value to set
	 * @param x
	 * @param width
	 */
	public void clearRegion1D(int value, int x, int width)
	{
		clearRegion3D(value, x, 0, 0, width, 1, 1);
	}
	
	/**
	 * Sets the values of the texture from <code>(x; y)</code> to <code>(x + width; y + height)</code> (excluded) to the specified value.
	 * 
	 * @param value the value to set
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void clearRegion2D(int value, int x, int y, int width, int height)
	{
		clearRegion3D(value, x, y, 0, width, height, 1);
	}
	
	/**
	 * Sets the values of the texture from <code>(x; y; z)</code> to <code>(x + width; y + height; z + depth)</code> (excluded) to the specified value.
	 * 
	 * @param value the value to set
	 * @param x
	 * @param y
	 * @param z
	 * @param width
	 * @param height
	 * @param depth
	 */
	public void clearRegion3D(int value, int x, int y, int z, int width, int height, int depth)
	{
		if (value == 0) GL45.glClearTexSubImage(id, mipmapLevels, x, y, z, width, height, depth, template.textureFormat(), template.dataType(), (int[]) null);
		else GL45.glClearTexSubImage(id, mipmapLevels, x, y, z, width, height, depth, template.textureFormat(), template.dataType(), point(value));
	}
	
	/**
	 * Resize this texture to the specified dimensions.
	 * <p>
	 * If this texture is bigger than <code>width</code>, the resulting texture will have less data than the original.
	 * 
	 * @param width
	 */
	@Optimizable
	public void resize1D(int width)
	{
		resize1D(width, mipmapLevels);
	}
	
	/**
	 * Resize this texture to the specified dimensions.
	 * <p>
	 * If this texture is bigger than <code>width</code>, the resulting texture will have less data than the original.
	 * 
	 * @param width
	 * @param mipmapLevels the level-of-detail of the texture or 0
	 */
	@Optimizable
	public void resize1D(int width, int mipmapLevels)
	{
		setData1D(template, getDataArray(), width, mipmapLevels);
	}
	
	/**
	 * Resize this texture to the specified dimensions.
	 * <p>
	 * If this texture has any dimension bigger than the given one, the resulting texture will have less data than the original.
	 * 
	 * @param width
	 * @param height
	 */
	public void resize2D(int width, int height)
	{
		resize2D(width, height, mipmapLevels);
	}
	
	/**
	 * Resize this texture to the specified dimensions.
	 * <p>
	 * If this texture has any dimension bigger than the given one, the resulting texture will have less data than the original.
	 * 
	 * @param width
	 * @param height
	 * @param mipmapLevels the level-of-detail of the texture or 0
	 */
	public void resize2D(int width, int height, int mipmapLevels)
	{
		int[] pixels = getDataArray();
		int[] newPixels = new int[width * height];
		for (int i = 0; i < this.height; i++) for (int j = 0; j < this.width; j++) newPixels[i * width + j] = pixels[i * this.width + j];
		setData2D(template, newPixels, width, height, mipmapLevels);
	}
	
	/**
	 * Resize this texture to the specified dimensions.
	 * <p>
	 * If this texture has any dimension bigger than the given one, the resulting texture will have less data than the original.
	 * 
	 * @param width
	 * @param height
	 * @param depth
	 */
	public void resize3D(int width, int height, int depth)
	{
		resize3D(width, height, depth, mipmapLevels);
	}
	
	/**
	 * Resize this texture to the specified dimensions.
	 * <p>
	 * If this texture has any dimension bigger than the given one, the resulting texture will have less data than the original.
	 * 
	 * @param width
	 * @param height
	 * @param depth
	 * @param mipmapLevels the level-of-detail of the texture or 0
	 */
	public void resize3D(int width, int height, int depth, int mipmapLevels)
	{
		int length2D = width * height;
		int length2D0 = this.width * this.height;
		int[] pixels = getDataArray();
		int[] newPixels = new int[length2D * depth];
		for (int i = 0; i < this.depth; i++) for (int j = 0; j < this.height; j++) for (int k = 0; k < this.width; k++) newPixels[i * length2D + j * width + k] = pixels[i * length2D0 + j * this.width + k];
		setData3D(template, newPixels, width, height, depth, mipmapLevels);
	}
	
	/**
	 * Copy this texture to the specified one.
	 * <p>
	 * If this texture is bigger in any dimension than the specified one, than will be copied the data that fits in that texture.
	 * 
	 * @param texture the destination of the copy
	 */
	public void copyTo1D(Texture texture)
	{
		FRAME_BUFFER.offer();
		FRAME_BUFFER.setTexture(this);
		GL45.glCopyTextureSubImage1D(texture.id, 0, 0, 0, 0, width);
		FRAME_BUFFER.poll();
	}
	
	/**
	 * Copy this texture from <code>sourceX</code> to <code>sourceX + width</code> (excluded) to the specified one
	 * from <code>destinationX</code> to <code>destinationX + width</code>.
	 * <p>
	 * If this texture is bigger in any dimension than the specified one, than will be copied the data that fits in that texture.
	 * 
	 * @param texture the destination of the copy
	 * @param sourceX
	 * @param destinationX
	 * @param width
	 */
	public void copyTo1D(Texture texture, int sourceX, int destinationX, int width)
	{
		FRAME_BUFFER.offer();
		FRAME_BUFFER.setTexture(this);
		GL45.glCopyTextureSubImage1D(texture.id, 0, destinationX, sourceX, 0, width);
		FRAME_BUFFER.poll();
	}
	
	/**
	 * Copy this texture to the specified one.
	 * <p>
	 * If this texture is bigger in any dimension than the specified one, than will be copied the data that fits in that texture.
	 * 
	 * @param texture the destination of the copy
	 */
	public void copyTo2D(Texture texture)
	{
		FRAME_BUFFER.offer();
		FRAME_BUFFER.setTexture(this);
		GL45.glCopyTextureSubImage2D(texture.id, 0, 0, 0, 0, 0, width, height);
		FRAME_BUFFER.poll();
	}
	
	/**
	 * Copy this texture from <code>(sourceX; sourceY)</code> to <code>(sourceX + width; sourceY + height)</code> (excluded) to the specified one
	 * from <code>(destinationX; destinationY)</code> to <code>(destinationX + width; destinationY + height)</code>.
	 * <p>
	 * If this texture is bigger in any dimension than the specified one, than will be copied the data that fits in that texture.
	 * 
	 * @param texture the destination of the copy
	 * @param sourceX
	 * @param sourceY
	 * @param destinationX
	 * @param destinationY
	 * @param width
	 * @param height
	 */
	public void copyTo2D(Texture texture, int sourceX, int sourceY, int destinationX, int destinationY, int width, int height)
	{
		FRAME_BUFFER.offer();
		FRAME_BUFFER.setTexture(this);
		GL45.glCopyTextureSubImage2D(texture.id, 0, destinationX, destinationY, sourceX, sourceY, width, height);
		FRAME_BUFFER.poll();
	}
	
	/**
	 * Copy this texture to the specified one.
	 * <p>
	 * If this texture is bigger in any dimension than the specified one, than will be copied the data that fits in that texture.
	 * 
	 * @param texture the destination of the copy
	 */
	@Broken
	public void copyTo3D(Texture texture)
	{
		FRAME_BUFFER.offer();
		FRAME_BUFFER.setTexture(this);
		for (int i = 0; i < depth; i++) GL45.glCopyTextureSubImage3D(texture.id, 0, 0, 0, i, 0, 0, width, height);
		FRAME_BUFFER.poll();
	}
	
	/**
	 * Copy this texture from <code>(sourceX; sourceY; sourceZ)</code> to <code>(sourceX + width; sourceY + height; sourceZ + depth)</code> (excluded) to the specified one
	 * from <code>(destinationX; destinationY; destinationZ)</code> to <code>(destinationX + width; destinationY + height; destinationZ + depth)</code>.
	 * <p>
	 * If this texture is bigger in any dimension than the specified one, than will be copied the data that fits in that texture.
	 * 
	 * @param texture the destination of the copy
	 * @param sourceX
	 * @param sourceY
	 * @param sourceZ ({@link Broken}: no effect, always used 0)
	 * @param destinationX
	 * @param destinationY
	 * @param destinationZ
	 * @param width
	 * @param height
	 * @param depth ({@link Broken}: sourceZ will be used for each layer)
	 */
	public void copyTo3D(Texture texture, int sourceX, int sourceY, @Broken int sourceZ, int destinationX, int destinationY, int destinationZ, int width, int height, @Broken int depth)
	{
		FRAME_BUFFER.offer();
		FRAME_BUFFER.setTexture(this);
		// int sourceZEnd = sourceZ + depth;
		int destinationZEnd = destinationZ + depth;
		while (destinationZ < destinationZEnd) GL45.glCopyTextureSubImage3D(texture.id, 0, destinationX, destinationY, destinationZ++, sourceX, sourceY, width, height);
		FRAME_BUFFER.poll();
	}
	
	public void enableAntiAlias()
	{
		GL45.glTextureParameteri(id, GL45.GL_TEXTURE_MIN_FILTER, GL45.GL_LINEAR);
		GL45.glTextureParameteri(id, GL45.GL_TEXTURE_MAG_FILTER, GL45.GL_LINEAR);
	}
	
	public void disableAntiAlias()
	{
		GL45.glTextureParameteri(id, GL45.GL_TEXTURE_MIN_FILTER, GL45.GL_NEAREST);
		GL45.glTextureParameteri(id, GL45.GL_TEXTURE_MAG_FILTER, GL45.GL_NEAREST);
	}
	
	/**
	 * Returns an array containing all the pixels of this texture.
	 * @return this texture as array
	 */
	public int[] getDataArray()
	{
		return getDataArray(template);
	}
	
	/**
	 * Returns an array containing all the pixels of this texture using the specified {@link TextureTemplate}.
	 * @param template
	 * @return this texture as array
	 */
	public int[] getDataArray(TextureTemplate template)
	{
		int glLength = getGlLength();
		int[] pixels = glLength == 1 ? POINTER : new int[glLength];
		GL45.glGetTextureImage(id, mipmapLevels, template.textureFormat(), template.dataType(), pixels);
		return glLength == 1 ? Arrays.copyOf(pixels, glLength) : pixels;
	}
	
	public int getGlLength()
	{
		return getGlWidth() * getGlHeight() * getGlDepth();
	}
	
	public int getGlWidth()
	{
		return GL45.glGetTextureLevelParameteri(id, mipmapLevels, GL45.GL_TEXTURE_WIDTH);
	}
	
	public int getGlHeight()
	{
		return GL45.glGetTextureLevelParameteri(id, mipmapLevels, GL45.GL_TEXTURE_HEIGHT);
	}
	
	public int getGlDepth()
	{
		return GL45.glGetTextureLevelParameteri(id, mipmapLevels, GL45.GL_TEXTURE_DEPTH);
	}
	
	public IntBuffer getDataBuffer()
	{
		return IntBuffer.wrap(getDataArray());
	}
	
	public TextureTemplate getTemplate()
	{
		return template;
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getLength()
	{
		return length;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getDepth()
	{
		return depth;
	}
	
	public int getMipmapLevels()
	{
		return mipmapLevels;
	}
	
	private boolean valid = true;
	
	@Override
	public void close()
	{
		GL45.glDeleteTextures(id);
		if (valid) valid = false;
		else if (Util.isDebug()) System.err.println("Recleaning");
		if (Util.isDebug()) System.out.println("Clean");
	}
	
	private int[] point(int value)
	{
		POINTER[0] = value;
		return POINTER;
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Texture t && equals(t);
	}
	
	public boolean equals(Texture texture)
	{
		return id == texture.id;
	}
	
	@Override
	public String toString()
	{
		if (depth == 1)
		{
			if (height == 1) return "Texture{id=" + id + ", size=" + width + "}";
			return "Texture{id=" + id + ", size=" + width + "x" + height + "}";
		}
		return "Texture{id=" + id + ", size=" + width + "x" + height + "x" + depth + "}";
	}
}
