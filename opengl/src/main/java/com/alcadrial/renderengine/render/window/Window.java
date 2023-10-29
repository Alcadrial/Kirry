package com.alcadrial.renderengine.render.window;

import java.awt.Toolkit;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

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
import org.lwjgl.system.MemoryUtil;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.core.cursor.CursorInputMode;
import com.alcadrial.renderengine.core.cursor.ICursor;
import com.alcadrial.renderengine.core.util.Image;
import com.alcadrial.renderengine.render.framebuffer.FrameBuffer;
import com.alcadrial.renderengine.render.renderingmanager.RenderingManager;
import com.alcadrial.renderengine.render.textures.Texture;
import com.alcadrial.renderengine.render.window.widget.ClickableWidget;
import com.alcadrial.renderengine.render.window.widget.DrawableWidget;
import com.alcadrial.renderengine.render.window.widget.Widget;

public class Window {
	
	public static final int MULTI_CLICK_INTERVAL = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
	
	private class MouseButtonState {
		
		private long lastPressTimestamp;
		private long lastReleaseTimestamp = 1;
		private boolean canDrag;
		private Vector2i lastMousePos;
		private boolean isDragging;
		private int clickCount;
	}
	
	private long id;
	private boolean isCreated;
	private String title;
	private Vector2i mousePos;
	private Vector2i size;
	private float aspectRatio;
	private CursorInputMode cursorInputMode;
	
	private List<Entry<Supplier<RenderingManager>, DrawableWidget>> renderizableWidgets;
	private List<ClickableWidget> clickableWidgets;
	
	private List<List<Object>> callbacksList;
	private List<GLFWCharCallbackI> charCallbackList;
	private List<GLFWCursorPosCallbackI> cursorPosCallbackList;
	private List<GLFWCursorEnterCallbackI> cursorEnterCallbackList;
	private List<GLFWKeyCallbackI> keyCallbackList;
	private List<GLFWMouseButtonCallbackI> mouseButtonCallbackList;
	private List<GLFWScrollCallbackI> scrollCallbackList;
	private List<GLFWWindowSizeCallbackI> windowSizeCallbackList;
	private List<MouseClickCallback> mouseClickCallbackList;
	private List<MouseDragStartCallback> mouseDragStartCallbackList;
	private List<MouseDragEndCallback> mouseDragEndCallbackList;
	
	private List<MouseButtonState> mouseButtonStates;
	
	private GLCapabilities glCapabilities;
	
	public Window(int width, int height, String title)
	{
		this.title = title;
		size = Vector2i.of(width, height);
		aspectRatio = (float) size.x() / size.y();
		isCreated = false;
		mousePos = Vector2i.of(0, 0);
		cursorInputMode = CursorInputMode.NORMAL;
		firstInit();
	}
	
	public Window(Vector2i size, String title)
	{
		this.title = title;
		this.size = Vector2i.copyOf(size);
		aspectRatio = (float) size.x() / size.y();
		isCreated = false;
		mousePos = Vector2i.of(0, 0);
		cursorInputMode = CursorInputMode.NORMAL;
		firstInit();
	}
	
