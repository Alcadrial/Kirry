package com.alcadrial.renderengine.render.window;

import java.awt.Toolkit;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWCursorEnterCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWDropCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryUtil;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.core.Window;
import com.alcadrial.renderengine.core.callback.DropCallback;
import com.alcadrial.renderengine.core.callback.ExtraCallback;
import com.alcadrial.renderengine.core.callback.MouseClickCallback;
import com.alcadrial.renderengine.core.callback.MouseDragEndCallback;
import com.alcadrial.renderengine.core.callback.MouseDragStartCallback;
import com.alcadrial.renderengine.core.cursor.CursorInputMode;
import com.alcadrial.renderengine.render.framebuffer.FrameBuffer;
import com.alcadrial.renderengine.render.renderingmanager.RenderingManager;
import com.alcadrial.renderengine.render.textures.Texture;
import com.alcadrial.renderengine.render.window.widget.ClickableWidget;
import com.alcadrial.renderengine.render.window.widget.DrawableWidget;
import com.alcadrial.renderengine.render.window.widget.Widget;

public class OpenGlWindow extends Window {
	
	public static final int MULTI_CLICK_INTERVAL = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
	
	private class MouseButtonState {
		
		private long lastPressTimestamp;
		private long lastReleaseTimestamp = 1;
		private boolean canClick;
		private boolean canDrag;
		private Vector2i lastMousePos = mousePos;
		private boolean isDragging;
		private int clickCount;
	}
	
	private boolean isCreated;
	private String title;
	private Vector2i size;
	private float aspectRatio;
	
	private List<Entry<Supplier<RenderingManager>, DrawableWidget>> renderizableWidgets;
	private List<ClickableWidget> clickableWidgets;
	private List<ClickableWidget> draggingWidgets;
	
	private List<List<Object>> callbacksList;
	private List<GLFWCharCallbackI> glfwCharCallbackList;
	private List<GLFWCursorPosCallbackI> glfwCursorPosCallbackList;
	private List<GLFWCursorEnterCallbackI> glfwCursorEnterCallbackList;
	private List<GLFWDropCallbackI> glfwDropCallbackList;
	private List<GLFWKeyCallbackI> glfwKeyCallbackList;
	private List<GLFWMouseButtonCallbackI> glfwMouseButtonCallbackList;
	private List<GLFWScrollCallbackI> glfwScrollCallbackList;
	private List<GLFWWindowCloseCallbackI> glfwWindowCloseCallbackList;
	private List<GLFWWindowSizeCallbackI> glfwWindowSizeCallbackList;
	private List<DropCallback> dropCallbackList;
	private List<MouseClickCallback> mouseClickCallbackList;
	private List<MouseDragEndCallback> mouseDragEndCallbackList;
	private List<MouseDragStartCallback> mouseDragStartCallbackList;
	
	private List<MouseButtonState> mouseButtonStates;
	
	private GLCapabilities glCapabilities;
	
	public OpenGlWindow(int width, int height, String title)
	{
		this.title = title;
		size = Vector2i.of(width, height);
		aspectRatio = (float) size.x() / size.y();
		isCreated = false;
		firstInit();
	}
	
	public OpenGlWindow(Vector2i size, String title)
	{
		this.title = title;
		this.size = Vector2i.copyOf(size);
		aspectRatio = (float) size.x() / size.y();
		isCreated = false;
		firstInit();
	}
	
	private void firstInit()
	{
		renderizableWidgets = new LinkedList<>();
		clickableWidgets = new LinkedList<>();
		draggingWidgets = new LinkedList<>();
		
		glfwCharCallbackList = new ArrayList<>(1);
		glfwCursorPosCallbackList = new ArrayList<>(1);
		glfwCursorEnterCallbackList = new ArrayList<>(1);
		glfwDropCallbackList = new ArrayList<>(1);
		glfwKeyCallbackList = new ArrayList<>(1);
		glfwMouseButtonCallbackList = new ArrayList<>(1);
		glfwScrollCallbackList = new ArrayList<>(1);
		glfwWindowCloseCallbackList = new ArrayList<>(1);
		glfwWindowSizeCallbackList = new ArrayList<>(1);
		dropCallbackList = new ArrayList<>(1);
		mouseClickCallbackList = new ArrayList<>(1);
		mouseDragEndCallbackList = new ArrayList<>(1);
		mouseDragStartCallbackList = new ArrayList<>(1);
		
		callbacksList = new ArrayList<>(7);
		callbacksList.add(cast(glfwCharCallbackList));
		callbacksList.add(cast(glfwCursorPosCallbackList));
		callbacksList.add(cast(glfwCursorEnterCallbackList));
		callbacksList.add(cast(glfwDropCallbackList));
		callbacksList.add(cast(glfwKeyCallbackList));
		callbacksList.add(cast(glfwMouseButtonCallbackList));
		callbacksList.add(cast(glfwScrollCallbackList));
		callbacksList.add(cast(glfwWindowCloseCallbackList));
		callbacksList.add(cast(glfwWindowSizeCallbackList));
		
		callbacksList.add(cast(dropCallbackList));
		callbacksList.add(cast(mouseClickCallbackList));
		callbacksList.add(cast(mouseDragEndCallbackList));
		callbacksList.add(cast(mouseDragStartCallbackList));
		
		mouseButtonStates = new ArrayList<>(GLFW.GLFW_MOUSE_BUTTON_LAST + 1);
		for (int i = 0; i <= GLFW.GLFW_MOUSE_BUTTON_LAST; i++) mouseButtonStates.add(new MouseButtonState());
	}
	
