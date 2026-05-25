package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.util.List;

import org.lwjgl.glfw.GLFW;
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

import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
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
import com.alcadrial.renderengine.render.util.OverflowException;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class DepthBufferTest extends RenderTest {
	
	private static final String SHADER = "shader1";
	
	public static void main(String[] args)
	{
		mainTest(args, DepthBufferTest::start, () -> defaultAnimationLoop(DepthBufferTest::loop), DepthBufferTest::end);
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
	
	private static ShaderVariable lightPos;
	private static ShaderVariable lightIntensity;
	private static ShaderVariable spaceFogIntensity;
	
	private static float f;
	private static boolean isModified;
	
	private static Matrix4f buildModelMatrix(float f)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().rotateZ(AngleUnit.RADIANS.fromDegrees(f / 11)));
		matrix.multiply(new Matrix4f().rotateY(AngleUnit.RADIANS.fromDegrees(f / 17)));
		matrix.multiply(new Matrix4f().rotateX(AngleUnit.RADIANS.fromDegrees(f / 13)));
		matrix.multiply(new Matrix4f().scale(Vector3f.of(1, 1, 1).divideAll((float) Math.sqrt(3))));
		return matrix;
	}
	
	public static void start() throws VulkanException
	{
		try
		{
			shaderProgram = ShaderProgram.create().add(loadShader(SHADER + ".vert"), loadShader(SHADER + ".frag")).build();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngineVulkan.terminate();
			return;
		}
		
		RenderEngineVulkan.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngineVulkan.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngineVulkan.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = VulkanWindow.defaultBuilder()
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.build(1080, 720, "Test");
		Device device = window.getDevice();
		
		window.setCallback((GLFWWindowSizeCallbackI) (_, _, _) -> {
			isModified = true;
		});
		
		registerDragging();

		try (RenderPassTemplate template = RenderPassTemplate.initColorDepthTextureTemplate(window.getSurface().getData().format().format()))
		{
			renderPass = template.createRenderPass(device);
		}
		
		staticBigBuffer = new StaticBigBuffer(device, 65536);
		
		vertexBuffer = staticBigBuffer.allocate(24 * 10 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		indexBuffer = staticBigBuffer.allocate(36 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 6, 16 * 2 * 4);
		uniformBuffer.getMemory().mapMemory();
		
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
			BufferWriter indexBufferWriter = transfert.beginTransfer(indexBuffer);
			
			RenderingDataUtils.loadOrthoCube(vertexBufferWriter, 0, indexBufferWriter, 0, 0, Vector3f.of(-1, -1, -1), Vector3f.of(1, 1, 1), (i, _) -> colors[i]);
		}
		catch (OverflowException e)
		{
			e.printStackTrace(System.out);
			RenderEngineVulkan.terminate();
			return;
		}

		lightPos = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "LIGHT_POS");
		lightIntensity = ShaderVariable.of(ShaderInputType.FLOAT, "LIGHT_INTENSITY");
		spaceFogIntensity = ShaderVariable.of(ShaderInputType.FLOAT, "SPACE_FOG_INTENSITY");
		f = 0;
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.maxSets(3)
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
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
						.pImmutableSamplers(null))
					.rewind()
				);
			DescriptorPool descriptorPool = new DescriptorPool(device, descriptorPoolCreateInfo);
			DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device, descriptorSetLayoutCreateInfo);
			DescriptorSet[] descriptorSets = descriptorPool.allocates(descriptorSetLayout, 3);
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
			graphicsPipelineTemplate = GraphicsPipelineTemplate.create3dRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 40, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(1, 0, ShaderInputType.VECTOR_FLOAT_4, 12)
					.addAttribute(2, 0, ShaderInputType.VECTOR_FLOAT_3, 28)
				)
				.build();
			graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, window, pipelineLayout, renderPass, 0);
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.buffersCount(window.getFramesCount())
				.descriptorSets(descriptorSets)
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 2 * i, 1, 16 * 2 * 4), 1, List.of(
						ShaderVariable.PROJECTION,
						ShaderVariable.MODEL
					)),
					BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 2 * i + 1, 1, (3 + 1 + 1) * 4), 1, List.of(
						ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "LIGHT_POS"),
						ShaderVariable.of(ShaderInputType.FLOAT, "LIGHT_INTENSITY"),
						ShaderVariable.of(ShaderInputType.FLOAT, "SPACE_FOG_INTENSITY")
					))
				})
				.build();
			renderer.setBuffer(0, vertexBuffer);
			renderer.setIndexBuffer(indexBuffer);

			Matrix4f projectionMatrix = buildFullSceneMatrix(window, (float) (Math.PI * 2 / 3));
			Matrix4f modelMatrix = buildModelMatrix(f);
			
			for (int i = 0; i < window.getFramesCount(); i++)
			{
				renderer.useBufferLayoutWriter(i, 0, writer -> {
					writer.set(ShaderVariable.MODEL, modelMatrix);
					writer.set(ShaderVariable.PROJECTION, projectionMatrix);
					writer.flushAndInvalidate();
				});
				renderer.useBufferLayoutWriter(i, 1, writer -> {
					writer.set(lightPos, Vector3f.of(0.3F, 1, 0));
					writer.set(lightIntensity, 0.75F);
					writer.set(spaceFogIntensity, 0.1F);
					writer.flushAndInvalidate();
				});
			}
		}
		Debug.runOnDebug(() -> {
			renderPass.setName("Renderpass");
			staticBigBuffer.setName("Big buffer");
			vertexBuffer.setName("Vertex buffer");
			indexBuffer.setName("Index buffer");
			uniformBuffer.setName("Uniform buffer");
		});
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException, InterruptedException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanWindow window = RenderEngineVulkan.getVulkan().getWindow();
			switch (window.startPresentation())
			{
				case EMPTY_SURFACE:
					return;
				case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
					throw new VulkanException("Presentaton error");
			}
			if (isModified)
			{
				isModified = false;
				println("Resize", () -> {
					graphicsPipeline.close();
					graphicsPipeline = graphicsPipelineTemplate.createPipeline(window.getDevice(), shaderProgram, window, pipelineLayout, renderPass, 0);
					renderer.changePipeline(graphicsPipeline, null);
					
					Matrix4f projectionMatrix = buildFullSceneMatrix(window, (float) (Math.PI * 2 / 3));
					for (int i = 0; i < window.getFramesCount(); i++)
					{
						renderer.useBufferLayoutWriter(i, 0, writer -> {
							writer.set(ShaderVariable.PROJECTION, projectionMatrix);
							writer.flushAndInvalidate();
						});
					}
				}, TestProperties.PRINT_LOADING_TIMES);
			}
			
			CommandBuffer commandBuffer = window.getCurrentCommandBuffer();
			
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

			renderer.useBufferLayoutWriter(window.getFrameIndex(), 0, writer -> {
				writer.set(ShaderVariable.MODEL, buildModelMatrix(f));
				writer.flushAndInvalidate();
			});
			
			commandBuffer.beginRecording();
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, window.getFrameIndex());
			commandBuffer.endRenderPass();
			commandBuffer.endRecording();
			window.endPresentation(commandBuffer);
			f = time / 1000000F;
		}
	}
	
	public static void end()
	{
		graphicsPipeline.close();
		graphicsPipelineTemplate.close();
		pipelineLayout.close();
		uniformBuffer.close();
		staticBigBuffer.close();
		RenderEngineVulkan.terminate();
	}
}