	private void firstInit()
	{
		renderizableWidgets = new LinkedList<>();
		clickableWidgets = new LinkedList<>();
		
		charCallbackList = new ArrayList<>(1);
		cursorPosCallbackList = new ArrayList<>(1);
		cursorEnterCallbackList = new ArrayList<>(1);
		keyCallbackList = new ArrayList<>(1);
		mouseButtonCallbackList = new ArrayList<>(1);
		scrollCallbackList = new ArrayList<>(1);
		windowSizeCallbackList = new ArrayList<>(1);
		mouseClickCallbackList = new ArrayList<>(1);
		mouseDragStartCallbackList = new ArrayList<>(1);
		mouseDragEndCallbackList = new ArrayList<>(1);
		
		callbacksList = new ArrayList<>(7);
		callbacksList.add(cast(charCallbackList));
		callbacksList.add(cast(cursorPosCallbackList));
		callbacksList.add(cast(cursorEnterCallbackList));
		callbacksList.add(cast(keyCallbackList));
		callbacksList.add(cast(mouseButtonCallbackList));
		callbacksList.add(cast(scrollCallbackList));
		callbacksList.add(cast(windowSizeCallbackList));
		callbacksList.add(cast(mouseClickCallbackList));
		callbacksList.add(cast(mouseDragStartCallbackList));
		callbacksList.add(cast(mouseDragEndCallbackList));
		
		mouseButtonStates = new ArrayList<>(GLFW.GLFW_MOUSE_BUTTON_LAST + 1);
		for (int i = 0; i <= GLFW.GLFW_MOUSE_BUTTON_LAST; i++) mouseButtonStates.add(new MouseButtonState());
	}
	
	protected void charCallback(long id, int codepoint)
	{
		for (GLFWCharCallbackI callback : charCallbackList) callback.invoke(id, codepoint);
	}
	
	protected void cursorPosCallback(long id, double x, double y)
	{
		mousePos.setAll((int) x, getHeight() - (int) y);
		for (GLFWCursorPosCallbackI callback : cursorPosCallbackList) callback.invoke(id, x, y);
		
		for (int i = 0; i < mouseButtonStates.size(); i++)
		{
			MouseButtonState state = mouseButtonStates.get(i);
			if (state.canDrag && !state.isDragging)
			{
				if (Math.abs(mousePos.x() - state.lastMousePos.x()) > 5 || Math.abs(mousePos.y() - state.lastMousePos.y()) > 5)
				{
					state.isDragging = true;
					for (MouseDragStartCallback callback : mouseDragStartCallbackList) callback.invoke(id, i, state.lastMousePos.x(), state.lastMousePos.y());
				}
			}
		}
	}
	
	protected void cursorEnterCallback(long id, boolean entered)
	{
		for (GLFWCursorEnterCallbackI callback : cursorEnterCallbackList) callback.invoke(id, entered);
	}
	
	protected void keyCallback(long id, int key, int scancode, int action, int mods)
	{
		for (GLFWKeyCallbackI callback : keyCallbackList) callback.invoke(id, key, scancode, action, mods);
	}
	
	protected void mouseButtonCallback(long id, int button, int action, int mods)
	{
		for (GLFWMouseButtonCallbackI callback : mouseButtonCallbackList) callback.invoke(id, button, action, mods);
		MouseButtonState state = mouseButtonStates.get(button);
		switch (action)
		{
			case GLFW.GLFW_PRESS ->
			{
				for (ClickableWidget e : clickableWidgets) if (e.isMouseHover()) e.onClick(button, mousePos);
				
				if (state.lastPressTimestamp < state.lastReleaseTimestamp)//release -> press
				{
					state.lastMousePos = Vector2i.copyOfImmutable(mousePos);
					
					long now = System.currentTimeMillis();
					
					if (now - state.lastPressTimestamp > MULTI_CLICK_INTERVAL)
					{
						state.canDrag = true;
						state.clickCount = 0;
					}
					else
					{
						state.clickCount++;
						for (MouseClickCallback callback : mouseClickCallbackList) callback.invoke(id, button, state.clickCount, mods);
					}
					
					state.lastPressTimestamp = now;
				}
			}
			case GLFW.GLFW_RELEASE ->
			{
				if (state.lastPressTimestamp > state.lastReleaseTimestamp)//press -> release
				{
					if (state.isDragging)
					{
						for (MouseDragEndCallback callback : mouseDragEndCallbackList) callback.invoke(id, button, mousePos.x(), mousePos.y());
					}
					else if (state.clickCount == 0)
					{
						state.clickCount = 1;
						for (MouseClickCallback callback : mouseClickCallbackList) callback.invoke(id, button, state.clickCount, mods);
					}
				}
				
				long now = System.currentTimeMillis();
				state.lastReleaseTimestamp = now;
				state.isDragging = false;
				state.canDrag = false;
			}
		}
	}
	
