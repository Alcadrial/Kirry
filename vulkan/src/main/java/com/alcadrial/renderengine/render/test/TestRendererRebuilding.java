package com.alcadrial.renderengine.render.test;

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
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
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
import com.alcadrial.renderengine.render.buffer.VulkanBufferRange;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.util.Properties;
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
		
		private RenderPass renderPass;
		private Renderer renderer;
		private VertexBuffer vertexBuffer;
		private IndexBuffer indexBuffer;
		private VulkanBuffer uniformBuffer;
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
			window.setUseDepthTexture(false);
			if (!pickPhysicalDevice(window)) return;
			
			VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
			deviceFeatures.wideLines(true);
			Device device = vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);
			window.init(device);
			
			window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> {
				System.out.println(iconified ? "Iconified" : "Restored");
			});
			window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
				System.out.println("Resized " + width + "x" + height);
				renderer.updateUniforms(uniformBuffer, l -> l.set("TRANSFORMATION", uniformBuffer, new Matrix4f().orthographic(window.getWidth(), window.getHeight())));
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
			
			uniformBuffer = new VulkanBuffer(device, 64, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
			
			Builder builder = Renderer.create();
			builder.shaderProgram(shaderProgram);
			builder.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
			builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP);
			builder.lineWidth(2F);
			builder.bufferBindings(new VulkanBufferRange(uniformBuffer, 0));
			builder.dynamicViewport(false).dynamicScissor(false);
			
			renderPass = RenderPassTemplate.initColorTextureTemplate(window.getSurface().getFormat().format()).createRenderPass(device);
			renderer = builder.build(device, window.getSurface().getSize(), renderPass);
			
			vertexBuffer = new VertexBuffer(device, 70);
			indexBuffer = new IndexBuffer(device, 24);
			renderer.addVertexBuffer(0, vertexBuffer.getGpuBuffer());
			renderer.setIndexBuffer(indexBuffer.getGpuBuffer());
			renderer.updateUniforms(uniformBuffer, l -> l.set("TRANSFORMATION", uniformBuffer, new Matrix4f().orthographic(window.getWidth(), window.getHeight())));
			
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
		
		public void loop()
		{
			VulkanWindow window = RenderEngine.getVulkan().getWindow();
			
			int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
			long firstHighMilestone = 1000000000;
			long firstMilestone = 1000000000 / maxfps;
			
			long time = System.nanoTime();
			long current;
			long frame = 0;
			long milestone = firstMilestone;
			long highMilestone = firstHighMilestone;
			
			ModificationPlan modificationPlan = renderer.getCurrentModificationPlan();
			boolean modified = false;
			while (!window.shouldClose())
			{
				
				try (MemoryStack stack = MemoryStack.stackPush())
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
					
					CommandBuffer commandBuffer = window.getDevice().createSingleUseCommandBuffer(stack);
					
					VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
					renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
					renderPassBeginInfo.pNext(0);
					
					renderPassBeginInfo.renderPass(renderPass.getVk());
					
					FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
					renderPassBeginInfo.framebuffer(frameBuffer.getVk());
					
					VkRect2D renderArea = VkRect2D.calloc(stack);
					renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
					renderPassBeginInfo.renderArea(renderArea);
					
					VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
					clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
					clearValues.get().depthStencil().depth(1).stencil(0);
					renderPassBeginInfo.pClearValues(clearValues.rewind());
					
					commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
					renderer.draw(commandBuffer);
					commandBuffer.endRenderPass();
					commandBuffer.endRecording();
					
					window.swapBuffers(commandBuffer);
					window.pollEvents();
					window.update();
					frame++;
				}
			}
		}
		
		public void end()
		{
			renderer.close();
			RenderEngine.getVulkan().getWindow().close();
			synchronized (MONITOR)
			{
				MONITOR.notify();
			}
		}
	}
}
