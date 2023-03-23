package com.alcadra.renderengine.window;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWCursorEnterCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.CallbackI;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.renderengine.buffer.FrameBuffer;
import com.alcadra.renderengine.renderer.object.ObjectRenderer;
import com.alcadra.renderengine.renderer.universal.UniversalRenderer;
import com.alcadra.renderengine.textures.Texture;
import com.alcadra.renderengine.util.Image;
import com.alcadra.renderengine.window.cursor.CursorInputMode;
import com.alcadra.renderengine.window.cursor.ICursor;
import com.alcadra.renderengine.window.widget.ClickableWidget;
import com.alcadra.renderengine.window.widget.DrawableWidget;

@SuppressWarnings("deprecation")
public class Window {
	
	private long windowId;
	private boolean isCreated = false;
	private String title;
	private Vector2i mousePos = Vector2i.getOrigin();
	private Vector2i size;
	private float aspectRatio;
	private CursorInputMode cursorInputMode = CursorInputMode.NORMAL;
	
	private List<Entry<UniversalRenderer, DrawableWidget<UniversalRenderer>>> renderizableUniversalWidgets = new LinkedList<>();
	private List<Entry<ObjectRenderer, DrawableWidget<ObjectRenderer>>> renderizableObjectWidgets = new LinkedList<>();
	private List<ClickableWidget> clickableWidgets = new LinkedList<>();
	
	private List<GLFWCharCallbackI> charCallbackList = new ArrayList<>(1);
	private List<GLFWCursorPosCallbackI> cursorPosCallbackList = new ArrayList<>(1);
	private List<GLFWCursorEnterCallbackI> cursorEnterCallbackList = new ArrayList<>(1);
	private List<GLFWKeyCallbackI> keyCallbackList = new ArrayList<>(1);
	private List<GLFWMouseButtonCallbackI> mouseButtonCallbackList = new ArrayList<>(1);
	private List<GLFWScrollCallbackI> scrollCallbackList = new ArrayList<>(1);
	private List<GLFWWindowSizeCallbackI> windowSizeCallbackList = new ArrayList<>(1);
	
	private GLFWCharCallbackI charCallback = (w, codepoint) -> charCallbackList.forEach(callback -> callback.invoke(w, codepoint));
	private GLFWCursorPosCallbackI cursorPosCallback = (w, x, y) -> {
		mousePos = new Vector2i((int) x, getHeight() - (int) y);
		for (GLFWCursorPosCallbackI callback : cursorPosCallbackList) callback.invoke(w, x, y);
	};
	private GLFWCursorEnterCallbackI cursorEnterCallback = (w, entered) -> cursorEnterCallbackList.forEach(callback -> callback.invoke(w, entered));
	private GLFWKeyCallbackI keyCallback = (w, key, scancode, action, mode) -> keyCallbackList.forEach(callback -> callback.invoke(w, key, scancode, action, mode));
	private GLFWMouseButtonCallbackI mouseButtonCallback = (w, button, action, mode) -> {
		for (GLFWMouseButtonCallbackI callback : mouseButtonCallbackList) callback.invoke(w, button, action, mode);
		if (action == GLFW.GLFW_PRESS) for (ClickableWidget e : clickableWidgets) if (e.isMouseHover()) e.onClick(button, mousePos);
	};
	private GLFWWindowSizeCallbackI windowSizeCallback = (w, x, y) -> windowSizeCallbackList.forEach(callback -> callback.invoke(w, x, y));
	private GLFWScrollCallbackI windowScrollCallback = (w, xOffset, yOffset) -> scrollCallbackList.forEach(callback -> callback.invoke(w, xOffset, yOffset));
	
	private GLCapabilities glCapabilities;
	
	public Window(int w, int h, String title)
	{
		this.title = title;
		size = new Vector2i(w, h);
		aspectRatio = (float) size.x() / size.y();
	}
	
	public void create()
	{
		if (!isCreated) isCreated = (windowId = GLFW.glfwCreateWindow(getWidth(), getHeight(), title, 0, 0)) != 0;
	}
	
