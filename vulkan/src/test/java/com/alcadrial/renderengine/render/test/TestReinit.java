package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.math.RoundingMode;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugMarker;
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
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.BigVulkanBuffer.PhysicalDeviceTranferState;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.util.OutOfSpaceException;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class TestReinit extends RenderTest {
	
	private static final String SHADER = "shader";
	
	public static void main(String[] args)
	{
		Property.parseProperties(args, Properties.PROPERTIES);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		Logic logic = new Logic();
		
		for (int i = 0; i < 1000; i++)
		{
			try
			{
				System.out.println("Iteration " + i);
				println("Render Engine initialization", RenderTest::initRenderEngine, TestProperties.PRINT_LOADING_TIMES);
				if (!RenderEngine.isInitializated()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
				
				logic.start();
				logic.loop();
				logic.end();
				RenderEngine.terminate();
			}
			catch (VulkanException e)
			{
				e.printStackTrace(System.out);
			}
		}
	}
	
	public static final class Logic {
		
		private RenderPass renderPass;
		private Renderer renderer;
		private StaticBigBuffer staticBigBuffer;
		private VulkanBuffer vertexBuffer;
		private VulkanBuffer indexBuffer;
		private VulkanBuffer uniformBuffer;
		private CommandBuffer commandBuffer;
		private int[] indices;
		
		public void start() throws VulkanException
		{
			ShaderProgram shaderProgram;
			try
			{
				shaderProgram = ShaderProgram.create().add(loadShader(SHADER + ".vert"), loadShader(SHADER + ".frag")).build();
			}
			catch (IOException e)
			{
				throw new VulkanException(e);
			}
			
			Vulkan vulkan = RenderEngine.getVulkan();
			
			RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
			RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			
			VulkanWindow window = new VulkanWindow(1080, 720, "Test");
			window.setPos(Vector2i.of(50, 50));
			vulkan.setWindow(window);
			window.setUseDepthTexture(false);
			if (!pickPhysicalDevice(window)) return;
			
			VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
			deviceFeatures.wideLines(true);
			Device device = Debug.get(
					() -> vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME, EXTDebugMarker.VK_EXT_DEBUG_MARKER_EXTENSION_NAME),
					() -> vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME));
			window.init(device);
			
			window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
				System.out.println("Resized " + width + "x" + height);
				//		renderer.updateUniforms(uniformBuffer, l -> l.set("TRANSFORMATION", uniformBuffer, new Matrix4f().orthographic(window.getWidth(), window.getHeight())));
			});
			
			Vector2i[] oldPos = new Vector2i[] {Vector2i.of(0, 0)};
			Vector2f[] oldMousePos = new Vector2f[] {Vector2f.of(0, 0), Vector2f.of(0, 0)};
			boolean[] pressing = new boolean[2];
			window.setCallback((GLFWMouseButtonCallbackI) (w, button, action, mods) -> {
				if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
				{
					if (pressing[0] = action == GLFW.GLFW_PRESS)
					{
						window.getMousePos(oldMousePos[0]);
					}
				}
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
			
			staticBigBuffer = new StaticBigBuffer(device, 65536);
			
			vertexBuffer = staticBigBuffer.allocate(70 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
			indexBuffer = staticBigBuffer.allocate(24 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
			uniformBuffer = staticBigBuffer.allocate(16 * 4, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
			
			Builder builder = Renderer.create();
			builder.shaderProgram(shaderProgram);
			builder.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
			builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP);
			builder.lineWidth(2F);
			builder.dynamicViewport(false).dynamicScissor(false);
			
			try (PhysicalDeviceTranferState transfert = staticBigBuffer.startLocalBuffering())
			{
				BufferWriter vertexBufferWriter = transfert.beginTransfer(vertexBuffer);
				BufferWriter indexBufferWriter = transfert.beginTransfer(indexBuffer);
				BufferWriter uniformBufferWriter = transfert.beginTransfer(uniformBuffer);
				
				vertexBufferWriter.put(-200, 0, 0).put(0, 255, 0, 255);
				vertexBufferWriter.put(0, 0, 0).put(255, 0, 0, 255);
				vertexBufferWriter.put(0, -200, 0).put(0, 255, 0, 255);
				vertexBufferWriter.put(-200, -200, 0).put(0, 0, 255, 255);
				vertexBufferWriter.put(-100, -100, 0).put(127, 127, 127, 255);
				vertexBufferWriter.put(0, 0, 0).put(0, 0, 255, 255);
				vertexBufferWriter.put(0, 200, 0).put(255, 255, 255, 255);
				vertexBufferWriter.put(200, 200, 0).put(255, 0, 0, 255);
				vertexBufferWriter.put(200, 0, 0).put(0, 255, 0, 255);
				vertexBufferWriter.put(100, 100, 0).put(127, 127, 127, 255);
				
				indices = new int[] {
					0, 1, 4,
					1, 2, 4,
					4, 2, 3,
					0, 4, 3,
					
					5, 6, 9,
					6, 7, 9,
					9, 7, 8,
					5, 9, 8,
				};
				
				indexBufferWriter.put(indices);
				
				builder.bufferBindings(uniformBufferWriter);
				
				renderPass = RenderPassTemplate.initColorTextureTemplate(window.getSurface().getFormat().format()).createRenderPass(device);
				renderer = builder.build(device, window.getSurface().getSize(), renderPass);
				renderer.setVertexBuffers(0, vertexBuffer);
				renderer.setIndexBuffer(indexBuffer);
				renderer.updateUniforms(l -> {
					l.set("TRANSFORMATION", new Matrix4f().orthographic(window.getWidth(), window.getHeight()));
					device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), l::flush);
				});
			}
			catch (OutOfSpaceException e)
			{
				throw new VulkanException(e);
			}
			
			commandBuffer = device.createMultiUseCommandBuffer();
			
		}
		
		public void loop() throws VulkanException
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
					
					FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
					
					VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
					renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
					renderPassBeginInfo.pNext(0);
					renderPassBeginInfo.renderPass(renderPass.getVk());
					renderPassBeginInfo.framebuffer(frameBuffer.getVk());
					
					VkRect2D renderArea = VkRect2D.calloc(stack);
					renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
					renderPassBeginInfo.renderArea(renderArea);
					
					VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
					clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
					clearValues.get().depthStencil().depth(1).stencil(0);
					renderPassBeginInfo.pClearValues(clearValues.rewind());
					
					commandBuffer.beginRecording();
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
		}
	}
}