	protected void windowSizeCallback(long id, int width, int height)
	{
		size.setAll(width, height);
		FrameBuffer.DEFAULT.setDrawArea(0, 0, width, height);
		for (GLFWWindowSizeCallbackI callback : windowSizeCallbackList) callback.invoke(id, width, height);
	}
	
	protected void windowScrollCallback(long id, double x, double y)
	{
		for (GLFWScrollCallbackI callback : scrollCallbackList) callback.invoke(id, x, y);
	}
	
	public void create()
	{
		if (!isCreated) isCreated = (id = GLFW.glfwCreateWindow(getWidth(), getHeight(), title, 0, 0)) != 0;
	}
	
	public void init()
	{
		if (isCreated)
		{
			FrameBuffer.DEFAULT.setDrawArea(0, 0, getWidth(), getHeight());
			GLFW.glfwSwapInterval(0);
			GLFW.glfwSetCharCallback(id, this::charCallback);
			GLFW.glfwSetCursorEnterCallback(id, this::cursorEnterCallback);
			GLFW.glfwSetCursorPosCallback(id, this::cursorPosCallback);
			GLFW.glfwSetKeyCallback(id, this::keyCallback);
			GLFW.glfwSetMouseButtonCallback(id, this::mouseButtonCallback);
			GLFW.glfwSetScrollCallback(id, this::windowScrollCallback);
			GLFW.glfwSetWindowSizeCallback(id, this::windowSizeCallback);
		}
	}
	
	public void initializeWidgets(Matrix4f matrix, float partialTicks)
	{
		for (Entry<Supplier<RenderingManager>, DrawableWidget> entry : renderizableWidgets)
		{
			while (FrameBuffer.getBinded().notDefault()) FrameBuffer.getBinded().unbind();
			entry.getValue().initializeRendering(new Matrix4f(matrix), entry.getKey().get(), partialTicks);
		}
	}
	
	public void renderWidgets(Matrix4f matrix, float partialTicks)
	{
		for (Entry<Supplier<RenderingManager>, DrawableWidget> entry : renderizableWidgets)
		{
			while (FrameBuffer.getBinded().notDefault()) FrameBuffer.getBinded().unbind();
			entry.getValue().render(new Matrix4f(matrix), entry.getKey().get(), partialTicks);
		}
	}
	
	public void drawWidgets(Matrix4f matrix, float partialTicks)
	{
		for (Entry<Supplier<RenderingManager>, DrawableWidget> entry : renderizableWidgets)
		{
			while (FrameBuffer.getBinded().notDefault()) FrameBuffer.getBinded().unbind();
			entry.getValue().draw(new Matrix4f(matrix), entry.getKey().get(), partialTicks);
		}
	}
	
	public void update()
	{
		pollEvents();
		swapBuffers();
	}
	
	public final void clear(boolean color, boolean depth, boolean stencil)
	{
		FrameBuffer.DEFAULT.clear(color, depth, stencil);
	}
	
	public void addEvent(CallbackI event)
	{
		if (event instanceof GLFWCharCallbackI callback) charCallbackList.add(callback);
		if (event instanceof GLFWCursorPosCallbackI callback) cursorPosCallbackList.add(callback);
		if (event instanceof GLFWCursorEnterCallbackI callback) cursorEnterCallbackList.add(callback);
		if (event instanceof GLFWKeyCallbackI callback) keyCallbackList.add(callback);
		if (event instanceof GLFWMouseButtonCallbackI callback) mouseButtonCallbackList.add(callback);
		if (event instanceof GLFWScrollCallbackI callback) scrollCallbackList.add(callback);
		if (event instanceof GLFWWindowSizeCallbackI callback) windowSizeCallbackList.add(callback);
	}
	
