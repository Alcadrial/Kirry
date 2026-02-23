package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.Map;
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
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
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
import com.alcadrial.renderengine.render.shader.ShaderLayoutBinding;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderUtils;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.RenderingDataUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class Test2dPanel extends RenderTest {
	
	private static final String SHADER = "shader";
	private static final String TRANSFORMATION_UNIFORM = "TRANSFORMATION";
	private static final int SIZE = 256;
	
	public static void main(String[] args)
	{
		mainTest(args, Test2dPanel::start, () -> defaultLoop(Test2dPanel::loop), Test2dPanel::end, false);
	}
	
	private static RenderPass renderPass;
	private static Renderer renderer;
	private static GraphicsPipeline graphicsPipeline;
	private static VulkanBuffer vertexBuffer;
	private static VulkanBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static Vector2i panelPos;
	
	private static boolean isModifiedMatrix;
	private static boolean isModified;
	private static StaticBigBuffer staticBigBuffer;
	
	public static void start() throws VulkanException
	{
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = VulkanWindow.builder()
			.deviceExtensions(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
			.useDepthTexture(false)
			.size(1080, 720)
			.title("Test")
			.build();
		
		window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> { System.out.println(iconified ? "Iconified" : "Restored"); });
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			System.out.println("Resized " + width + "x" + height);
			isModifiedMatrix = true;
		});
		window.setCallback((GLFWWindowRefreshCallbackI) w -> {
			System.out.println("Refresh");
			isModified = true;
		});
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
		
		Device device = window.getDevice();
		
		try (RenderPassTemplate template = RenderPassTemplate.initColorTextureTemplate(window.getSurface().getData().format().format()))
		{
			renderPass = template.createRenderPass(device);
		}
		
		staticBigBuffer = new StaticBigBuffer(device, ((7 * 4 + 6) * SIZE * SIZE * 4 + 64) * 4);
		
		vertexBuffer = staticBigBuffer.allocate(7 * 4 * SIZE * SIZE * 4 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		indexBuffer = staticBigBuffer.allocate(6 * SIZE * SIZE * 4 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		uniformBuffer = new VulkanBuffer(device, 256 * 4, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT | VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
		uniformBuffer.getMemory().mapMemory();
		
		staticBigBuffer.setName("StaticBigBuffer");
		vertexBuffer.setName("VertexBuffer");
		indexBuffer.setName("IndexBuffer");
		uniformBuffer.setName("UniformBuffer");
		
		long seed = Double.doubleToRawLongBits(Math.random());
		
		try (PhysicalDeviceTransferState transfer = staticBigBuffer.startLocalBuffering())
		{
			BufferWriter vertexBufferWriter = transfer.beginTransfer(vertexBuffer);
			BufferWriter indexBufferWriter = transfer.beginTransfer(indexBuffer);
			
			Random random = new Random(seed);
			int k = 0;
			for (int i = -SIZE; i < SIZE; i++) for (int j = -SIZE; j < SIZE; j++)
			{
				RenderingDataUtils.loadRectangleColor(vertexBufferWriter, indexBufferWriter, k, j, i, j + 1, i + 1, 0, random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
				k += 4;
			}
		}
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			Map<Integer, ShaderLayoutBinding> bindingFlags = ShaderUtils.extractData(shaderProgram);
			DescriptorPool descriptorPool = DescriptorPool.ofShaderProgram(device, shaderProgram, 1);
			DescriptorSetLayout descriptorSetLayout = DescriptorSetLayout.ofBindings(device, bindingFlags);
			DescriptorSet descriptorSet = descriptorPool.allocate(descriptorSetLayout);
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			PipelineLayout layout = new PipelineLayout(device, pipelineLayoutInfo);
			
			try (GraphicsPipelineTemplate template = GraphicsPipelineTemplate.create3dRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 28, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(0, 0, 0)
					.addAttribute(1, 0, 12)
				)
				.build())
			{
				graphicsPipeline = template.createPipeline(device, shaderProgram, window.getSurface().getData().size(), layout, renderPass, 0);
			}
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSet)
				.shaderProgram(shaderProgram)
				.buffersCount(window.getFramesCount())
				.writers(i -> new BufferWriter[] {
					new BufferWriter(uniformBuffer, uniformBuffer, 256 * i, 256)
				})
				.bindingsLayout(new ShaderVariable[] {
					ShaderVariable.TRANSFORMATION
				})
				.build();
		}
		
		renderer.setBuffer(0, vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
		
		Matrix4f transformation = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
		Matrix4f model = new Matrix4f().translateYTo(-panelPos.y()).translateXTo(-panelPos.x());
		transformation.multiply(model);
		for (int i = 0; i < window.getFramesCount(); i++)
		{
			BufferLayout bufferLayout = renderer.getBufferLayout(i)[0];
			bufferLayout.set(TRANSFORMATION_UNIFORM, transformation);
			bufferLayout.flush();
			bufferLayout.updateDescriptorSets();
		}
		
		isModifiedMatrix = true;
		isModified = true;
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		if (isModifiedMatrix)
		{
			isModifiedMatrix = false;
			
			isModified = true;
		}
		if (isModified)
		{
			isModified = false;
			
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				Debug.debug("Before startPresentation");
				window.startPresentation();
				Debug.debug("After startPresentation");
				FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
				CommandBuffer commandBuffer = window.getCurrentCommandBuffer();
				
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
				
				commandBuffer.reset();
				commandBuffer.beginRecording();
				//commandBuffer.setScissor(0, 0, window.getWidth(), window.getHeight());
				//commandBuffer.setViewport(0, window.getHeight(), window.getWidth(), -window.getHeight(), 0, 1);
				commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
				renderer.draw(commandBuffer);
				commandBuffer.endRenderPass();
				commandBuffer.endRecording();
				Debug.debug("Before endPresentation");
				window.endPresentation(commandBuffer);
				Debug.debug("After endPresentation");
			}
		}
		window.pollEvents();
	}
	
	public static void end()
	{
		graphicsPipeline.close();
		staticBigBuffer.close();
		RenderEngine.getVulkan().getWindow().hide();
		synchronized (MONITOR)
		{
			MONITOR.notify();
		}
		RenderEngine.terminate();
	}
}
