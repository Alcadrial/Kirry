package com.alcadrial.kirry.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kirry.core.cursor.CursorInputMode;
import com.alcadrial.kirry.core.cursor.ICursor;
import com.alcadrial.kirry.core.util.Image;

public class Window {
	
	protected long id;
	protected CursorInputMode cursorInputMode;
	protected Vector2i mousePos;
	
	public Window()
	{
		cursorInputMode = CursorInputMode.NORMAL;
		mousePos = Vector2i.origin();
	}
	
	public boolean shouldClose()
	{
		return GLFW.glfwWindowShouldClose(id);
	}
	
	public final void show()
	{
		GLFW.glfwShowWindow(id);
		onShow();
	}
	
	protected void onShow()
	{
		
	}
	
	public final void hide()
	{
		GLFW.glfwHideWindow(id);
		onHide();
	}
	
	protected void onHide()
	{
		
	}
	
	public final void iconify()
	{
		GLFW.glfwIconifyWindow(id);
		onIconify();
	}
	
	protected void onIconify()
	{
		
	}
	
	public final void maximize()
	{
		GLFW.glfwMaximizeWindow(id);
		onMaximize();
	}
	
	protected void onMaximize()
	{
		
	}
	
	public final void restore()
	{
		GLFW.glfwRestoreWindow(id);
		onRestore();
	}
	
	protected void onRestore()
	{
		
	}
	
	public final void focus()
	{
		GLFW.glfwFocusWindow(id);
		//onFocus();
	}
	
	protected void onFocus()
	{
		
	}
	
	public final void requestAttention()
	{
		GLFW.glfwRequestWindowAttention(id);
	}
	
	public void pollEvents()
	{
		GLFW.glfwPollEvents();
	}
	
	public final void setDefaultCursor()
	{
		GLFW.glfwSetCursor(id, 0);
	}
	
	public final void setCursor(ICursor cursor)
	{
		GLFW.glfwSetCursor(id, cursor.getId());
	}
	
	public final void setCursorInputMode(CursorInputMode mode)
	{
		if (cursorInputMode != mode)
		{
			cursorInputMode = mode;
			GLFW.glfwSetInputMode(id, GLFW.GLFW_CURSOR, mode.ordinal() + GLFW.GLFW_CURSOR_NORMAL);
		}
	}
	
	public void setOpacity(float alpha)
	{
		GLFW.glfwSetWindowOpacity(id, alpha / 255);
	}
	
	public final void setMinMaxSizes(Vector2i minSize, Vector2i maxSize)
	{
		GLFW.glfwSetWindowSizeLimits(id, minSize.x(), minSize.y(), maxSize.x(), maxSize.y());
	}
	
	public final void setMinMaxSizes(int minWidth, int minHeight, int maxWidth, int maxHeight)
	{
		GLFW.glfwSetWindowSizeLimits(id, minWidth, minHeight, maxWidth, maxHeight);
	}
	
	public final void setLockKeyModsInputMode(boolean value)
	{
		GLFW.glfwSetInputMode(id, GLFW.GLFW_LOCK_KEY_MODS, value ? 1 : 0);
	}
	
	public final void setMousePos(Vector2i mousePos)
	{
		if (!this.mousePos.equals(mousePos))
		{
			this.mousePos = mousePos;
			GLFW.glfwSetCursorPos(id, mousePos.x(), mousePos.y());
		}
	}
	
	public final void setIcon(Image image)
	{
		Buffer b = GLFWImage.malloc(1).put(0, GLFWImage.malloc().set(image.getBuffered().getWidth(), image.getBuffered().getHeight(), image.toByteBuffer()));
		GLFW.glfwSetWindowIcon(id, b);
		b.get().free();
	}
}