	protected void charCallback(long id, int codepoint)
	{
		for (GLFWCharCallbackI callback : glfwCharCallbackList) callback.invoke(id, codepoint);
	}
	
	protected void cursorPosCallback(long id, double x, double y)
	{
		mousePos = Vector2i.ofImmutable((int) x, getHeight() - (int) y);
		for (GLFWCursorPosCallbackI callback : glfwCursorPosCallbackList) callback.invoke(id, x, y);
		for (ClickableWidget widget : clickableWidgets)
		{
			if (widget.isMouseHover())
			{
				if (!widget.isHovered(mousePos)) widget.onMouseExit(mousePos);
			}
			else
			{
				if (widget.isHovered(mousePos)) widget.onMouseEnter(mousePos);
			}
		}
		
		for (int i = 0; i < mouseButtonStates.size(); i++)
		{
			MouseButtonState state = mouseButtonStates.get(i);
			boolean flag = state.canDrag && !state.isDragging;
			if (state.canClick || flag)
			{
				int xOffset = Math.abs(mousePos.x() - state.lastMousePos.x());
				int yOffset = Math.abs(mousePos.y() - state.lastMousePos.y());
				if (state.canClick && (xOffset > 2 || yOffset > 2)) state.canClick = false;
				if (flag && (xOffset > 5 || yOffset > 5))
				{
					state.isDragging = true;
					for (MouseDragStartCallback callback : mouseDragStartCallbackList) callback.invoke(this, i, state.lastMousePos.x(), state.lastMousePos.y());
					for (ClickableWidget w : clickableWidgets) if (w.isHovered(state.lastMousePos))
					{
						w.onDragStart(state.lastMousePos);
						draggingWidgets.add(w);
					}
				}
			}
		}
	}
	
	protected void cursorEnterCallback(long id, boolean entered)
	{
		for (GLFWCursorEnterCallbackI callback : glfwCursorEnterCallbackList) callback.invoke(id, entered);
	}
	
	protected void keyCallback(long id, int key, int scancode, int action, int mods)
	{
		for (GLFWKeyCallbackI callback : glfwKeyCallbackList) callback.invoke(id, key, scancode, action, mods);
	}
	
