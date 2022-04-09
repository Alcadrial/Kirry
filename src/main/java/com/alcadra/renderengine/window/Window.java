package com.alcadra.renderengine.window;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorEnterCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.CallbackI;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.renderengine.buffer.FrameBuffer;
import com.alcadra.renderengine.renderer.ColorRenderer;
import com.alcadra.renderengine.renderer.TextureRenderer;
import com.alcadra.renderengine.textures.Texture;
import com.alcadra.renderengine.util.Image;
import com.alcadra.renderengine.window.widget.ClickableWidget;
import com.alcadra.renderengine.window.widget.RenderizableWidget;
import com.alcadra.renderengine.window.widget.Widget;

public class Window {
	
	private long windowId;
	private boolean isBuild = false;
	private String title;
	private Vector2i mousePos = Vector2i.getOrigin();
	private Vector2i size;
	private float aspectRatio;
	private CursorInputMode cursorInputMode = CursorInputMode.NORMAL;
	
	private List<Widget> widgets = new LinkedList<>();
	private List<RenderizableWidget> renderizableWidgets = new LinkedList<>();
	private List<Widget> renderizableWidgets0 = new LinkedList<>();
	private List<ClickableWidget> clickableWidgets = new LinkedList<>();
	private List<Widget> clickableWidgets0 = new LinkedList<>();
	
	private List<GLFWCursorPosCallbackI> cursorPosCallbackList = new ArrayList<>(1);
	private List<GLFWCursorEnterCallbackI> cursorEnterCallbackList = new ArrayList<>(1);
	private List<GLFWMouseButtonCallbackI> mouseButtonCallbackList = new ArrayList<>(1);
	private List<GLFWScrollCallbackI> scrollCallbackList = new ArrayList<>(1);
	private List<GLFWKeyCallbackI> keyCallbackList = new ArrayList<>(1);
	private List<GLFWWindowSizeCallbackI> windowSizeCallbackList = new ArrayList<>(1);
	private GLFWCursorPosCallbackI cursorPosCallback = (w, x, y) -> {
		mousePos = new Vector2i((int) x, getHeight() - (int) y);
		for (GLFWCursorPosCallbackI callback : cursorPosCallbackList) callback.invoke(w, x, y);
	};
	private GLFWCursorEnterCallbackI cursorEnterCallback = (w, entered) -> { for (GLFWCursorEnterCallbackI callback : cursorEnterCallbackList) callback.invoke(w, entered); };
	private GLFWMouseButtonCallbackI mouseButtonCallback = (w, button, action, mode) -> {
		for (GLFWMouseButtonCallbackI callback : mouseButtonCallbackList) callback.invoke(w, button, action, mode);
		if (action == GLFW.GLFW_PRESS)
		{
			for (ClickableWidget clickableWidget : clickableWidgets)
			{
				Widget widget = (Widget) clickableWidget;
				if (widget.isMouseHovered()) clickableWidget.onClick(button, mousePos);
			}
		}
	};
	private GLFWKeyCallbackI keyCallback = (w, key, scancode, action, mode) -> { for (GLFWKeyCallbackI callback : keyCallbackList) callback.invoke(w, key, scancode, action, mode); };
	private GLFWWindowSizeCallbackI windowSizeCallback = (w, x, y) -> { for (GLFWWindowSizeCallbackI callback : windowSizeCallbackList) callback.invoke(w, x, y); };
	private GLFWScrollCallbackI windowScrollCallback = (w, xOffset, yOffset) -> { for (GLFWScrollCallbackI callback : scrollCallbackList) callback.invoke(w, xOffset, yOffset); };
	
	public Window(int w, int h, String title)
	{
		this.title = title;
		size = new Vector2i(w, h);
		aspectRatio = (float) size.x() / size.y();
	}
	
	public void build()
	{
		if (!isBuild)
		{
			windowId = GLFW.glfwCreateWindow(getWidth(), getHeight(), title, 0, 0);
			if (isBuild = windowId != 0)
			{
				GLFW.glfwMakeContextCurrent(windowId);
				GLFW.glfwSwapInterval(0);
				GLFW.glfwSetCursorPosCallback(windowId, cursorPosCallback);
				GLFW.glfwSetCursorEnterCallback(windowId, cursorEnterCallback);
				GLFW.glfwSetMouseButtonCallback(windowId, mouseButtonCallback);
				GLFW.glfwSetScrollCallback(windowId, windowScrollCallback);
				GLFW.glfwSetKeyCallback(windowId, keyCallback);
				GLFW.glfwSetWindowSizeCallback(windowId, windowSizeCallback);
			}
		}
	}
	