	public void addEvent(ExtraCallback event)
	{
		if (event instanceof MouseClickCallback callback) mouseClickCallbackList.add(callback);
	}
	
	public void removeEvent(CallbackI event)
	{
		for (List<Object> list : callbacksList) list.remove(event);
	}
	
	public void removeEvent(ExtraCallback event)
	{
		for (List<Object> list : callbacksList) list.remove(event);
	}
	
	public void addWidget(DrawableWidget widget, Supplier<RenderingManager> manager)
	{
		if (widget instanceof ClickableWidget w) addWidget(w);
		renderizableWidgets.add(cast(Map.entry(manager, widget)));
	}
	
	public void removeWidget(Widget widget)
	{
		renderizableWidgets.removeIf(e -> e.getValue().equals(widget));
		clickableWidgets.remove(widget);
	}
	
	public void addWidget(ClickableWidget widget)
	{
		clickableWidgets.add(widget);
	}
	
	public final long getId()
	{
		return id;
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
		return Vector2i.copyOf(mousePos);
	}
	
	public final Vector2i getSize()
	{
		return Vector2i.copyOf(size);
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
	
	public final void setWidth(int width)
	{
		setSize(Vector2i.of(width, getHeight()));
	}
	
	public final void setHeight(int height)
	{
		setSize(Vector2i.of(getWidth(), height));
	}
	
	public final void setSize(Vector2i size)
	{
		Vector2i oldSize = this.size.xy();
		resize(size);
		this.size = size.xy();
		onResize(oldSize, size);
	}
	
	protected void resize(Vector2i newSize)
	{
		aspectRatio = (float) newSize.x() / newSize.y();
		if (isCreated) FrameBuffer.DEFAULT.setDrawArea(Vector2i.origin(), newSize);
	}
	
	protected void onResize(Vector2i oldSize, Vector2i newSize)
	{
		
	}
	
	public final void makeContextCurrent()
	{
		GLFW.glfwMakeContextCurrent(id);
		if (glCapabilities == null) glCapabilities = GL.createCapabilities();
		else GL.setCapabilities(glCapabilities);
	}
	
	public final void detachCurrentContext()
	{
		GLFW.glfwMakeContextCurrent(0);
	}
	
	// --------------------------------------------------------------------------------------
	
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
	}
	
	protected void onFocus()
	{
		
	}
	
	public final void requestAttention()
	{
		GLFW.glfwRequestWindowAttention(id);
	}
	
	public final void pollEvents()
	{
		GLFW.glfwPollEvents();
	}
	// --------------------------------------------------------------------------------------
	
	public final void swapBuffers()
	{
		GLFW.glfwSwapBuffers(id);
	}
	
	// --------------------------------------------------------------------------------------
	public final void removeCursor()
	{
		GLFW.glfwSetCursor(id, 0);
	}
	
	public final void setOpacity(float alpha)
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
	
	public final void setIcon(Texture texture)
	{
		IntBuffer buffer = MemoryUtil.memAllocInt(texture.getLength());
		buffer.put(texture.getDataArray()).flip();
		Buffer b = GLFWImage.malloc(1).put(0, GLFWImage.malloc().set(texture.getWidth(), texture.getHeight(), MemoryUtil.memByteBuffer(buffer)));
		GLFW.glfwSetWindowIcon(id, b);
		b.get().free();
		MemoryUtil.memFree(buffer);
	}
	
	public final void setIcon(Image image)
	{
		Buffer b = GLFWImage.malloc(1).put(0, GLFWImage.malloc().set(image.getBuffered().getWidth(), image.getBuffered().getHeight(), image.toByteBuffer()));
		GLFW.glfwSetWindowIcon(id, b);
		b.get().free();
	}
	
	@SuppressWarnings("unchecked")
	private <T, X> X cast(T t)
	{
		return (X) t;
	}
}
