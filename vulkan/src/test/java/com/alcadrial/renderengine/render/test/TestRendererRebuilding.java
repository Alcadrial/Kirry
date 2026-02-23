package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.util.List;

import org.lwjgl.glfw.GLFW;
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
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Renderer.ModificationPlan;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.AbstractBigVulkanBuffer.PhysicalDeviceTransferState;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.RenderingDataUtils;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class TestRendererRebuilding extends RenderTest {
	
	private static final String SHADER = "shader";
	
	public static void main(String[] args)
	{
		TestProperties.init(args);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		try
		{
			println("Render Engine initialization", RenderTest::initMultiThreadRenderEngine, TestProperties.PRINT_LOADING_TIMES);
			if (Properties.DEBUG.getValue()) Debug.setEnabledAllDebug(true);
		}
		catch (VulkanException e)
		{
			e.printStackTrace(System.out);
			return;
		}
		
		incrementalIdTest();
		
		Runnable task = () -> {
			try
			{
				Logic logic = new Logic();
				logic.load();
				logic.start();
				logic.loop();
				logic.end();
			}
			catch (VulkanException | IOException e)
			{
				e.printStackTrace(System.out);
			}
		};
		
		Thread t = new Thread(task);
		Thread t0 = new Thread(task);
		
		t.start();
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
	
	public static void incrementalIdTest()
	{
		Runnable obji = TestRendererRebuilding::incrementalIdTest;
		for (int i = 0; i < 10; i++)
		{
			System.out.println("Id " + Debug.getIncrementalId(obji));
			incrementalIdTest0();
		}
	}
	
	public static void incrementalIdTest0()
	{
		Runnable obj = TestRendererRebuilding::incrementalIdTest0;
		for (int j = 0; j < 10; j++)
		{
			System.out.println("Jd " + Debug.getIncrementalId(obj));
		}
	}
	
	public static final class Logic extends RenderTest.Logic {
		
		private ShaderProgram shaderProgram;
		private RenderPass renderPass;
		private Renderer renderer;
		private StaticBigBuffer staticBigBuffer;
		private VulkanBuffer vertexBuffer;
		private VulkanBuffer indexBuffer;
		private VulkanBuffer uniformBuffer;
		private VulkanBuffer localUniformBuffer;
		private List<CommandBuffer> commandBuffers;
		private float f;
		private int i, j, k, l;
		private int[] indices;
		
		@Override
		public void load() throws IOException
		{
			shaderProgram = ShaderProgram.create().add(
					loadShader(SHADER + ".vert"),
					loadShader(SHADER + ".frag"))
				.build();
		}

		@Override
		public void start() throws VulkanException
		{
			RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
			RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			
			println("Window init", this::initWindow, TestProperties.PRINT_LOADING_TIMES);
			println("Buffers and Renderer init", this::initBuffersAndRenderer, TestProperties.PRINT_LOADING_TIMES);
			
			commandBuffers = RenderEngine.getVulkan().getDevice().createMultiUseCommandBuffer(3);
			for (int i = 0; i < commandBuffers.size(); i++) commandBuffers.get(i).setName("CommandBuffer" + i);
			f = 1;
			i = j = k = l = 0;
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
			window.setName("Window");
			
			window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
				System.out.println("Resized " + width + "x" + height);
				//		renderer.updateUniforms(uniformBuffer, l -> l.set("TRANSFORMATION", uniformBuffer, new Matrix4f().orthographic(window.getWidth(), window.getHeight())));
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
			renderer.setBuffer(0, vertexBuffer);
			renderer.setIndexBuffer(indexBuffer);
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), b -> {
				renderer.updateUniform("TRANSFORMATION", l -> {
					l.set(new Matrix4f().orthographic(window.getWidth(), window.getHeight()));
				}, b);
			});
			
			renderer.setName("Renderer");
			
			try (PhysicalDeviceTransferState transfert = staticBigBuffer.startLocalBuffering())
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
			synchronized (MONITOR)
			{
				RenderingDataUtils.printBufferContent(staticBigBuffer, vertexBuffer, 10, true, "xyzrgba");
				RenderingDataUtils.printBufferContent(staticBigBuffer, indexBuffer, 8, false, "ijk");
				RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, 4, true, "mmmm");
			}
		}

		@Override
		public void loop() throws VulkanException
		{
			RenderTest.defaultLoop((enlapsed, enlapsedFrame) -> {
				boolean modified = false;
				
				if (i++ == 5)
				{
					f += 0.25F;
					if (f > 50) f = 1;
					modified = true;
					i = 0;
				}
				if (j++ % 10 == 0)
				{
					try (PhysicalDeviceTransferState transfert = staticBigBuffer.startLocalBuffering())
					{
						BufferWriter indexBufferWriter = transfert.beginTransfer(indexBuffer, (k % 8 * 3) * 4, 12);
						
						if (k % 16 >= 8) indexBufferWriter.put(indices, k % 8 * 3, 3);
						else indexBufferWriter.put(0, 0, 0);
					}
					k++;
				}
				if (++l % 25 == 0) modified = true;
				if (modified)
				{
					System.out.println("Rebuild");
					ModificationPlan plan = renderer.startModificationPlan();
					if (i == 0) plan.lineWidth(f);
					if (l % 25 == 0) plan.topology(l % 50 == 0 ? VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST : VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP);
					plan.submit();
				}
				
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

		@Override
		public void end() throws VulkanException
		{
			renderer.close();
			localUniformBuffer.close();
			staticBigBuffer.close();
			for (CommandBuffer commandBuffer : commandBuffers) commandBuffer.close();
			synchronized (MONITOR)
			{
				MONITOR.notify();
			}
		}
	}
}