	protected void mouseButtonCallback(long id, int button, int action, int mods)
	{
		for (GLFWMouseButtonCallbackI callback : glfwMouseButtonCallbackList) callback.invoke(id, button, action, mods);
		
		List<ClickableWidget> hovereds = new LinkedList<>();
		for (ClickableWidget e : clickableWidgets) if (e.isHovered(mousePos)) hovereds.add(e);
		
		MouseButtonState state = mouseButtonStates.get(button);
		switch (action)
		{
			case GLFW.GLFW_PRESS ->
			{
				for (ClickableWidget w : hovereds) w.onPress(button, mousePos);
				if (state.lastPressTimestamp < state.lastReleaseTimestamp)//release -> press
				{
					long now = System.currentTimeMillis();
					
					if (now - state.lastPressTimestamp > MULTI_CLICK_INTERVAL)
					{
						state.canClick = true;
						state.clickCount = 0;
					}
					else if (state.canClick)
					{
						state.clickCount++;
						for (MouseClickCallback callback : mouseClickCallbackList) callback.invoke(this, button, state.clickCount, mods);
						for (ClickableWidget w : hovereds) w.onClick(button, state.clickCount, mousePos);
					}
					else state.clickCount = 0;
					
					state.lastPressTimestamp = now;
					state.lastMousePos = mousePos;
					state.canDrag = true;
				}
			}
			case GLFW.GLFW_RELEASE ->
			{
				for (ClickableWidget w : hovereds) w.onRelease(button, mousePos);
				if (state.lastPressTimestamp > state.lastReleaseTimestamp)//press -> release
				{
					if (state.isDragging)
					{
						for (MouseDragEndCallback callback : mouseDragEndCallbackList) callback.invoke(this, button, mousePos.x(), mousePos.y());
						for (ClickableWidget w : draggingWidgets)
						{
							w.onDragEnd(mousePos);
							w.onDrag(state.lastMousePos, mousePos);
						}
						draggingWidgets.clear();
					}
					else if (state.clickCount == 0 && state.canClick)
					{
						state.clickCount = 1;
						for (MouseClickCallback callback : mouseClickCallbackList) callback.invoke(this, button, state.clickCount, mods);
						for (ClickableWidget w : hovereds) w.onClick(button, state.clickCount, mousePos);
					}
				}
				
				long now = System.currentTimeMillis();
				state.lastReleaseTimestamp = now;
				state.isDragging = false;
				state.canDrag = false;
				state.canClick = true;
			}
		}
	}
	
	protected void windowCloseCallback(long id)
	{
		for (GLFWWindowCloseCallbackI callback : glfwWindowCloseCallbackList) callback.invoke(id);
	}
	
	protected void windowScrollCallback(long id, double x, double y)
	{
		for (GLFWScrollCallbackI callback : glfwScrollCallbackList) callback.invoke(id, x, y);
	}
	
	protected void windowSizeCallback(long id, int width, int height)
	{
		size.setAll(width, height);
		FrameBuffer.DEFAULT.setDrawArea(0, 0, width, height);
		for (GLFWWindowSizeCallbackI callback : glfwWindowSizeCallbackList) callback.invoke(id, width, height);
	}
	
	protected void dropCallback(long id, int count, long names)
	{
		for (GLFWDropCallbackI callback : glfwDropCallbackList) callback.invoke(id, count, names);
		PointerBuffer p = MemoryUtil.memPointerBuffer(names, count);
		String[] s = new String[count];
		for (int i = 0; i < count; i++) s[i] = MemoryUtil.memUTF8(p.get());
		for (DropCallback callback : dropCallbackList) callback.invoke(this, s);
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
			GLFW.glfwSetDropCallback(id, this::dropCallback);
			GLFW.glfwSetKeyCallback(id, this::keyCallback);
			GLFW.glfwSetMouseButtonCallback(id, this::mouseButtonCallback);
			GLFW.glfwSetScrollCallback(id, this::windowScrollCallback);
			GLFW.glfwSetWindowCloseCallback(id, this::windowCloseCallback);
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
		if (event instanceof GLFWCharCallbackI callback) glfwCharCallbackList.add(callback);
		if (event instanceof GLFWCursorPosCallbackI callback) glfwCursorPosCallbackList.add(callback);
		if (event instanceof GLFWCursorEnterCallbackI callback) glfwCursorEnterCallbackList.add(callback);
		if (event instanceof GLFWDropCallbackI callback) glfwDropCallbackList.add(callback);
		if (event instanceof GLFWKeyCallbackI callback) glfwKeyCallbackList.add(callback);
		if (event instanceof GLFWMouseButtonCallbackI callback) glfwMouseButtonCallbackList.add(callback);
		if (event instanceof GLFWScrollCallbackI callback) glfwScrollCallbackList.add(callback);
		if (event instanceof GLFWWindowCloseCallbackI callback) glfwWindowCloseCallbackList.add(callback);
		if (event instanceof GLFWWindowSizeCallbackI callback) glfwWindowSizeCallbackList.add(callback);
	}
	
	public void addEvent(ExtraCallback event)
	{
		if (event instanceof DropCallback callback) dropCallbackList.add(callback);
		if (event instanceof MouseClickCallback callback) mouseClickCallbackList.add(callback);
		if (event instanceof MouseDragEndCallback callback) mouseDragEndCallbackList.add(callback);
		if (event instanceof MouseDragStartCallback callback) mouseDragStartCallbackList.add(callback);
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
		return mousePos;
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
	
	public final void swapBuffers()
	{
		GLFW.glfwSwapBuffers(id);
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
	
	@SuppressWarnings("unchecked")
	private <T, X> X cast(T t)
	{
		return (X) t;
	}
}
