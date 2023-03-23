package com.alcadra.renderengine.window.cursor;

import org.lwjgl.glfw.GLFW;

public enum StandardCursor implements ICursor {
	
	ARROW(GLFW.GLFW_ARROW_CURSOR),
	IBEAM(GLFW.GLFW_IBEAM_CURSOR),
	CROSSHAIR(GLFW.GLFW_CROSSHAIR_CURSOR),
	POINTING_HAND(GLFW.GLFW_POINTING_HAND_CURSOR),
	RESIZE_EW(GLFW.GLFW_RESIZE_EW_CURSOR),
	RESIZE_NS(GLFW.GLFW_RESIZE_NS_CURSOR),
	RESIZE_NWSE(GLFW.GLFW_RESIZE_NWSE_CURSOR),
	RESIZE_NESW(GLFW.GLFW_RESIZE_NESW_CURSOR),
	RESIZE_ALL(GLFW.GLFW_RESIZE_ALL_CURSOR),
	NOT_ALLOWED(GLFW.GLFW_NOT_ALLOWED_CURSOR);
	
	private long id;
	
	private StandardCursor(int shape)
	{
		id = GLFW.glfwCreateStandardCursor(shape);
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
