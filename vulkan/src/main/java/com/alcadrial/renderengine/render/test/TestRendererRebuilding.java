package com.alcadrial.renderengine.render.test;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowIconifyCallbackI;
import org.lwjgl.glfw.GLFWWindowMaximizeCallbackI;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Renderer.ModificationPlan;
import com.alcadrial.renderengine.render.VertexAccumulator;
import com.alcadrial.renderengine.render.VertexAccumulator.VertexResult;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.window.Presenter;
import com.alcadrial.renderengine.render.window.Presenter.Result;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class TestRendererRebuilding extends RenderTest {
	
	private static Object WARNING;
	private static final String SHADER = "shader";
	
	public static void main(String[] args)
	{
		Property.parseProperties(args, Properties.PROPERTIES);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		println("Render Engine initialization", RenderTest::initMultiThreadRenderEngine, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		
		Thread t = new Thread(() -> {
			Logic logic = new Logic();
			logic.start();
			logic.loop();
			logic.end();
		});
		t.start();
		
		Thread t0 = new Thread(() -> {
			Logic logic = new Logic();
			logic.start();
			logic.loop();
			logic.end();
		});
		t0.start();
		
		new Thread(() -> {
			
			while (t.isAlive() || t0.isAlive())
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
		}).start();
		
		RenderEngine.serveRenderEngine();
	}
	
	public static final class Logic {
		
		private Presenter presenter;
		private Renderer renderer;
		private VertexBuffer vertexBuffer;
		private IndexBuffer indexBuffer;
		private float f;
		private int i, j, k, l;
		private int[] indices;
		
		public void start()
		{
			ShaderProgram shaderProgram;
			try
			{
				shaderProgram = ShaderProgram.create().add(loadShader(SHADER + ".vert"), loadShader(SHADER + ".frag")).build();
			}
			catch (IOException e)
			{
				e.printStackTrace(System.out);
				RenderEngine.terminate();
				return;
			}
			
			Vulkan vulkan = RenderEngine.getVulkan();
			
			RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
			RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			
			VulkanWindow window = new VulkanWindow(1080, 720, "Test");
			vulkan.setWindow(window);
			if (!pickPhysicalDevice(window)) return;
			
			VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
			deviceFeatures.wideLines(true);
			Device device = vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);
			
			window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> {
				System.out.println(iconified ? "Iconified" : "Restored");
			});
			window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
				System.out.println("Resized " + width + "x" + height);
				renderer.updateUniformBuffers((b, l) -> l.set("TRANSFORMATION", b, new Matrix4f().orthographic(window.getWidth(), window.getHeight())));
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
			
			presenter = new Presenter(device, window, null, 3);
			
			Builder builder = Renderer.create();
			builder.shaderProgram(shaderProgram);
			builder.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
			builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP);
			builder.lineWidth(2F);
			builder.uniformBuffer(new VulkanBuffer(device, 64, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT));
			builder.dynamicViewport(false).dynamicScissor(false);
			renderer = builder.build(device, window.getSurface().getSize(), presenter.getRenderPass(), 3);
			
			vertexBuffer = new VertexBuffer(device, 70);
			indexBuffer = new IndexBuffer(device, 24);
			renderer.addVertexBuffer(0, vertexBuffer);
			renderer.setIndexBuffer(indexBuffer);
			renderer.updateUniformBuffers((b, l) -> l.set("TRANSFORMATION", b, new Matrix4f().orthographic(window.getWidth(), window.getHeight())));
			
			VertexAccumulator vertexAccumulator = renderer.accumulate(vertexBuffer);
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
			vertexBuffer.sendToPhysicalDevice();
			
			indices = new int[] {
				result0.getIndex(), result1.getIndex(), result4.getIndex(),
				result1.getIndex(), result2.getIndex(), result4.getIndex(),
				result4.getIndex(), result2.getIndex(), result3.getIndex(),
				result0.getIndex(), result4.getIndex(), result3.getIndex(),
				
				result5.getIndex(), result6.getIndex(), result9.getIndex(),
				result6.getIndex(), result7.getIndex(), result9.getIndex(),
				result9.getIndex(), result7.getIndex(), result8.getIndex(),
				result5.getIndex(), result9.getIndex(), result8.getIndex(),
			};
			
			indexBuffer.asIntBuffer().put(0, indices);
			indexBuffer.sendToPhysicalDevice();
			
			f = 1;
			i = j = k = l = 0;
			
		}
		
		private long time;
		
		public void loop()
		{
			VulkanWindow window = RenderEngine.getVulkan().getWindow();
			
			presenter.setClearColor(Color.of(0, 0, 0, 0));
			
			int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
			long firstHighMilestone = 1000000000;
			long firstMilestone = 1000000000 / maxfps;
			
			time = System.nanoTime();
			long current;
			long frame = 0;
			long milestone = firstMilestone;
			long highMilestone = firstHighMilestone;
			
			ModificationPlan modificationPlan = renderer.getCurrentModificationPlan();
			boolean modified = false;
			
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
				
				if (i++ == 5)
				{
					f += 0.25F;
					if (f > 50) f = 1;
					modificationPlan.lineWidth(f);
					modified = true;
					i = 0;
				}
				if (j++ % 10 == 0)
				{
					if (k % 16 >= 8) indexBuffer.asIntBuffer().put(k % 8 * 3, indices, k % 8 * 3, 3);
					else indexBuffer.asIntBuffer().put(k % 8 * 3, new int[] {0, 0, 0}, 0, 3);
					indexBuffer.sendToPhysicalDevice(k % 8 * 3, 3);
					k++;
				}
				if (++l % 25 == 0)
				{
					modificationPlan.topology(l % 50 == 0 ? VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST : VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP);
					modified = true;
				}
				if (modified)
				{
					renderer.startModificationPlan().submit();
				}
				window.pollEvents();
				while (presenter.present((commandBuffer, frameIndex) -> {
					presenter.beginRenderPass(commandBuffer);
					renderer.setUniformBufferIndex(frameIndex);
					renderer.draw(commandBuffer);
					commandBuffer.endRenderPass();
				}, () -> {}) == Result.SWAPCHAIN_OUT_OF_DATE);
				window.update();
				frame++;
			}
		}
		
		public void end()
		{
			renderer.close();
			presenter.close();
			RenderEngine.getVulkan().getWindow().close();
			synchronized (MONITOR)
			{
				MONITOR.notify();
			}
		}
		
		public static File getFile(String path)
		{
			return new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/" + path);
		}
	}
}
