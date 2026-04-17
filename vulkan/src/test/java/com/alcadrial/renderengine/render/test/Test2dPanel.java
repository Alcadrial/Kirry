package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowIconifyCallbackI;
import org.lwjgl.glfw.GLFWWindowMaximizeCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.buffer.AbstractBigVulkanBuffer.PhysicalDeviceTransferState;
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
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.binding.BindingInfo;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.RenderingDataUtils;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class Test2dPanel extends RenderTest {
	
	private static final String SHADER = "shader";
	private static final int SIZE = 256;
	
	public static void main(String[] args)
	{
		mainTest(args, Test2dPanel::start, () -> staticLoop(Test2dPanel::loop, r -> {
			render = r;
			RenderEngine.getVulkan().getWindow().show();
		}), Test2dPanel::end);
	}
	
	private static RenderPass renderPass;
	private static Renderer renderer;
	private static ShaderProgram shaderProgram;
	private static PipelineLayout pipelineLayout;
	private static GraphicsPipelineTemplate graphicsPipelineTemplate;
	private static GraphicsPipeline graphicsPipeline;
	private static StaticBigBuffer staticBigBuffer;
	private static VulkanBuffer vertexBuffer;
	private static VulkanBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	
	private static Runnable render;
	private static Vector2f panelPos;
	private static boolean isModifiedMatrix;
	private static boolean isRendererDirty;
	
	public static void start() throws VulkanException
	{
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = VulkanWindow.defaultBuilder()
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.useDepthTexture(false)
			.build(1080, 720, "Test");
		
		window.setCallback((GLFWWindowIconifyCallbackI) (_, iconified) -> { System.out.println(iconified ? "Iconified" : "Restored"); });
		window.setCallback((GLFWWindowSizeCallbackI) (_, width, height) -> {
			System.out.println("Resized " + width + "x" + height);
			isModifiedMatrix = true;
			isRendererDirty = true;
		});
		window.setCallback((GLFWWindowCloseCallbackI) _ -> { System.out.println("Closed"); });
		window.setCallback((GLFWWindowMaximizeCallbackI) (_, maximized) -> { System.out.println(maximized ? "Maximized" : "Restored"); });
		
		Vector2i[] oldPos = new Vector2i[] {Vector2i.of(0, 0)};
		Vector2f[] oldMousePos = new Vector2f[] {Vector2f.of(0, 0), Vector2f.of(0, 0)};
		boolean[] pressing = new boolean[2];
		window.setCallback((GLFWMouseButtonCallbackI) (_, button, action, _) -> {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
			{
				if (pressing[0] = action == GLFW.GLFW_PRESS)
				{
					window.getMousePos(oldMousePos[0]);
				}
			}
		});
		window.setCallback((GLFWCursorPosCallbackI) (_, x, y) -> {
			if (pressing[0] && !pressing[1])
			{
				pressing[1] = true;
				window.getPos(oldPos[0]);
				window.setPos(Vector2i.of((float) x - oldMousePos[0].x() + oldPos[0].x(), (float) y - oldMousePos[0].y() + oldPos[0].y(), RoundingMode.FLOOR));
				window.getMousePos(oldMousePos[0]);
				pressing[1] = false;
			}
		});
		panelPos = Vector2f.of(0, 0);
		window.setCallback((GLFWKeyCallbackI) (_, k, _, a, m) -> {
			if (a == GLFW.GLFW_PRESS || a == GLFW.GLFW_REPEAT)
			{
				float v = 1;
				if ((m & GLFW.GLFW_MOD_SHIFT) != 0) v += 2;
				if ((m & GLFW.GLFW_MOD_CONTROL) != 0) v *= 2;
				switch (k)
				{
					case 'W':
						panelPos.add(0, v);
						break;
					case 'A':
						panelPos.add(-v, 0);
						break;
					case 'S':
						panelPos.add(0, -v);
						break;
					case 'D':
						panelPos.add(v, 0);
						break;
					default:
						return;
				}
				isModifiedMatrix = true;
				render.run();
			}
		});
		
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
		uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 1);
		
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
				RenderingDataUtils.loadRectangleColor(vertexBufferWriter, indexBufferWriter, k, j, i, j + 1, i + 1, 1, random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
				k += 4;
			}
		}
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.maxSets(1)
				.pPoolSizes(VkDescriptorPoolSize.malloc(2, stack)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
					)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.descriptorCount(1)
					)
					.rewind()
				);
			
			VkDescriptorSetLayoutCreateInfo descriptorSetLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.pBindings(VkDescriptorSetLayoutBinding.malloc(1, stack)
					.apply(b -> b
						.binding(0)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT)
						.pImmutableSamplers(null))
					.rewind()
				);
			
			DescriptorPool descriptorPool = new DescriptorPool(device, descriptorPoolCreateInfo);
			DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device, descriptorSetLayoutCreateInfo);
			DescriptorSet descriptorSet = descriptorPool.allocate(descriptorSetLayout);
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			graphicsPipelineTemplate = GraphicsPipelineTemplate.createScreenRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 28, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(1, 0, ShaderInputType.VECTOR_FLOAT_4, 12)
				)
				.build();
			graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, window, pipelineLayout, renderPass, 0);
			
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSet)
				.buffersCount(1)
				.bindings(
					BindingInfo.ofUniform(uniformBuffer, VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, List.of(ShaderVariable.TRANSFORMATION))
				)
				.build();
		}
		
		renderer.setBuffer(0, vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
		
		isModifiedMatrix = true;
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanWindow window = RenderEngine.getVulkan().getWindow();
			switch (window.startPresentation())
			{
				case EMPTY_SURFACE:
					return;
				case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
					throw new VulkanException("Presentaton error");
			}
			if (isRendererDirty)
			{
				isRendererDirty = false;
				println("Rebuild graphics pipeline", () -> {
					graphicsPipeline.close();
					graphicsPipeline = graphicsPipelineTemplate.createPipeline(window.getDevice(), shaderProgram, window, pipelineLayout, renderPass, 0);
					renderer.changePipeline(graphicsPipeline, null);
				}, TestProperties.PRINT_LOADING_TIMES);
				isModifiedMatrix = true;
			}
			if (isModifiedMatrix)
			{
				isModifiedMatrix = false;
				float scale = (float) Math.min(window.getWidth(), window.getHeight()) / SIZE;
				Matrix4f transformation = new Matrix4f().scale(Vector3f.of(scale / window.getWidth(), -scale / window.getHeight(), 0.02F));
				Matrix4f model = new Matrix4f().translateTo(panelPos);
				transformation.multiply(model);
				uniformBuffer.getMemory().mapMemory();
				renderer.useBufferLayoutWriter(0, 0, writer -> {
					writer.set(ShaderVariable.TRANSFORMATION, transformation);
					writer.flushAndInvalidate();					
				});
				uniformBuffer.getMemory().unmapMemory();
			}
			
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
			
			commandBuffer.beginRecording();
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, 0);
			commandBuffer.endRenderPass();
			commandBuffer.endRecording();
			
			window.endPresentation(commandBuffer);
		}
	}
	
	public static void end()
	{
		graphicsPipeline.close();
		graphicsPipelineTemplate.close();
		pipelineLayout.close();
		uniformBuffer.close();
		staticBigBuffer.close();
		RenderEngine.terminate();
	}
}
