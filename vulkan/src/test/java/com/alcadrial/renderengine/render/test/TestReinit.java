package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.List;

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
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
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
		
		for (int i = 0; i < 100; i++)
		{
			try
			{
				System.out.println("Iteration " + i);
				println("Render Engine initialization", RenderTest::initRenderEngine, TestProperties.PRINT_LOADING_TIMES);
				if (!RenderEngine.isInitializated()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
				
				logic.load();
				logic.start();
				logic.loop();
				logic.end();
				RenderEngine.terminate();
			}
			catch (VulkanException | IOException e)
			{
				e.printStackTrace(System.out);
			}
		}
	}
	
	public static final class Logic {
		
		private ShaderProgram shaderProgram;
		private RenderPass renderPass;
		private Renderer renderer;
		private StaticBigBuffer staticBigBuffer;
		private VulkanBuffer vertexBuffer;
		private VulkanBuffer indexBuffer;
		private VulkanBuffer uniformBuffer;
		private VulkanBuffer localUniformBuffer;
		private List<CommandBuffer> commandBuffers;
		private int i;
		private int[] indices;
		
		public void load() throws IOException
		{
			shaderProgram = ShaderProgram.create().add(
					loadShader(SHADER + ".vert"),
					loadShader(SHADER + ".frag"))
				.build();
		}
		
		public void start() throws VulkanException
		{
			RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
			RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			
			println("Window init", this::initWindow, TestProperties.PRINT_LOADING_TIMES);
			println("Buffers and Renderer init", this::initBuffersAndRenderer, TestProperties.PRINT_LOADING_TIMES);
			
			commandBuffers = RenderEngine.getVulkan().getDevice().createMultiUseCommandBuffer(3);
			for (int i = 0; i < commandBuffers.size(); i++) commandBuffers.get(i).setName("CommandBuffer" + i);
			i = 0;
		}
		
		private void initWindow() throws VulkanException
		{
			Vulkan vulkan = RenderEngine.getVulkan();
			
			VulkanWindow window = new VulkanWindow(1080, 720, "Test");
			vulkan.setWindow(window);
			window.setUseDepthTexture(false);
			if (!pickPhysicalDevice(window)) throw new VulkanException("Physical device not found");
			VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
			deviceFeatures.wideLines(true);
			Device device = vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);
			window.init(device);
			
			window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> {
				System.out.println(iconified ? "Iconified" : "Restored");
			});
			window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
				System.out.println("Resized " + width + "x" + height);
				//		renderer.updateUniforms(uniformBuffer, l -> l.set("TRANSFORMATION", uniformBuffer, new Matrix4f().orthographic(window.getWidth(), window.getHeight())));
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
		}
		
		private void initBuffersAndRenderer() throws VulkanException
		{
			Vulkan vulkan = RenderEngine.getVulkan();
			Device device = vulkan.getDevice();
			VulkanWindow window = vulkan.getWindow();
			
			staticBigBuffer = new StaticBigBuffer(device, 65536);
			
			vertexBuffer = staticBigBuffer.allocate(70 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
			indexBuffer = staticBigBuffer.allocate(24 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
			uniformBuffer = staticBigBuffer.allocate(16 * 4, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
			localUniformBuffer = new VulkanBuffer(device, uniformBuffer.getSize(), VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT | VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT);
			localUniformBuffer.getMemory().mapMemory();
			
			staticBigBuffer.setName("StaticBigBuffer");
			vertexBuffer.setName("VertexBuffer");
			indexBuffer.setName("IndexBuffer");
			uniformBuffer.setName("UniformBuffer");
			localUniformBuffer.setName("LocalUniformBuffer");
			
			renderPass = RenderPassTemplate.initColorTextureTemplate(window.getSurface().getFormat().format()).createRenderPass(device);
			
			Builder builder = Renderer.create();
			builder.shaderProgram(shaderProgram);
			builder.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
			builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP);
			builder.lineWidth(2F);
			builder.dynamicViewport(false).dynamicScissor(false);
			builder.bufferBindings(new BufferWriter(localUniformBuffer, 0, uniformBuffer, 0, uniformBuffer.getSize()));
			renderer = builder.build(device, window.getSurface().getSize(), renderPass);
			renderer.setVertexBuffers(0, vertexBuffer);
			renderer.setIndexBuffer(indexBuffer);
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), b -> {
				renderer.updateUniform("TRANSFORMATION", l -> {
					l.set(new Matrix4f().orthographic(window.getWidth(), window.getHeight()));
				}, b);
			});
			renderer.setName("Renderer");
			
			try (PhysicalDeviceTranferState transfert = staticBigBuffer.startLocalBuffering())
			{
				BufferWriter vertexBufferWriter = transfert.beginTransfer(vertexBuffer);
				BufferWriter indexBufferWriter = transfert.beginTransfer(indexBuffer);
				
				vertexBufferWriter.put(-200F, 0, 0).put(0F, 1, 0, 1);
				vertexBufferWriter.put(0F, 0, 0).put(1F, 0, 0, 1);
				vertexBufferWriter.put(0F, -200, 0).put(0F, 1, 0, 1);
				vertexBufferWriter.put(-200F, -200, 0).put(0F, 0, 1, 1);
				vertexBufferWriter.put(-100F, -100, 0).put(0.5F, 0.5F, 0.5F, 1);
				vertexBufferWriter.put(0F, 0, 0).put(0F, 0, 1, 1);
				vertexBufferWriter.put(0F, 200, 0).put(1F, 1, 1, 1);
				vertexBufferWriter.put(200F, 200, 0).put(1F, 0, 0, 1);
				vertexBufferWriter.put(200F, 0, 0).put(0, 1F, 0, 1);
				vertexBufferWriter.put(100F, 100, 0).put(0.5F, 0.5F, 0.5F, 1);
				
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
			}
		}
		
		public void loop() throws VulkanException
		{
			RenderTest.defaultLoop((enlapsed, enlapsedFrame) -> {
				
				if (i++ == 5) RenderEngine.getVulkan().getWindow().setShouldClose(true);
				
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					VulkanWindow window = RenderEngine.getVulkan().getWindow();
					FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
					CommandBuffer commandBuffer = commandBuffers.get(window.getFrameIndex());
					
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
				}
			});
		}
		
		public void end() throws VulkanException
		{
			renderer.close();
			localUniformBuffer.close();
			staticBigBuffer.close();
			for (CommandBuffer commandBuffer : commandBuffers) commandBuffer.close();
			RenderEngine.getVulkan().getWindow().close();
		}
	}
}
