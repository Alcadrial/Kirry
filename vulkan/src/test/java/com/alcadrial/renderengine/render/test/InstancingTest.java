package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.Map;

import org.jspecify.annotations.Nullable;
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
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.buffer.AbstractBigVulkanBuffer.PhysicalDeviceTransferState;
import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderLayoutBinding;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderUtils;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.RenderingDataUtils;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.util.OverflowException;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class InstancingTest extends RenderTest {
	
	private static final String SHADER = "shader2";
	private static final String PROJECTION = "PROJECTION";
	private static final String MODEL = "MODEL";
	private static final String LIGHT_POS = "LIGHT_POS";
	private static final String LIGHT_INTENSITY = "LIGHT_INTENSITY";
	private static final String SPACE_FOG_INTENSITY = "SPACE_FOG_INTENSITY";
	
	public static void main(String[] args)
	{
		mainTest(args, InstancingTest::start, () -> defaultLoop(InstancingTest::loop), InstancingTest::end, false);
	}
	
	private static RenderPass renderPass;
	private static Renderer renderer;
	private static GraphicsPipeline graphicsPipeline;
	private static StaticBigBuffer staticBigBuffer;
	private static VulkanBuffer vertexBuffer;
	private static VulkanBuffer instanceVertexBuffer;
	private static VulkanBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static @Nullable VulkanBuffer localUniformBuffer;
	private static Vector3f lightPos;
	private static float f;
	private static int instanceCount;
	
	private static Matrix4f buildProjectionMatrix(VulkanWindow window)
	{
		Matrix4f matrix = new Matrix4f();
		//matrix.multiply(new Matrix4f().project(AngleUnit.RADIANS.fromDegrees(90), 0.09F, -0.1F, (float) window.getWidth() / window.getHeight()));
		matrix.multiply(new Matrix4f().view(Vector3f.of(0, 0, -1), 0, 0));
		return matrix.transpose();
	}
	
	private static Matrix4f buildModelMatrix(float f)
	{
		Matrix4f matrix = new Matrix4f();
		//matrix.multiply(new Matrix4f().rotateZ(AngleUnit.RADIANS.fromDegrees(f / 11)));
		//matrix.multiply(new Matrix4f().rotateY(AngleUnit.RADIANS.fromDegrees(f / 17)));
		//matrix.multiply(new Matrix4f().rotateX(AngleUnit.RADIANS.fromDegrees(f / 13)));
		matrix.multiply(buildScaleMatrix());
		return matrix;
	}
	
	private static Matrix4f buildScaleMatrix()
	{
		return new Matrix4f().scale(Vector3f.of(1, 1, 1).divideAll(16));
	}
	
	public static void start() throws VulkanException
	{
		ShaderProgram shaderProgram;
		try
		{
			shaderProgram = ShaderProgram.create()
				.add(loadShader(SHADER + ".vert"))
				.add(loadShader(SHADER + ".frag"))
				.build();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = VulkanWindow.builder()
			.physicalDeviceFilter(PhysicalDevice.Filter.withSurfaceGraphicsAndCompute())
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.deviceFeatures(v -> v
				.wideLines(true)
				.samplerAnisotropy(true)
			)
			.deviceExtensions(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
			.asDefaultPrimary()
			.size(1080, 720)
			.title("Test")
			.build();
		
		window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> { System.out.println(iconified ? "Iconified" : "Restored"); });
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			System.out.println("Resized " + width + "x" + height);
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				for (int i = 0; i < window.getFramesCount(); i++)
				{
					BufferLayout bufferLayout = renderer.getBufferLayout(i)[0];
					bufferLayout.set(PROJECTION, buildProjectionMatrix(window));
					bufferLayout.flushAndInvalidate();
					//TODO notify changes to render-thread
				}
			}
			catch (VulkanException e)
			{
				e.printStackTrace(System.out);
				RenderEngine.terminate();
				return;
			}
		});
		window.setCallback((GLFWWindowRefreshCallbackI) w -> { System.out.println("Refresh"); });
		window.setCallback((GLFWWindowCloseCallbackI) w -> { System.out.println("Closed"); });
		window.setCallback((GLFWWindowMaximizeCallbackI) (w, maximized) -> { System.out.println(maximized ? "Maximized" : "Restored"); });
		
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
		
		window.setCallback((GLFWKeyCallbackI) (w, k, s, a, m) -> {
			if (k == GLFW.GLFW_KEY_R)
			{
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					for (int i = 0; i < window.getFramesCount(); i++)
					{
						BufferLayout bufferLayout = renderer.getBufferLayout(i)[0];
						bufferLayout.set(PROJECTION, buildProjectionMatrix(window));
						bufferLayout.set(MODEL, buildModelMatrix(f));
						bufferLayout.set(LIGHT_POS, buildScaleMatrix().multiply(lightPos).xyz());
						bufferLayout.set(LIGHT_INTENSITY, 0.75F);
						bufferLayout.set(SPACE_FOG_INTENSITY, 0.1F);
						bufferLayout.flushAndInvalidate();
						//TODO notify changes to render-thread
					}
				}
				catch (VulkanException e)
				{
					e.printStackTrace(System.out);
					RenderEngine.terminate();
					return;
				}
			}
		});
		
		Device device = window.getDevice();
		
		try (RenderPassTemplate template = RenderPassTemplate.initColorDepthTextureTemplate(window.getSurface().getData().format().format()))
		{
			renderPass = template.createRenderPass(device);
		}
		
		int size = 20;
		int density = 2;
		int a = ((2 * size + 1) / density + 1);
		instanceCount = a * a;
		
		staticBigBuffer = new StaticBigBuffer(device, 65536);
		
		vertexBuffer = staticBigBuffer.allocate(240 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		instanceVertexBuffer = staticBigBuffer.allocate(instanceCount * 3 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		indexBuffer = staticBigBuffer.allocate(36 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		try
		{
			uniformBuffer = new VulkanBuffer(device, 256 * 2 * window.getFramesCount() * 4, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
			uniformBuffer.getMemory().mapMemory();
		}
		catch (OverflowException e)
		{
			throw e;
			/*
			uniformBuffer = staticBigBuffer.allocate(256 * 2 * window.getFrameCount() * 4, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
			localUniformBuffer = VulkanBuffer.asLocal(uniformBuffer);
			localUniformBuffer.getMemory().mapMemory();
			*/
		}
		
		staticBigBuffer.setName("StaticBigBuffer");
		vertexBuffer.setName("VertexBuffer");
		instanceVertexBuffer.setName("InstanceVertexBuffer");
		indexBuffer.setName("IndexBuffer");
		uniformBuffer.setName("UniformBuffer");
		
		Color[] colors = new Color[] {
			Color.VERY_LIGHT_YELLOW,
			Color.VERY_LIGHT_RED,
			Color.VERY_LIGHT_MAGENTA,
			Color.VERY_LIGHT_GREEN,
			Color.VERY_LIGHT_CYAN,
			Color.VERY_LIGHT_BLUE
		};
		
		try (PhysicalDeviceTransferState transfert = staticBigBuffer.startLocalBuffering())
		{
			BufferWriter vertexBufferWriter = transfert.beginTransfer(vertexBuffer);
			BufferWriter vertexBuffer0Writer = transfert.beginTransfer(instanceVertexBuffer);
			BufferWriter indexBufferWriter = transfert.beginTransfer(indexBuffer);
			
			for (int j = -size; j <= size; j += density) for (int k = -size; k <= size; k += density)
			{
				vertexBuffer0Writer.put(Vector3f.of(j, k, 0));
			}
			
			RenderingDataUtils.loadOrthoCube(vertexBufferWriter, 0, indexBufferWriter, 0, 0, Vector3f.of(-1, -1, -1), Vector3f.of(0, 0, 0), (i, j) -> colors[i]);
		}
		catch (OverflowException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		lightPos = Vector3f.of(5, 20, 0);
		f = 0;
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			Map<Integer, ShaderLayoutBinding> bindingFlags = ShaderUtils.extractData(shaderProgram);
			DescriptorPool descriptorPool = DescriptorPool.ofShaderProgram(device, shaderProgram, window.getFramesCount());
			DescriptorSetLayout descriptorSetLayout = DescriptorSetLayout.ofBindings(device, bindingFlags);
			DescriptorSet[] descriptorSets = descriptorPool.allocates(descriptorSetLayout, 3);
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			PipelineLayout layout = new PipelineLayout(device, pipelineLayoutInfo);
			
			try (GraphicsPipelineTemplate template = GraphicsPipelineTemplate.create3dRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 40, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addBinding(1, 12, VK10.VK_VERTEX_INPUT_RATE_INSTANCE)
					.addAttribute(0, 0, 0)
					.addAttribute(1, 0, 12)
					.addAttribute(2, 0, 28)
					.addAttribute(3, 1, 0)
				)
				.build())
			{
				graphicsPipeline = template.createPipeline(device, shaderProgram, window.getSurface().getData().size(), layout, renderPass, 0);
			}
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSets)
				.shaderProgram(shaderProgram)
				.buffersCount(window.getFramesCount())
				.writers(i -> new BufferWriter[] {
					new BufferWriter(uniformBuffer, uniformBuffer, 512 * i, 256),
					new BufferWriter(uniformBuffer, uniformBuffer, 512 * i + 256, 256)
				})
				.bindingsLayout(
						new ShaderVariable[] {
							ShaderVariable.PROJECTION,
							ShaderVariable.MODEL
						},
						new ShaderVariable[] {
							ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, LIGHT_POS),
							ShaderVariable.of(ShaderInputType.FLOAT, LIGHT_INTENSITY),
							ShaderVariable.of(ShaderInputType.FLOAT, SPACE_FOG_INTENSITY)
						}
				)
				.build();
			renderer.setBuffer(0, vertexBuffer);
			renderer.setBuffer(1, instanceVertexBuffer);
			renderer.setIndexBuffer(indexBuffer);
			
			VulkanBuffer localUniformBuffer = InstancingTest.localUniformBuffer;
			if (localUniformBuffer != null)
			{
				CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
				for (int i = 0; i < window.getFramesCount(); i++)
				{
					BufferLayout bufferLayout = renderer.getBufferLayout(i)[0];
					bufferLayout.set(PROJECTION, buildProjectionMatrix(window));
					bufferLayout.set(LIGHT_POS, buildScaleMatrix().multiply(lightPos).xyz());
					bufferLayout.set(LIGHT_INTENSITY, 0.75F);
					bufferLayout.set(SPACE_FOG_INTENSITY, 0.1F);
					bufferLayout.updateDescriptorSets();
					bufferLayout.transfer(commandBuffer);
					
					RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, i * 128 + 0, 16, true, "PPPP");
					RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, i * 128 + 16, 16, true, "MMMM");
					RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, i * 128 + 64 + 0, 3, true, "xyz");
					RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, i * 128 + 64 + 3, 1, true, "I");
					RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, i * 128 + 64 + 4, 1, true, "F");
				}
				commandBuffer.endRecording();
				device.submitSingleUseCommandBuffer(stack, commandBuffer).join();
			}
			else
			{
				for (int i = 0; i < window.getFramesCount(); i++)
				{
					BufferLayout bufferLayout = renderer.getBufferLayout(i)[0];
					bufferLayout.set(MODEL, buildModelMatrix(f));
					bufferLayout.set(PROJECTION, buildProjectionMatrix(window));
					bufferLayout.flush();
					bufferLayout.updateDescriptorSets();
					bufferLayout = renderer.getBufferLayout(i)[1];
					bufferLayout.set(LIGHT_POS, buildScaleMatrix().multiply(lightPos).xyz());
					bufferLayout.set(LIGHT_INTENSITY, 0.75F);
					bufferLayout.set(SPACE_FOG_INTENSITY, 0.1F);
					bufferLayout.flush();
					bufferLayout.updateDescriptorSets();
					
					RenderingDataUtils.printBufferContent(uniformBuffer, uniformBuffer, i * 128 + 0, 16, true, "PPPP");
					RenderingDataUtils.printBufferContent(uniformBuffer, uniformBuffer, i * 128 + 16, 16, true, "MMMM");
					RenderingDataUtils.printBufferContent(uniformBuffer, uniformBuffer, i * 128 + 64 + 0, 3, true, "xyz");
					RenderingDataUtils.printBufferContent(uniformBuffer, uniformBuffer, i * 128 + 64 + 3, 1, true, "I");
					RenderingDataUtils.printBufferContent(uniformBuffer, uniformBuffer, i * 128 + 64 + 4, 1, true, "F");
				}
			}
		}
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			window.startPresentation();
			FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
			CommandBuffer commandBuffer = window.getCurrentCommandBuffer();
			BufferLayout bufferLayout = renderer.getBufferLayout(window.getFrameIndex())[0];
			
			VkRect2D renderArea = VkRect2D.malloc(stack);
			renderArea.offset().set(0, 0);
			renderArea.extent().set(frameBuffer.getWidth(), frameBuffer.getHeight());
			VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
			clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
			clearValues.get().depthStencil().depth(1).stencil(0);
			
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
				.pNext(0)
				.renderPass(renderPass.getVk())
				.framebuffer(frameBuffer.getVk())
				.renderArea(renderArea)
				.pClearValues(clearValues.rewind());
			
			bufferLayout.set(MODEL, buildModelMatrix(f));
			if (localUniformBuffer == null) bufferLayout.flushAndInvalidate();
			
			commandBuffer.reset();
			commandBuffer.beginRecording();
			if (localUniformBuffer != null)
			{
				bufferLayout.transfer(commandBuffer);
				//barrier
			}
			//commandBuffer.setScissor(0, 0, window.getWidth(), window.getHeight());
			//commandBuffer.setViewport(0, window.getHeight(), window.getWidth(), -window.getHeight(), 0, 1);
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, window.getFrameIndex(), instanceCount);
			commandBuffer.endRenderPass();
			commandBuffer.endRecording();
			
			window.endPresentation(commandBuffer);
		}
		println("pollEvents", () -> window.pollEvents(), TestProperties.PRINT_FRAME_TIMES);
		f = time / 1000000F;
	}
	
	public static void end()
	{
		graphicsPipeline.close();
		synchronized (MONITOR)
		{
			MONITOR.notify();
		}
		RenderEngine.terminate();
	}
}
