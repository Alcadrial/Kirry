package com.alcadrial.kirry.core.cursor;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import com.alcadrial.kirry.core.util.Image;

public class Cursor implements ICursor {
	
	private long id;
	
	public Cursor(Image image)
	{
		this(image, 0, 0);
	}
	
	public Cursor(Image image, int xOffset, int yOffset)
	{
		GLFWImage image0 = GLFWImage.malloc().set(image.getBuffered().getWidth(), image.getBuffered().getHeight(), image.toByteBuffer());
		id = GLFW.glfwCreateCursor(image0, xOffset, yOffset);
		image0.free();
		if (id == 0) throw new CursorException();
	}
	
	@Override
	public void destroy()
	{
		if (id != 0)
		{
			GLFW.glfwDestroyCursor(id);
			id = 0;
		}
	}
	
	@Override
	public void close()
	{
		destroy();
	}
	
	@Override
	public long getId()
	{
		return id;
	}
}
