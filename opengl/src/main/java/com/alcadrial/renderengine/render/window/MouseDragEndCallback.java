package com.alcadrial.renderengine.render.window;

import org.lwjgl.system.NativeType;

public interface MouseDragEndCallback {
	
	void invoke(@NativeType("GLFWwindow *") long window, int button, double x, double y);
}
