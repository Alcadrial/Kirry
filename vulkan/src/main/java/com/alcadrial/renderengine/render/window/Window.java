package com.alcadrial.renderengine.render.window;

import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWCursorEnterCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWDropCallbackI;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowContentScaleCallbackI;
import org.lwjgl.glfw.GLFWWindowFocusCallbackI;
import org.lwjgl.glfw.GLFWWindowIconifyCallbackI;
import org.lwjgl.glfw.GLFWWindowMaximizeCallbackI;
import org.lwjgl.glfw.GLFWWindowPosCallbackI;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryStack;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Surface;
import com.alcadrial.renderengine.render.util.Pair;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanResource;

public class Window extends VulkanResource<Long> {
	
	static
	{
		if (!RenderEngine.isInitializated()) throw new RuntimeException("Use of RenderEngine Vulkan without initialization, use RenderEngine#initialize before anything");
	}
	
	private Surface surface;
	private int width;
	private int height;
	private List<WindowResizeAction> resizeActions;
	
	public Window(int width, int height, String title)
	{
		vk = VulkanErrorUtils.nonNull(GLFW.glfwCreateWindow(width, height, title, 0, 0), "Cannot create window");
		this.width = width;
		this.height = height;
		surface = new Surface(this);
	}
	
	public void update()
	{
		
	}
	
	public void addResizeAction(WindowResizeAction action)
	{
		resizeActions.add(action);
	}
	
