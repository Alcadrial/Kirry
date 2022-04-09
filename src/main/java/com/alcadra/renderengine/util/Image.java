package com.alcadra.renderengine.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

import com.alcadra.math.vector.Vector2f;

public class Image {
	
	private BufferedImage image;
	
	public Image(File file) throws IOException
	{
		image = ImageIO.read(file);
	}
	
	public Image(int[] pixels, int width, int height)
	{
		image = new BufferedImage(width, height, 2);
		for (int i = 0; i < pixels.length; i++) pixels[i] = fromRGBAtoARGB(pixels[i]);
		image.setRGB(0, 0, width, height, pixels, 0, width);
	}
	
	public ByteBuffer toByteBuffer()
	{
		int[] data = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(data.length * 4);
		for (int element : data) buffer.putInt(element);
		return buffer.flip();
	}
	
	public int[] loadToBuffer()
	{
		int[] buffer = new int[image.getWidth() * image.getHeight()];
		for (int y = 0, i = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) buffer[i++] = image.getRGB(x, y);
		return buffer;
	}
	
	public List<Integer> loadToListARGB()
	{
		List<Integer> buffer = new ArrayList<>();
		for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) buffer.add(image.getRGB(x, y));
		return buffer;
	}
	
	public List<Integer> loadToListRGBA()
	{
		List<Integer> buffer = new ArrayList<>();
		for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) buffer.add(fromARGBtoRGBA(image.getRGB(x, y)));
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
	
	public Vector2f getSize()
	{
		return new Vector2f(image.getWidth(), image.getHeight());
	}
}
