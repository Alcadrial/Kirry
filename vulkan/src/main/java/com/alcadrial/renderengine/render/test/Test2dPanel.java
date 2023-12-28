package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.IntBuffer;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowIconifyCallbackI;
import org.lwjgl.glfw.GLFWWindowMaximizeCallbackI;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Renderer.DrawResult;
import com.alcadrial.renderengine.render.VertexAccumulator;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.test.RendererUtils.RectangleVertexResult;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class Test2dPanel extends RenderTest {
	
	private static final String TRANSFORMATION_UNIFORM = "TRANSFORMATION";
	
	public static void main(String[] args)
	{
		TestProperties.init(args);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		println("Render Engine initialization", () -> {
			if (!RenderEngine.create("Test", new Version(0, 0, 1, 0)).messenger(Test2dPanel::debugCallback).initialize()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		}, TestProperties.PRINT_LOADING_TIMES);
		
		println("Start", Test2dPanel::start, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		println("Loop", Test2dPanel::loop, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		println("End", Test2dPanel::end, TestProperties.PRINT_LOADING_TIMES);
	}
	
	private static Renderer renderer;
	private static VertexBuffer vertexBuffer;
	private static IndexBuffer indexBuffer;
	private static Vector2i panelPos;
	
	public static void start()
	{
		Vulkan vulkan = RenderEngine.getVulkan();
		
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		println("Window creation", () -> vulkan.setWindow(new VulkanWindow(1080, 720, "Test")), TestProperties.PRINT_LOADING_TIMES);
		
		vulkan.pickPhysicalDevices(PhysicalDevice.typeBestMatcher());
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
		deviceFeatures.wideLines(true);
		vulkan.createDevice(deviceFeatures);
		
		VulkanWindow window = vulkan.getWindow();
		Device device = vulkan.getDevice();
		window.assignDevice(device);
		
		window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> {
			System.out.println(iconified ? "Iconified" : "Restored");
		});
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			System.out.println("Resized " + width + "x" + height);
			isModifiedMatrix = true;
		});
		window.setCallback((GLFWWindowRefreshCallbackI) w -> {
			System.out.println("Refresh");
			isModified = true;
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
		panelPos = Vector2i.of(0, 0);
		window.setCallback((GLFWKeyCallbackI) (w, k, s, a, m) -> {
			if (a == GLFW.GLFW_PRESS || a == GLFW.GLFW_REPEAT)
			{
				switch (k)
				{
					case 'W':
						panelPos.add(0, 1);
						isModified = isModifiedMatrix = true;
						break;
					case 'A':
						panelPos.add(-1, 0);
						isModified = isModifiedMatrix = true;
						break;
					case 'S':
						panelPos.add(0, -1);
						isModified = isModifiedMatrix = true;
						break;
					case 'D':
						panelPos.add(1, 0);
						isModified = isModifiedMatrix = true;
						break;
				}
			}
		});
		
		Shader vertexShader;
		ShaderInfo vertexShaderInfo;
		Shader fragmentShader;
		ShaderInfo fragmentShaderInfo;
		try
		{
			vertexShader = new Shader(getFile("shader.vert"));
			vertexShaderInfo = ShaderInfo.fromJson(getFile("shader.vert.json"));
			vertexShader.compile(vertexShaderInfo);
			if (!vertexShader.isCompilationSuccessfull()) throw new IOException("Shader " + vertexShader.getFile() + " has errors");
			
			fragmentShader = new Shader(getFile("shader.frag"));
			fragmentShaderInfo = ShaderInfo.fromJson(getFile("shader.frag.json"));
			fragmentShader.compile(fragmentShaderInfo);
			if (!fragmentShader.isCompilationSuccessfull()) throw new IOException("Shader " + fragmentShader.getFile() + " has errors");
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		Builder builder = Renderer.create();
		builder.vertexShader(vertexShader, vertexShaderInfo).fragmentShader(fragmentShader, fragmentShaderInfo);
		builder.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		builder.lineWidth(2F);
		builder.uniformBuffer(new VulkanBuffer(device, 64, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT));
		renderer = builder.build(window, device);
		
		vertexBuffer = new VertexBuffer(7 * 4 * 512 * 512);
		indexBuffer = new IndexBuffer(6 * 512 * 512);
		renderer.addVertexBuffer(0, vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
		
		Random random = new Random();
		VertexAccumulator vertexAccumulator = renderer.accumulate(vertexBuffer);
		IntBuffer buffer = indexBuffer.asIntBuffer();
		for (int i = -256; i < 256; i++)
		{
			for (int j = -256; j < 256; j++)
			{
				RectangleVertexResult result = RendererUtils.loadRectangleXYZRGBA(vertexAccumulator, j, i, j + 1, i + 1, 0, random.nextFloat(255), random.nextFloat(255), random.nextFloat(255), random.nextFloat(255));
				buffer.put(result.getR0().getIndex());
				buffer.put(result.getR1().getIndex());
				buffer.put(result.getR3().getIndex());
				buffer.put(result.getR1().getIndex());
				buffer.put(result.getR2().getIndex());
				buffer.put(result.getR3().getIndex());
			}
		}
		vertexAccumulator.end();
		indexBuffer.sendToPhysicalDevice();
		
	}
	
	private static long time;
	private static boolean isModifiedMatrix;
	private static boolean isModified;
	
	public static void loop()
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		renderer.setClearColor(Color.of(0, 0, 0, 0));
		isModified = true;
		isModifiedMatrix = true;
		
		int maxfps = 120;
		long firstHighMilestone = 1000000000;
		long firstMilestone = 1000000000 / maxfps;
		
		time = System.nanoTime();
		long current;
		long frame = 0;
		long milestone = firstMilestone;
		long highMilestone = firstHighMilestone;
		
		while (!window.shouldClose())
		{
			current = System.nanoTime();
			long enlapsed = current - time;
			long pauseTime = milestone - enlapsed;
			if (pauseTime > 0)
			{
				try
				{
					synchronized (window)
					{
						window.wait(pauseTime / 1000000, (int) (pauseTime % 1000000));
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace(System.out);
				}
				continue;
			}
			milestone += (1 - pauseTime / firstMilestone) * firstMilestone;
			if (enlapsed >= highMilestone)
			{
				window.setTitle("Test fps=" + frame);
				frame = 0;
				highMilestone += (enlapsed - highMilestone + firstHighMilestone) / firstHighMilestone * firstHighMilestone;
			}
			
			println("Frame " + frame, Test2dPanel::frame, TestProperties.PRINT_FRAME_TIMES);
			frame++;
		}
	}
	
	public static void frame()
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		if (isModifiedMatrix)
		{
			isModifiedMatrix = false;
			renderer.updateUniformBuffers((b, l) -> {
				Matrix4f orthographic = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
				Matrix4f model = new Matrix4f().translateYTo(-panelPos.y()).translateXTo(-panelPos.x());
				l.set(TRANSFORMATION_UNIFORM, b, orthographic.multiply(model));
			});
		}
		if (isModified)
		{
			isModified = false;
			while (renderer.draw() == DrawResult.FAILED);
			window.update();
		}
		window.pollEvents();
	}
	
	public static void end()
	{
		renderer.close();
		RenderEngine.getVulkan().getWindow().hide();
		synchronized (MONITOR)
		{
			MONITOR.notify();
		}
		RenderEngine.terminate();
	}
}
