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

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.VertexAccumulator;
import com.alcadrial.renderengine.render.VertexAccumulator.VertexResult;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.UniformBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.window.Window;

@Test
public class Tests {
	
	private static final Object MONITOR = new Object();
	
	public static void main(String[] args)
	{
		Property.parseProperties(args, Properties.PROPERTIES);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		System.out.println("Render Engine initialization time = " + recordTime(() -> {
			if (!RenderEngine.create("Test", new Version(0, 0, 1, 0)).messenger(Tests::debugCallback).initialize()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		}, TimeUnit.MICROSECONDS) / 1000D + "ms");
		
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
		//GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);
		
		Thread t = new Thread(() -> {
			start();
			loop();
			end();
		});
		//t.start();
		
		System.out.println("Start time = " + recordTime(Tests::start, TimeUnit.MICROSECONDS) / 1000D + "ms");
		if (!RenderEngine.isInitializated()) return;
		System.out.println("Loop time = " + recordTime(Tests::loop, TimeUnit.MICROSECONDS) / 1000D + "ms");
		if (!RenderEngine.isInitializated()) return;
		System.out.println("End time = " + recordTime(Tests::end, TimeUnit.MICROSECONDS) / 1000D + "ms");
		
		Thread main = Thread.currentThread();
		new Thread(() -> {
			
			while (t.isAlive() || main.isAlive())
			{
				synchronized (MONITOR)
				{
					try
					{
						MONITOR.wait();
						synchronized (args)
						{
							args.wait(100);
						}
					}
					catch (InterruptedException e)
					{}
				}
			}
			
			RenderEngine.terminate();
		});//.start();
	}
	
	private static Renderer renderer;
	private static VertexBuffer vertexBuffer;
	private static IndexBuffer indexBuffer;
	private static float f;
	private static int i;
	
	public static void start()
	{
		Vulkan vulkan = RenderEngine.getVulkan();
		
		System.out.println("Window creation time = " + recordTime(() -> vulkan.setWindow(new Window(1080, 720, "Test")), TimeUnit.MICROSECONDS) / 1000D + "ms");
		
		vulkan.setPhysicalDeviceFilter(physicalDevice -> physicalDevice.getQueueGraphicsFamilyProperties() != null);
		vulkan.pickPhysicalDevices(PhysicalDevice.typeBestMatcher());
		vulkan.createDevice();
		
		Window window = vulkan.getWindow();
		window.assignDevice(vulkan.getDevice());
		
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
		
		Shader vertexShader;
		Shader fragmentShader;
		try
		{
			vertexShader = new Shader(getFile("shader.vert"), getFile("shader.vert.json"));
			if (!vertexShader.isCompilationSuccessfull()) throw new IOException("Shader " + vertexShader.getFile() + " has errors");
			fragmentShader = new Shader(getFile("shader.frag"), getFile("shader.frag.json"));
			if (!fragmentShader.isCompilationSuccessfull()) throw new IOException("Shader " + fragmentShader.getFile() + " has errors");
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		Builder builder = Renderer.create();
		builder.vertexShader(vertexShader).fragmentShader(fragmentShader);
		builder.cullMode(VK10.VK_CULL_MODE_NONE);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP);
		builder.lineWidth(2F);
		renderer = builder.build(window, vulkan.getDevice());
		//renderer.startModificationPlan().lineWidth(3F).submit();
		
		vertexBuffer = new VertexBuffer(56);
		indexBuffer = new IndexBuffer(15);
		renderer.setVertexBuffer(vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
		for (UniformBuffer uniformBuffer : renderer.getUniformBuffers())
		{
			uniformBuffer.set("TRANSFORMATION", new Matrix4f().orthographic(window.getWidth(), window.getHeight()));
			uniformBuffer.updateDescriptorSets();
		}
		
		VertexAccumulator vertexAccumulator = renderer.accumulate();
		VertexResult result0 = vertexAccumulator.vertex(-200, 0, 0).color(0, 255, 0, 255).end();
		VertexResult result1 = vertexAccumulator.vertex(0, 0, 0).color(255, 0, 0, 255).end();
		VertexResult result2 = vertexAccumulator.vertex(0, -200, 0).color(0, 255, 0, 255).end();
		VertexResult result3 = vertexAccumulator.vertex(-200, -200, 0).color(0, 0, 255, 255).end();
		VertexResult result4 = vertexAccumulator.vertex(-100, -100, 0).color(127, 127, 127, 255).end();
		VertexResult result5 = vertexAccumulator.vertex(0, 0, 0).color(0, 0, 255, 255).end();
		VertexResult result6 = vertexAccumulator.vertex(0, 200, 0).color(255, 255, 255, 255).end();
		VertexResult result7 = vertexAccumulator.vertex(200, 200, 0).color(255, 0, 0, 255).end();
		VertexResult result8 = vertexAccumulator.vertex(200, 0, 0).color(0, 255, 0, 255).end();
		VertexResult result9 = vertexAccumulator.vertex(100, 100, 0).color(127, 127, 127, 255).end();
		vertexBuffer.sendToGpu();
		
		indexBuffer.asIntBuffer().put(0, new int[] {
			result0.getIndex(), result1.getIndex(), result4.getIndex(),
			result1.getIndex(), result2.getIndex(), result4.getIndex(),
			result4.getIndex(), result2.getIndex(), result3.getIndex(),
			result0.getIndex(), result4.getIndex(), result3.getIndex(),
			
			result5.getIndex(), result6.getIndex(), result9.getIndex(),
			result6.getIndex(), result7.getIndex(), result9.getIndex(),
			result9.getIndex(), result7.getIndex(), result8.getIndex(),
			result5.getIndex(), result9.getIndex(), result8.getIndex(),
		});
		indexBuffer.sendToGpu();
		
		f = 1;
		i = 0;
		
		//Renderer.create().vertexShader(vertexShader).build(window, vulkan.getDevice()).close();
		//Renderer.create().vertexShader(vertexShader).build(window, vulkan.getDevice()).close();
		//Renderer.create().vertexShader(vertexShader).build(window, vulkan.getDevice()).close();
		//Renderer.create().vertexShader(vertexShader).build(window, vulkan.getDevice()).close();
		//Renderer.create().vertexShader(vertexShader).build(window, vulkan.getDevice()).close();
		//Renderer.create().vertexShader(vertexShader).build(window, vulkan.getDevice()).close();
	}
	
	public static void loop()
	{
		Window window = RenderEngine.getVulkan().getWindow();
		
		renderer.setClearColor(Color.of(0, 0, 0, 0));
		
		while (!window.shouldClose())
		{
			if (i++ == 10)
			{
				f += 0.1F;
				if (f > 5) f = 1;
				//renderer.startModificationPlan().lineWidth(f).submit();
				i = 0;
			}
			window.pollEvents();
			renderer.draw(null);
			window.update();
		}
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
	
	private static final String[] SEVERITIES = new String[] {"UNKNOWN", "VERBOSE", "INFO", "WARNING", "ERROR"};
	private static final String[] TYPES = new String[] {"UNKNOWN", "GENERAL", "VALIDATION", "PERFORMANCE"};
	
	public static int debugCallback(int messageSeverity, int messageType, long callbackDataId, long userDataId)
	{
		VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataId);
		
		int i = switch (messageSeverity)
		{
			case 0x0001 -> 1;
			case 0x0010 -> 2;
			case 0x0100 -> 3;
			case 0x1000 -> 4;
			default -> 0;
		};
		int j = switch (messageType)
		{
			case 0b001 -> 1;
			case 0b010 -> 2;
			case 0b100 -> 3;
			default -> 0;
		};
		
		System.out.println("[Vulkan debug/" + SEVERITIES[i] + " " + TYPES[j] + "] " + callbackData.pMessageString());
		
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
