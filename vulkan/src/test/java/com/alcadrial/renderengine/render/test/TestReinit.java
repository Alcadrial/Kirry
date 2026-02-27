package com.alcadrial.renderengine.render.test;

import java.io.IOException;

import org.lwjgl.glfw.GLFW;
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

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Vulkan;
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
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout.Type;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class TestReinit extends RenderTest {
	
	private static final String SHADER = "shader";
	
	public static void main(String[] args)
	{
		TestProperties.init(args);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		Logic logic = new Logic();
		
		try
		{
			boolean[] flag = {true};
			for (int i = 0; i < 100 && flag[0]; i++)
			{
				System.out.println("Iteration " + i);
				println("Render Engine initialization", RenderTest::initMultiThreadRenderEngine, TestProperties.PRINT_LOADING_TIMES);
				if (!RenderEngine.isInitializated()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
				
				if (Properties.DEBUG.getValue())
				{
					Debug.setEnabledAllDebug(true);
					Debug.setEnabledExtraTimePrecision(true);
				}
				Thread.ofPlatform().name("Logic").start(() -> {
					try
					{
						logic.load();
						logic.start();
						logic.loop();
						logic.end();
					}
					catch (Throwable e)
					{
						e.printStackTrace(System.out);
						flag[0] = false;
					}
					finally
					{
						System.out.println("Terminate");
						RenderEngine.terminate();
					}
				});
				RenderEngine.serveRenderEngine();
			}
			System.out.println("Test SUCCESS");
		}
		catch (VulkanException e)
		{
			e.printStackTrace(System.out);
		}
	}
	
	public static final class Logic {
		
		private ShaderProgram shaderProgram;
		private RenderPass renderPass;
		private Renderer renderer;
		private GraphicsPipeline pipeline;
		private StaticBigBuffer staticBigBuffer;
		private VulkanBuffer vertexBuffer;
		private VulkanBuffer indexBuffer;
		private VulkanBuffer uniformBuffer;
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
			RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, false);
			RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			
			println("Window init", this::initWindow, TestProperties.PRINT_LOADING_TIMES);
			println("Buffers and Renderer init", this::initBuffersAndRenderer, TestProperties.PRINT_LOADING_TIMES);
			
			i = 0;
		}
		
		private void initWindow() throws VulkanException
		{
			VulkanWindow.defaultBuilder()
				.deviceFeatures(f -> f.wideLines(true))
				.build(1080, 720, "Test");
		}
		
		private void initBuffersAndRenderer() throws VulkanException
		{
			Vulkan vulkan = RenderEngine.getVulkan();
			Device device = vulkan.getDevice();
			VulkanWindow window = vulkan.getWindow();
			
			staticBigBuffer = new StaticBigBuffer(device, 65536);
			
			vertexBuffer = staticBigBuffer.allocate(70 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
			indexBuffer = staticBigBuffer.allocate(24 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
			uniformBuffer = new VulkanBuffer(device, 16 * 4, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
			
			staticBigBuffer.setName("StaticBigBuffer");
			vertexBuffer.setName("VertexBuffer");
			indexBuffer.setName("IndexBuffer");
			uniformBuffer.setName("UniformBuffer");
			
			try (RenderPassTemplate template = RenderPassTemplate.initColorDepthTextureTemplate(window.getSurface().getData().format().format()))
			{
				renderPass = template.createRenderPass(device);
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
					.pBindings(VkDescriptorSetLayoutBinding.malloc(2, stack)
						.apply(b -> b
							.binding(0)
							.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
							.descriptorCount(1)
							.stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT)
							.pImmutableSamplers(null))
						.apply(b -> b
							.binding(1)
							.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
							.descriptorCount(1)
							.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
							.pImmutableSamplers(null))
						.rewind()
					);
				DescriptorPool descriptorPool = new DescriptorPool(device, descriptorPoolCreateInfo);
				DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device, descriptorSetLayoutCreateInfo);
				DescriptorSet descriptorSet = descriptorPool.allocate(descriptorSetLayout);
				VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
					.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
				PipelineLayout pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
				
				uniformBuffer.getMemory().mapMemory();
				
				try (GraphicsPipelineTemplate graphicsPipelineTemplate = GraphicsPipelineTemplate.createScreenRenderingPipeline()
					.configureVertexInputState(s -> s
						.addBinding(0, 28, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
						.addAttribute(0, 0, 0)
						.addAttribute(1, 0, 12)
					)
					.configureInputAssemblyState(s -> s
						.topology(VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP)
					)
					.configureRasterizationState(s -> s
						.cullMode(VK10.VK_CULL_MODE_NONE)
						.lineWidth(2)
					)
					.build())
				{
					pipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, window, pipelineLayout, renderPass, 0);
				}
				renderer = Renderer.builder()
					.graphicsPipeline(pipeline)
					.descriptorSets(descriptorSet)
					.bindingsLayout(
						new ShaderLayoutBinding(VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, Type.UNIFORM_BUFFER, 1, ShaderVariable.TRANSFORMATION)
					)
					.writers(i -> new BufferWriter[] {
						new BufferWriter(uniformBuffer)
					})
					.build();
				
				renderer.setBuffer(0, vertexBuffer);
				renderer.setIndexBuffer(indexBuffer);
				
				BufferLayout bufferLayout = renderer.getBufferLayout(0)[0];
				bufferLayout.set("TRANSFORMATION", new Matrix4f().orthographic(window.getWidth(), window.getHeight()));
				bufferLayout.flushAndInvalidate();
				bufferLayout.updateDescriptorSets();
				uniformBuffer.getMemory().unmapMemory();
				
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
			}
		}
		
		public void loop() throws VulkanException
		{
			RenderTest.defaultAnimationLoop((enlapsed, enlapsedFrame) -> {
				if (++i == 30) RenderEngine.getVulkan().getWindow().setShouldClose(true);
				
				VulkanWindow window = RenderEngine.getVulkan().getWindow();
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					switch (window.startPresentation())
					{
						case EMPTY_SURFACE:
							return;
						case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
							throw new VulkanException("Presentaton error");
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
					if (!commandBuffer.endRecording()) System.out.println("not recording");
					
					window.endPresentation(commandBuffer);
					
					System.out.println("Frame " + i);
				}
			});
		}
		
		public void end() throws VulkanException
		{
			pipeline.close();
			uniformBuffer.close();
			staticBigBuffer.close();
		}
	}
}
