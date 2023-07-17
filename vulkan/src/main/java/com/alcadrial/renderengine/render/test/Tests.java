package com.alcadrial.renderengine.render.test;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowIconifyCallbackI;
import org.lwjgl.glfw.GLFWWindowMaximizeCallbackI;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.Window;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.util.Properties;

@Test
public class Tests {
	
	public static void main(String[] args)
	{
		Property.parseProperties(args, Properties.PROPERTIES);
		
		System.out.println("Start time = " + recordTime(Tests::start, TimeUnit.MICROSECONDS) / 1000D + "ms");
		if (!RenderEngine.isInitializated()) return;
		System.out.println("Loop time = " + recordTime(Tests::loop, TimeUnit.MICROSECONDS) / 1000D + "ms");
		if (!RenderEngine.isInitializated()) return;
		System.out.println("End time = " + recordTime(Tests::end, TimeUnit.MICROSECONDS) / 1000D + "ms");
	}
	
	public static void start()
	{
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		RenderEngine.setDebugMessengerCallback(Tests::debugCallback);
		
		System.out.println("Render Engine initialization time = " + recordTime(() -> {
			if (!RenderEngine.initialize()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		}, TimeUnit.MICROSECONDS) / 1000D + "ms");
		
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
		//GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);
		Vulkan vulkan = RenderEngine.getVulkan();
		
		System.out.println("Window creation time = " + recordTime(() -> vulkan.setWindow(new Window(1080, 720, "Test")), TimeUnit.MICROSECONDS) / 1000D + "ms");
		
		vulkan.setPhysicalDeviceFilter(physicalDevice -> physicalDevice.getQueueGraphicsFamilyProperties() != null);
		vulkan.pickPhysicalDevices(PhysicalDevice.typeBestMatcher());
		Window window = vulkan.getWindow();
		window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> {
			System.out.println(iconified ? "Iconified" : "Restored");
		});
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			System.out.println("Resized " + width + "x" + height);
		});
		window.setCallback((GLFWWindowRefreshCallbackI) w -> {
			System.out.println("Refresh");
		});
		window.setCallback((GLFWWindowCloseCallbackI) w -> {
			System.out.println("Closed");
		});
		window.setCallback((GLFWWindowMaximizeCallbackI) (w, maximized) -> {
			System.out.println(maximized ? "Maximized" : "Restored");
		});
		Vector2i[] oldPos = new Vector2i[] {Vector2i.of(0, 0)};
		Vector2f[] oldMousePos = new Vector2f[] {Vector2f.of(0, 0), Vector2f.of(0, 0)};
		boolean[] pressing = new boolean[2];
		window.setCallback((GLFWMouseButtonCallbackI) (w, button, action, mods) -> {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) if (pressing[0] = action == GLFW.GLFW_PRESS) window.getMousePos(oldMousePos[0]);
		});
		window.setCallback((GLFWCursorPosCallbackI) (w, x, y) -> {
			if (pressing[0] && !pressing[1])
			{
				pressing[1] = true;
				window.getPos(oldPos[0]);
				window.setPos(Vector2i.of((float) x - oldMousePos[0].x() + oldPos[0].x(), (float) y - oldMousePos[0].y() + oldPos[0].y(), RoundingMode.FLOOR));
				window.getMousePos(oldMousePos[0]);
				pressing[1] = false;
			}
		});
		
		System.out.println("Window initialization time = " + recordTime(window::initialize, TimeUnit.MICROSECONDS) / 1000D + "ms");
		Shader vertexShader;
		Shader fragmentShader;
		try
		{
			vertexShader = new Shader(getFile("shader.vert"), getFile("shader.vert.json"));
			if (vertexShader.compile().hasErrors()) throw new IOException("Shader " + vertexShader.getFile() + " has errors");
			fragmentShader = new Shader(getFile("shader.frag"), getFile("shader.frag.json"));
			if (fragmentShader.compile().hasErrors()) throw new IOException("Shader " + fragmentShader.getFile() + " has errors");
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		window.setPipelineFrontOrientation(true);
		
		System.out.println("Graphics pipeline build time = " + recordTime(() -> window.buildGraphicsPipeline(vertexShader, fragmentShader), TimeUnit.MICROSECONDS) / 1000D + "ms");
	}
	
	public static void loop()
	{
		Device device = RenderEngine.getVulkan().getDevice();
		Window window = device.getWindow();
		
		window.hide();
		window.show();
		window.maximize();
		window.restore();
		
		window.setClearColor(Color.of(0, 0, 0, 0));
		
		while (!window.shouldClose())
		{
			window.pollEvents();
			window.draw();
		}
	}
	
	public static void end()
	{
		RenderEngine.terminate();
	}
	
	public static int debugCallback(int messageSeverity, int messageType, long callbackDataId, long userDataId)
	{
		VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataId);
		
		System.out.println("[Vulkan debug] " + callbackData.pMessageString());
		
		return VK10.VK_FALSE;
	}
	
	public static File getFile(String path)
	{
		return new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/" + path);
	}
	
	public static long recordTime(Runnable runnable, TimeUnit unit)
	{
		long startTime;
		TimeUnit localUnit;
		if (unit == TimeUnit.MICROSECONDS || unit == TimeUnit.NANOSECONDS)
		{
			startTime = System.nanoTime();
			localUnit = TimeUnit.NANOSECONDS;
		}
		else
		{
			startTime = System.currentTimeMillis();
			localUnit = TimeUnit.MILLISECONDS;
		}
		
		runnable.run();
		
		long endTime;
		if (localUnit == TimeUnit.NANOSECONDS) endTime = System.nanoTime();
		else endTime = System.currentTimeMillis();
		
		return unit.convert(endTime - startTime, localUnit);
	}
}