	public void init()
	{
		if (isCreated)
		{
			FrameBuffer.DEFAULT.setDrawArea(0, 0, getWidth(), getHeight());
			GLFW.glfwSwapInterval(0);
			GLFW.glfwSetCharCallback(windowId, charCallback);
			GLFW.glfwSetCursorEnterCallback(windowId, cursorEnterCallback);
			GLFW.glfwSetCursorPosCallback(windowId, cursorPosCallback);
			GLFW.glfwSetKeyCallback(windowId, keyCallback);
			GLFW.glfwSetMouseButtonCallback(windowId, mouseButtonCallback);
			GLFW.glfwSetScrollCallback(windowId, windowScrollCallback);
			GLFW.glfwSetWindowSizeCallback(windowId, windowSizeCallback);
		}
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
	
	public void render(Matrix4f matrix, float partialTicks)
	{
		for (Entry<UniversalRenderer, DrawableWidget<UniversalRenderer>> entry : renderizableUniversalWidgets) entry.getValue().render(new Matrix4f(matrix), entry.getKey(), entry.getValue().getPos().xy(), partialTicks);
		for (Entry<ObjectRenderer, DrawableWidget<ObjectRenderer>> entry : renderizableObjectWidgets) entry.getValue().render(new Matrix4f(matrix), entry.getKey(), entry.getValue().getPos().xy(), partialTicks);
		while (FrameBuffer.getBinded().notDefault()) FrameBuffer.getBinded().unbind();
	}
	
	public void update(Matrix4f matrix, float partialTicks)
	{
		for (Entry<UniversalRenderer, DrawableWidget<UniversalRenderer>> entry : renderizableUniversalWidgets) entry.getValue().draw(new Matrix4f(matrix), entry.getKey(), partialTicks);
		for (Entry<ObjectRenderer, DrawableWidget<ObjectRenderer>> entry : renderizableObjectWidgets) entry.getValue().draw(new Matrix4f(matrix), entry.getKey(), partialTicks);
		pollEvents();
		swapBuffers();
	}
	
	public final void clear(boolean color, boolean depth, boolean stencil)
	{
		FrameBuffer.DEFAULT.clear(color, depth, stencil);
	}
	
	public final void addEvent(CallbackI event)
	{
		if (event instanceof GLFWCharCallbackI callback) charCallbackList.add(callback);
		if (event instanceof GLFWCursorPosCallbackI callback) cursorPosCallbackList.add(callback);
		if (event instanceof GLFWCursorEnterCallbackI callback) cursorEnterCallbackList.add(callback);
		if (event instanceof GLFWKeyCallbackI callback) keyCallbackList.add(callback);
		if (event instanceof GLFWMouseButtonCallbackI callback) mouseButtonCallbackList.add(callback);
		if (event instanceof GLFWScrollCallbackI callback) scrollCallbackList.add(callback);
		if (event instanceof GLFWWindowSizeCallbackI callback) windowSizeCallbackList.add(callback);
	}
	
	public final void addWidget(DrawableWidget<UniversalRenderer> widget, UniversalRenderer renderer)
	{
		if (widget instanceof ClickableWidget w) addWidget(w);
		renderizableUniversalWidgets.add(Map.entry(renderer, widget));
	}
	
	public final void addWidget(DrawableWidget<ObjectRenderer> widget, ObjectRenderer renderer)
	{
		if (widget instanceof ClickableWidget w) addWidget(w);
		renderizableObjectWidgets.add(Map.entry(renderer, widget));
	}
	
	public final void addWidget(ClickableWidget widget)
	{
		clickableWidgets.add(widget);
	}
	
	protected void onResize(Vector2i oldSize, Vector2i newSize)
	{
		aspectRatio = (float) newSize.x() / newSize.y();
		if (isCreated) FrameBuffer.DEFAULT.setDrawArea(Vector2i.getOrigin(), newSize);
	}
	
	public final long getId()
	{
		return windowId;
	}
	
	public final boolean isCreated()
	{
		return isCreated;
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
	
	public final void setMinMaxSizes(Vector2i minSize, Vector2i maxSize)
	{
		GLFW.glfwSetWindowSizeLimits(windowId, minSize.x(), minSize.y(), maxSize.x(), maxSize.y());
	}
	
	public final void setCursorInputMode(CursorInputMode mode)
	{
		if (cursorInputMode != mode)
		{
			cursorInputMode = mode;
			GLFW.glfwSetInputMode(windowId, GLFW.GLFW_CURSOR, mode.ordinal() + GLFW.GLFW_CURSOR_NORMAL);
		}
	}
	
	public final void setLockKeyModsInputMode(boolean value)
	{
		GLFW.glfwSetInputMode(windowId, GLFW.GLFW_LOCK_KEY_MODS, value ? 1 : 0);
	}
	
	public final void makeContextCurrent()
	{
		GLFW.glfwMakeContextCurrent(windowId);
		if (glCapabilities == null) glCapabilities = GL.createCapabilities();
		else GL.setCapabilities(glCapabilities);
	}
	
	public final void detachCurrentContext()
	{
		GLFW.glfwMakeContextCurrent(0);
	}
	
	public final boolean shouldClose()
	{
		return GLFW.glfwWindowShouldClose(windowId);
	}
	
	public final void show()
	{
		GLFW.glfwShowWindow(windowId);
		onResize(getSize(), getSize());
	}
	
	public final void hide()
	{
		GLFW.glfwHideWindow(windowId);
		onResize(getSize(), getSize());
	}
	
	public final void focus()
	{
		GLFW.glfwFocusWindow(windowId);
	}
	
	public final void requestAttention()
	{
		GLFW.glfwRequestWindowAttention(windowId);
	}
	
	public final void iconify()
	{
		GLFW.glfwIconifyWindow(windowId);
	}
	
	public final void maximize()
	{
		GLFW.glfwMaximizeWindow(windowId);
	}
	
	public final void restore()
	{
		GLFW.glfwRestoreWindow(windowId);
	}
	
	public final void setOpacity(float alpha)
	{
		GLFW.glfwSetWindowOpacity(windowId, alpha / 255);
	}
	
	public final void pollEvents()
	{
		GLFW.glfwPollEvents();
	}
	
	public final void swapBuffers()
	{
		GLFW.glfwSwapBuffers(windowId);
	}
	
	public final void setCursor(ICursor cursor)
	{
		GLFW.glfwSetCursor(windowId, cursor.getId());
	}
	
	public final void removeCursor()
	{
		GLFW.glfwSetCursor(windowId, 0);
	}
}
