package com.alcadrial.renderengine.render.test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowIconifyCallbackI;
import org.lwjgl.glfw.GLFWWindowMaximizeCallbackI;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
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
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.test.RendererUtils.RectangleVertexResult;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class TextureTest extends RenderTest {
	
	private static final String SHADER = "shader0";
	private static final String TRANSFORMATION_UNIFORM = "TRANSFORMATION";
	private static final String TEXTURE_SAMPLER_UNIFORM = "TEXTURE_SAMPLER";
	
	public static void main(String[] args)
	{
		TestProperties.init(args);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		println("Render Engine initialization", () -> {
			if (!RenderEngine.create("Test", new Version(0, 0, 1, 0)).messenger(TextureTest::debugCallback).initialize()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		}, TestProperties.PRINT_LOADING_TIMES);
		
		println("Start", TextureTest::start, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		println("Loop", TextureTest::loop, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		println("End", TextureTest::end, TestProperties.PRINT_LOADING_TIMES);
	}
	
	private static Renderer renderer;
	private static Texture texture;
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
		deviceFeatures.samplerAnisotropy(true);
		vulkan.createDevice(deviceFeatures);
		Device device = vulkan.getDevice();
		
		VulkanWindow window = vulkan.getWindow();
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
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vertexShader = new Shader(getFile(SHADER + ".vert"));
			vertexShaderInfo = ShaderInfo.fromJson(getFile(SHADER + ".vert.json"));
			vertexShader.compile(vertexShaderInfo);
			if (!vertexShader.isCompilationSuccessfull()) throw new IOException("Shader " + vertexShader.getFile() + " has errors");
			
			fragmentShader = new Shader(getFile(SHADER + ".frag"));
			fragmentShaderInfo = ShaderInfo.fromJson(getFile(SHADER + ".frag.json"));
			fragmentShader.compile(fragmentShaderInfo);
			if (!fragmentShader.isCompilationSuccessfull()) throw new IOException("Shader " + fragmentShader.getFile() + " has errors");
			
			BufferedImage image = ImageIO.read(getFile("icon.png"));
			
			texture = Texture.create2dTexture(device, image.getWidth(), image.getHeight(), VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			
			VkCommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			IntBuffer buffer = texture.getBuffer().asIntBuffer();
			for (int element : image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth())) buffer.put(element);
			texture.sendToPhysicalDevice(commandBuffer);
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			
			device.submitSingleUseCommandBuffer(stack, commandBuffer);
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
		builder.uniformBuffer(new VulkanBuffer(device, 64, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT));
		renderer = builder.build(window, device);
		
		vertexBuffer = new VertexBuffer(28 * 4);
		indexBuffer = new IndexBuffer(6 * 4);
		renderer.addVertexBuffer(0, vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
		
		RectangleVertexResult result = RendererUtils.sendRectangleXYZUV(renderer, vertexBuffer, -256, -256, 256, 256, 0, 0, 0, 1, 1);
		
		IntBuffer buffer = indexBuffer.asIntBuffer();
		buffer.put(result.getR0().getIndex());
		buffer.put(result.getR1().getIndex());
		buffer.put(result.getR3().getIndex());
		buffer.put(result.getR1().getIndex());
		buffer.put(result.getR2().getIndex());
		buffer.put(result.getR3().getIndex());
		indexBuffer.sendToPhysicalDevice();
		
		renderer.updateUniformBuffers((b, l) -> l.set(TEXTURE_SAMPLER_UNIFORM, texture.getView(), new TextureSampler(device)));
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
			println("Frame " + frame, TextureTest::frame, TestProperties.PRINT_FRAME_TIMES);
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
				Matrix4f model = new Matrix4f().translateYTo(-10 * panelPos.y()).translateXTo(-10 * panelPos.x());
				l.set(TRANSFORMATION_UNIFORM, b, orthographic.multiply(model).transpose());
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
