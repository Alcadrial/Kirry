package com.alcadrial.renderengine.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.lwjgl.BufferUtils;

import com.alcadrial.kector.vector.Vector2i;

public class Image {
	
	private BufferedImage image;
	private Vector2i size;
	
	public Image(File file) throws IOException
	{
		this(ImageIO.read(file));
	}
	
	public Image(ImageInputStream stream) throws IOException
	{
		this(ImageIO.read(stream));
	}
	
	public Image(InputStream stream) throws IOException
	{
		this(ImageIO.read(stream));
	}
	
	public Image(URL url) throws IOException
	{
		this(ImageIO.read(url));
	}
	
	private Image(BufferedImage image) throws IOException
	{
		this.image = image;
		size = Vector2i.of(image.getWidth(), image.getHeight());
	}
	
	public Image(int[] pixels, int width, int height)
	{
		image = new BufferedImage(width, height, 2);
		for (int i = 0; i < pixels.length; i++) pixels[i] = fromRGBAtoARGB(pixels[i]);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		size = Vector2i.of(width, height);
	}
	
	public ByteBuffer toByteBuffer()
	{
		int[] data = image.getRGB(0, 0, size.x(), size.y(), null, 0, size.x());
		ByteBuffer buffer = BufferUtils.createByteBuffer(data.length * 4);
		buffer.asIntBuffer().put(data).flip();
		return buffer;
	}
	
	public int[] loadToARGBBuffer()
	{
		return image.getRGB(0, 0, size.x(), size.y(), null, 0, size.x());
	}
	
	public int[] loadToRGBABuffer()
	{
		int[] data = image.getRGB(0, 0, size.x(), size.y(), null, 0, size.x());
		int[] buffer = new int[data.length];
		for (int i = 0; i < buffer.length; i++) buffer[i] = fromARGBtoRGBA(data[i]);
		return buffer;
	}
	
	public List<Integer> loadToListARGB()
	{
		List<Integer> buffer = new ArrayList<>();
		for (int y = 0; y < size.y(); y++) for (int x = 0; x < size.x(); x++) buffer.add(image.getRGB(x, y));
		return buffer;
	}
	
	public List<Integer> loadToListRGBA()
	{
		List<Integer> buffer = new ArrayList<>();
		for (int y = 0; y < size.y(); y++) for (int x = 0; x < size.x(); x++) buffer.add(fromARGBtoRGBA(image.getRGB(x, y)));
		return buffer;
	}
	
	private int fromARGBtoRGBA(int color)
	{
		return color << 8 | color >>> 24;
	}
	
	private int fromRGBAtoARGB(int color)
	{
		return color << 24 | color >>> 8;
	}
	
	public BufferedImage getBuffered()
	{
		return image;
	}
	
	public Vector2i getSize()
	{
		return size;
	}
}
