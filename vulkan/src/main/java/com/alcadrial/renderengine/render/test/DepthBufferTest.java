package com.alcadrial.renderengine.render.test;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.IntBuffer;

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

import com.alcadrial.kector.angle.Angle;
import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.axis.Axis;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Renderer.DrawResult;
import com.alcadrial.renderengine.render.VertexAccumulator.VertexResult;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.test.RendererUtils.OrthoCubeVertexResult;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class DepthBufferTest extends RenderTest {
	
	private static final String SHADER = "shader1";
	private static final String PROJECTION = "PROJECTION";
	private static final String MODEL = "MODEL";
	private static final String LIGHT_POS = "LIGHT_POS";
	private static final String LIGHT_INTENSITY = "LIGHT_INTENSITY";
	private static final String SPACE_FOG_INTENSITY = "SPACE_FOG_INTENSITY";
	
	public static void main(String[] args)
	{
		TestProperties.init(args);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		println("Render Engine initialization", () -> {
			if (!RenderEngine.create("Test", new Version(0, 0, 1, 0)).messenger(DepthBufferTest::debugCallback).initialize()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		}, TestProperties.PRINT_LOADING_TIMES);
		
		println("Start", DepthBufferTest::start, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		println("Loop", DepthBufferTest::loop, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		println("End", DepthBufferTest::end, TestProperties.PRINT_LOADING_TIMES);
	}
	
	private static Renderer renderer;
	private static VertexBuffer vertexBuffer;
	private static IndexBuffer indexBuffer;
	private static Texture depthTexture;
	private static Vector3f lightPos;
	private static double f;
	
	private static Matrix4f buildProjectionMatrix(VulkanWindow window)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().perspective(Angle.of(90, AngleUnit.DEGREES), 0.09F, -0.1F, (float) window.getWidth() / window.getHeight()));
		matrix.multiply(new Matrix4f().view(Vector3f.of(0, 0, -1), Angle.V0_DEG, Angle.V0_DEG));
		return matrix.transpose();
	}
	
	private static Matrix4f buildModelMatrix()
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().rotate(Axis.Z, Angle.of(f / 11, AngleUnit.DEGREES)));
		matrix.multiply(new Matrix4f().rotate(Axis.Y, Angle.of(f / 17, AngleUnit.DEGREES)));
		matrix.multiply(new Matrix4f().rotate(Axis.X, Angle.of(f / 13, AngleUnit.DEGREES)));
		matrix.multiply(new Matrix4f().scale(Vector3f.of(1, 1, 1).divideAll(2)));
		return matrix;
	}
	
	public static void start()
	{
		Vulkan vulkan = RenderEngine.getVulkan();
		
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		println("Window creation", () -> vulkan.setWindow(new VulkanWindow(1080, 720, "Test")), TestProperties.PRINT_LOADING_TIMES);
		
		vulkan.setPhysicalDeviceFilter(physicalDevice -> physicalDevice.getGraphicsQueueFamily() != null);
		if (TestProperties.DEVICE.isDefault()) vulkan.pickPhysicalDevices(PhysicalDevice.typeBestMatcher());
		else vulkan.pickPhysicalDevices(TestProperties.DEVICE.getValue());
		
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
		deviceFeatures.wideLines(true);
		deviceFeatures.samplerAnisotropy(true);
		vulkan.createDevice(deviceFeatures);
		
		VulkanWindow window = vulkan.getWindow();
		Device device = vulkan.getDevice();
		window.assignDevice(device);
		
		window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> {
			System.out.println(iconified ? "Iconified" : "Restored");
		});
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			System.out.println("Resized " + width + "x" + height);
			renderer.updateUniformBuffers((b, l) -> l.set(PROJECTION, b, buildProjectionMatrix(window)));
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
		
		window.setCallback((GLFWKeyCallbackI) (w, k, s, a, m) -> {
			if (k == GLFW.GLFW_KEY_R)
			{
				renderer.updateUniformBuffers((b, l) -> {
					l.set(PROJECTION, b, buildProjectionMatrix(window));
					l.set(MODEL, b, buildModelMatrix());
					l.set(LIGHT_POS, b, lightPos);
					l.set(LIGHT_INTENSITY, b, 0.8F);
					l.set(SPACE_FOG_INTENSITY, b, 0.4F);
				});
			}
		});
		
		Shader vertexShader;
		ShaderInfo vertexShaderInfo;
		Shader fragmentShader;
		ShaderInfo fragmentShaderInfo;
		try
		{
			vertexShader = new Shader(getFile(SHADER + ".vert"));
			vertexShaderInfo = ShaderInfo.fromJson(getFile(SHADER + ".vert.json"));
			vertexShader.compile(vertexShaderInfo);
			if (!vertexShader.isCompilationSuccessfull()) throw new IOException("Shader " + vertexShader.getFile() + " has errors");
			
			fragmentShader = new Shader(getFile(SHADER + ".frag"));
			fragmentShaderInfo = ShaderInfo.fromJson(getFile(SHADER + ".frag.json"));
			fragmentShader.compile(fragmentShaderInfo);
			if (!fragmentShader.isCompilationSuccessfull()) throw new IOException("Shader " + fragmentShader.getFile() + " has errors");
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		depthTexture = Texture.create2dTexture(device, window.getWidth(), window.getHeight(), VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
		depthTexture.changeLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		
		Builder builder = Renderer.create();
		builder.vertexShader(vertexShader, vertexShaderInfo).fragmentShader(fragmentShader, fragmentShaderInfo);
		builder.depthTexture(depthTexture);
		builder.depthTestEnable(true);
		builder.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		builder.uniformBuffer(new VulkanBuffer(device, Math.max(256 * 2, (16 + 16 + 3 + 1 + 1) * 4), VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT));
		renderer = builder.build(window, device);
		
		lightPos = Vector3f.of(0.3F, 1, 0);
		f = 0;
		
		vertexBuffer = new VertexBuffer(240);
		indexBuffer = new IndexBuffer(24);
		renderer.addVertexBuffer(0, vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
		renderer.updateUniformBuffers((b, l) -> {
			l.set(PROJECTION, b, buildProjectionMatrix(window));
			l.set(MODEL, b, buildModelMatrix());
			l.set(LIGHT_POS, b, lightPos);
			l.set(LIGHT_INTENSITY, b, 0.8F);
			l.set(SPACE_FOG_INTENSITY, b, 0.4F);
		});
		
		Color[] colors = new Color[] {
			Color.VERY_LIGHT_YELLOW,
			Color.VERY_LIGHT_RED,
			Color.VERY_LIGHT_MAGENTA,
			Color.VERY_LIGHT_GREEN,
			Color.VERY_LIGHT_CYAN,
			Color.VERY_LIGHT_BLUE
		};
		
		OrthoCubeVertexResult result = RendererUtils.loadOrthoCube(renderer, vertexBuffer, Vector3f.of(-1, -1, -1), Vector3f.of(1, 1, 1), (i, j) -> colors[i]);
		
		IntBuffer buffer = indexBuffer.asIntBuffer();
		result.foreach(rResult -> {
			VertexResult result0 = rResult.getR0();
			VertexResult result1 = rResult.getR1();
			VertexResult result2 = rResult.getR2();
			VertexResult result3 = rResult.getR3();
			
			buffer.put(new int[] {
				result0.getIndex(), result1.getIndex(), result3.getIndex(),
				result1.getIndex(), result2.getIndex(), result3.getIndex(),
			});
		});
		indexBuffer.sendToPhysicalDevice();
	}
	
	private static long time;
	
	public static void loop()
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		renderer.setClearColor(Color.of(0, 0, 0, 0));
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
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
			println("Frame " + frame, () -> {
				DrawResult result;
				do
				{
					window.pollEvents();
					result = renderer.draw();
					window.update();
				} while (result == DrawResult.FAILED);
				renderer.updateUniformBuffers((b, l) -> l.set(MODEL, b, buildModelMatrix()));
				f += (firstMilestone - pauseTime) / 1000000D;
			}, TestProperties.PRINT_FRAME_TIMES);
			frame++;
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
	
	public static File getFile(String path)
	{
		return new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/" + path);
	}
}
