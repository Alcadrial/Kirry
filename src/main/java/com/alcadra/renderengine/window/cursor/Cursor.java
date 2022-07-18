package com.alcadra.renderengine.window.cursor;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import com.alcadra.renderengine.util.Image;

public class Cursor implements ICursor {
	
	private long id;
	private GLFWImage image;
	
	public Cursor(Image image)
	{
		this.image = new GLFWImage(image.toByteBuffer());
		id = GLFW.glfwCreateCursor(this.image, 0, 0);
		if (id == 0) throw new CursorException();
	}
	
	@Override
	public void destroy()
	{
		GLFW.glfwDestroyCursor(id);
	}
	
	@Override
	public long getId()
	{
		return id;
	}
}
