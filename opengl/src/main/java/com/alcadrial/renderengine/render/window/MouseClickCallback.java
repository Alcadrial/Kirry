package com.alcadrial.renderengine.render.window;

import org.lwjgl.system.NativeType;

public interface MouseClickCallback {
	
	void invoke(@NativeType("GLFWwindow *") long window, int button, int count, int mods);
}