	public void assignDevice(Device device)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			surface.init(device, stack);
		}
	}
	
	public final void resetCallbacks()
	{
		//	 GLFW.glfwSetCharCallback(id, i);
		//	GLFW.glfwSetCharModsCallback(id, i);
		//	 GLFW.glfwSetCursorEnterCallback(id, i);
		//	 GLFW.glfwSetCursorPosCallback(id, i);
		//	 GLFW.glfwSetDropCallback(id, i);
		//	GLFW.glfwSetKeyCallback(id, i); 
		//	 GLFW.glfwSetMouseButtonCallback(id, i);
		//	 GLFW.glfwSetScrollCallback(id, i);
		GLFW.glfwSetWindowCloseCallback(vk, w -> onHide());
		//	GLFW.glfwSetWindowContentScaleCallback(id, i);
		GLFW.glfwSetWindowFocusCallback(vk, (w, f) -> onFocus());
		GLFW.glfwSetWindowIconifyCallback(vk, (w, c) -> {
			if (c) onIconify();
			else onRestore();
		});
		GLFW.glfwSetWindowMaximizeCallback(vk, (w, m) -> {
			if (m) onMaximize();
			else onRestore();
		});
		//	GLFW.glfwSetWindowPosCallback(id, i);
		//	GLFW.glfwSetWindowRefreshCallback(id, i);
		GLFW.glfwSetWindowSizeCallback(vk, (p, w, h) -> {
			width = w;
			height = h;
		});
		GLFW.glfwSetFramebufferSizeCallback(vk, (p, w, h) -> {
			for (WindowResizeAction action : resizeActions) action.run(w, h);
		});
	}
	
	public final void setCallback(CallbackI callback)
	{
		if (callback instanceof GLFWCharCallbackI i) GLFW.glfwSetCharCallback(vk, i);
		if (callback instanceof GLFWCharModsCallbackI i) GLFW.glfwSetCharModsCallback(vk, i);
		if (callback instanceof GLFWCursorEnterCallbackI i) GLFW.glfwSetCursorEnterCallback(vk, i);
		if (callback instanceof GLFWCursorPosCallbackI i) GLFW.glfwSetCursorPosCallback(vk, i);
		if (callback instanceof GLFWDropCallbackI i) GLFW.glfwSetDropCallback(vk, i);
		if (callback instanceof GLFWKeyCallbackI i) GLFW.glfwSetKeyCallback(vk, i);
		if (callback instanceof GLFWMouseButtonCallbackI i) GLFW.glfwSetMouseButtonCallback(vk, i);
		if (callback instanceof GLFWScrollCallbackI i) GLFW.glfwSetScrollCallback(vk, i);
		if (callback instanceof GLFWWindowCloseCallbackI i) GLFW.glfwSetWindowCloseCallback(vk, w -> {
			onHide();
			i.invoke(w);
		});
		if (callback instanceof GLFWWindowContentScaleCallbackI i) GLFW.glfwSetWindowContentScaleCallback(vk, i);
		if (callback instanceof GLFWWindowFocusCallbackI i) GLFW.glfwSetWindowFocusCallback(vk, (w, f) -> {
			onFocus();
			i.invoke(w, f);
		});
		if (callback instanceof GLFWWindowIconifyCallbackI i) GLFW.glfwSetWindowIconifyCallback(vk, (w, c) -> {
			if (c) onIconify();
			else onRestore();
			i.invoke(w, c);
		});
		if (callback instanceof GLFWWindowMaximizeCallbackI i) GLFW.glfwSetWindowMaximizeCallback(vk, (w, m) -> {
			if (m) onMaximize();
			else onRestore();
			i.invoke(w, m);
		});
		if (callback instanceof GLFWWindowPosCallbackI i) GLFW.glfwSetWindowPosCallback(vk, i);
		if (callback instanceof GLFWWindowRefreshCallbackI i) GLFW.glfwSetWindowRefreshCallback(vk, i);
		if (callback instanceof GLFWWindowSizeCallbackI i) GLFW.glfwSetWindowSizeCallback(vk, (p, w, h) -> {
			width = w;
			height = h;
			i.invoke(p, w, h);
		});
		if (callback instanceof GLFWFramebufferSizeCallbackI i) GLFW.glfwSetFramebufferSizeCallback(vk, (p, w, h) -> {
			for (WindowResizeAction action : resizeActions) action.run(w, h);
			i.invoke(p, w, h);
		});
	}
	
	public final Vector2f getMousePos(Vector2f holder)
	{
		double[][] p = new double[2][1];
		GLFW.glfwGetCursorPos(vk, p[0], p[1]);
		return holder.setAll((float) p[0][0], (float) p[1][0]);
	}
	
	public final Vector2f getMousePos()
	{
		double[][] p = new double[2][1];
		GLFW.glfwGetCursorPos(vk, p[0], p[1]);
		return Vector2f.of((float) p[0][0], (float) p[1][0]);
	}
	
	public final Pair<Double, Double> getHighPrecisionMousePos(Pair<Double, Double> holder)
	{
		double[][] p = new double[2][1];
		GLFW.glfwGetCursorPos(vk, p[0], p[1]);
		holder.setLeft(p[0][0]);
		holder.setRigth(p[1][0]);
		return holder;
	}
	
	public final Pair<Double, Double> getHighPrecisionMousePos()
	{
		double[][] p = new double[2][1];
		GLFW.glfwGetCursorPos(vk, p[0], p[1]);
		return new Pair<>(p[0][0], p[1][0]);
	}
	
	public final void setMousePos(Vector2f pos)
	{
		GLFW.glfwSetCursorPos(vk, pos.x(), pos.y());
	}
	
	public final Vector2i getPos(Vector2i holder)
	{
		int[][] p = new int[2][1];
		GLFW.glfwGetWindowPos(vk, p[0], p[1]);
		return holder.setAll(p[0][0], p[1][0]);
	}
	
	public final Vector2i getPos()
	{
		int[][] p = new int[2][1];
		GLFW.glfwGetWindowPos(vk, p[0], p[1]);
		return Vector2i.of(p[0][0], p[1][0]);
	}
	
	public final void setPos(Vector2i pos)
	{
		GLFW.glfwSetWindowPos(vk, pos.x(), pos.y());
	}
	
	public final boolean shouldClose()
	{
		return GLFW.glfwWindowShouldClose(vk);
	}
	
	public final void show()
	{
		GLFW.glfwShowWindow(vk);
		onShow();
	}
	
	protected void onShow()
	{
		
	}
	
	public final void hide()
	{
		GLFW.glfwHideWindow(vk);
		onHide();
	}
	
	protected void onHide()
	{
		
	}
	
	public final void iconify()
	{
		GLFW.glfwIconifyWindow(vk);
		onIconify();
	}
	
	protected void onIconify()
	{
		
	}
	
	public final void maximize()
	{
		GLFW.glfwMaximizeWindow(vk);
		onMaximize();
	}
	
	protected void onMaximize()
	{
		
	}
	
	public final void restore()
	{
		GLFW.glfwRestoreWindow(vk);
		onRestore();
	}
	
	protected void onRestore()
	{
		
	}
	
	public final void focus()
	{
		GLFW.glfwFocusWindow(vk);
	}
	
	protected void onFocus()
	{
		
	}
	
	public final void requestAttention()
	{
		GLFW.glfwRequestWindowAttention(vk);
	}
	
	public final void pollEvents()
	{
		GLFW.glfwPollEvents();
	}
	
	public Surface getSurface()
	{
		return surface;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	@Override
	public void close()
	{
		surface.close();
		GLFW.glfwDestroyWindow(vk);
		vk = 0L;
	}
}