	public final boolean shouldClose()
	{
		return GLFW.glfwWindowShouldClose(windowId);
	}
	
	public final void setIcon(Texture texture)
	{
		Buffer b = GLFWImage.malloc(1).put(0, GLFWImage.malloc().set(texture.getWidth(), texture.getHeight(), texture.getImage().toByteBuffer()));
		GLFW.glfwSetWindowIcon(windowId, b);
		b.free();
	}
	
	public final void setIcon(Image image)
	{
		Buffer b = GLFWImage.malloc(1).put(0, GLFWImage.malloc().set(image.getBuffered().getWidth(), image.getBuffered().getHeight(), image.toByteBuffer()));
		GLFW.glfwSetWindowIcon(windowId, b);
		b.free();
	}
	
	public final void show()
	{
		GLFW.glfwShowWindow(windowId);
		onResize(getSize(), getSize());
	}
	
	public final void pollEvents()
	{
		GLFW.glfwPollEvents();
	}
	
	public final void swapBuffers()
	{
		GLFW.glfwSwapBuffers(windowId);
	}
	
	public void update(Matrix4f matrix, ColorRenderer colorRenderer, TextureRenderer textureRenderer, float partialTicks)
	{
		Iterator<RenderizableWidget> i = renderizableWidgets.iterator();
		Iterator<Widget> j = renderizableWidgets0.iterator();
		while (i.hasNext())
		{
			colorRenderer.clear();
			i.next().render(new Matrix4f(matrix), colorRenderer, textureRenderer, j.next().getPos().xy(), partialTicks);
		}
		FrameBuffer.unbind();
		pollEvents();
		swapBuffers();
		GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
	}
	
	public final void addEvent(CallbackI event)
	{
		if (event instanceof GLFWCursorPosCallbackI callback) cursorPosCallbackList.add(callback);
		if (event instanceof GLFWCursorEnterCallbackI callback) cursorEnterCallbackList.add(callback);
		if (event instanceof GLFWMouseButtonCallbackI callback) mouseButtonCallbackList.add(callback);
		if (event instanceof GLFWScrollCallbackI callback) scrollCallbackList.add(callback);
		if (event instanceof GLFWKeyCallbackI callback) keyCallbackList.add(callback);
		if (event instanceof GLFWWindowSizeCallbackI callback) windowSizeCallbackList.add(callback);
	}
	
	public final void addWidget(Widget widget)
	{
		widgets.add(widget);
		if (widget instanceof RenderizableWidget w)
		{
			renderizableWidgets.add(w);
			renderizableWidgets0.add(widget);
		}
		if (widget instanceof ClickableWidget w)
		{
			clickableWidgets.add(w);
			clickableWidgets0.add(widget);
		}
	}
	
	protected void onResize(Vector2i oldSize, Vector2i newSize)
	{
		aspectRatio = (float) newSize.x() / newSize.y();
		if (isBuild) FrameBuffer.getDefault().setDrawArea(Vector2i.getOrigin(), newSize);
	}
	
	public final long getId()
	{
		return windowId;
	}
	
	public final boolean isBuild()
	{
		return isBuild;
	}
	
	public final String getTitle()
	{
		return title;
	}
	
	public Vector2i getMousePos()
	{
		return mousePos;
	}
	
	public final Vector2i getSize()
	{
		return size;
	}
	
	public final int getWidth()
	{
		return size.x();
	}
	
	public final int getHeight()
	{
		return size.y();
	}
	
	public final float getAspectRatio()
	{
		return aspectRatio;
	}
	
	public final CursorInputMode getCursorInputMode()
	{
		return cursorInputMode;
	}
	
	public final void setMousePos(Vector2i mousePos)
	{
		if (!this.mousePos.equals(mousePos))
		{
			this.mousePos = mousePos;
			GLFW.glfwSetCursorPos(windowId, mousePos.x(), mousePos.y());
		}
	}
	
	public final void setSize(Vector2i size)
	{
		onResize(this.size, size);
		this.size = size;
	}
	
	public final void setWidth(int width)
	{
		onResize(size, new Vector2i(width, getHeight()));
		setSize(new Vector2i(width, getHeight()));
	}
	
	public final void setHeight(int height)
	{
		onResize(size, new Vector2i(getWidth(), height));
		setSize(new Vector2i(getWidth(), height));
	}
	
	public final void setCursorInputMode(CursorInputMode mode)
	{
		if (cursorInputMode != mode)
		{
			cursorInputMode = mode;
			GLFW.glfwSetInputMode(windowId, GLFW.GLFW_CURSOR, mode.ordinal() + GLFW.GLFW_CURSOR_NORMAL);
		}
	}
}
