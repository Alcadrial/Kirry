package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.matrix.Matrix4f;
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
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
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
		mainTest(args, InstancingTest::start, () -> defaultAnimationLoop(InstancingTest::loop), InstancingTest::end, false);
	}
	
	private static RenderPass renderPass;
	private static Renderer renderer;
	private static ShaderProgram shaderProgram;
	private static PipelineLayout pipelineLayout;
	private static GraphicsPipelineTemplate graphicsPipelineTemplate;
	private static GraphicsPipeline graphicsPipeline;
	private static StaticBigBuffer staticBigBuffer;
	private static VulkanBuffer vertexBuffer;
	private static VulkanBuffer instanceVertexBuffer;
	private static VulkanBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static @Nullable VulkanBuffer localUniformBuffer;
	
	private static final int size = 20;
	private static final int density = 2;
	private static final int instanceCount;
	
	private static Vector3f lightPos;
	private static float f;
	private static boolean isModified;
	
	static {
		int a = ((2 * size + 1) / density + 1);
		instanceCount = a * a;
	}
	
	private static Matrix4f buildProjectionMatrix(VulkanWindow window)
	{
		float sceneRay = 1;//ray of the sphere containing the entire scene after applying the model matrix
		float aspectRatio = (float) window.getWidth() / window.getHeight();
		float fieldOfView = AngleUnit.RADIANS.fromDegrees(120);
		float scale = (float) (1 / Math.tan(fieldOfView / 2));
		float cameraZ = (float) (sceneRay * Math.sqrt(1 + scale * scale) / scale);
		float maxDepth = cameraZ + sceneRay;
		float minDepth = cameraZ - sceneRay;
		
		Matrix4f matrix = new Matrix4f();
		matrix.projectPositiveZ(fieldOfView, maxDepth, minDepth, aspectRatio);
		matrix.multiply(new Matrix4f().translateZTo(-cameraZ));
		
		return matrix;
	}
	
	private static Matrix4f buildModelMatrix(float f)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().rotateZ(AngleUnit.RADIANS.fromDegrees(f / 11)));
		matrix.multiply(new Matrix4f().rotateY(AngleUnit.RADIANS.fromDegrees(f / 17)));
		matrix.multiply(new Matrix4f().rotateX(AngleUnit.RADIANS.fromDegrees(f / 13)));
		matrix.multiply(buildScaleMatrix());
		return matrix;
	}
	
	private static Matrix4f buildScaleMatrix()
	{
		float ss = (size + 1) * (size + 1);
		float sceneRay = (float) Math.sqrt(ss + ss + 1 / ss);//ray of the sphere containing the entire scene
		return new Matrix4f().scale(Vector3f.of(1, 1, 1).divideAll(sceneRay));
	}
	
	public static void start() throws VulkanException
	{
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
		
		VulkanWindow window = VulkanWindow.defaultBuilder()
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.build(1080, 720, "Test");
		
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			isModified = true;
		});
		
		registerDragging();
		
		Device device = window.getDevice();
		
		try (RenderPassTemplate template = RenderPassTemplate.initColorDepthTextureTemplate(window.getSurface().getData().format().format()))
		{
			renderPass = template.createRenderPass(device);
		}
		
		staticBigBuffer = new StaticBigBuffer(device, 65536);
		
		vertexBuffer = staticBigBuffer.allocate(240 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		instanceVertexBuffer = staticBigBuffer.allocate(instanceCount * 3 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		indexBuffer = staticBigBuffer.allocate(36 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		try
		{
			uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 6);
			uniformBuffer.getMemory().mapMemory();
		}
		catch (OverflowException e)
		{
			throw e;
			/* TODO: fallback to memory transfers
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
			
			RenderingDataUtils.loadOrthoCube(vertexBufferWriter, 0, indexBufferWriter, 0, 0, Vector3f.origin(), Vector3f.of(1, 1, 1), (i, j) -> colors[i]);
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
						.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
					)
					.rewind()
				);
			
			VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.malloc(stack)
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
			DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device, createInfo);
			DescriptorSet[] descriptorSets = descriptorPool.allocates(descriptorSetLayout, 3);
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			graphicsPipelineTemplate = GraphicsPipelineTemplate.create3dRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 40, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addBinding(1, 12, VK10.VK_VERTEX_INPUT_RATE_INSTANCE)
					.addAttribute(0, 0, 0)
					.addAttribute(1, 0, 12)
					.addAttribute(2, 0, 28)
					.addAttribute(3, 1, 0)
				)
				.build();
			
			graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, window, pipelineLayout, renderPass, 0);
			
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSets)
				.buffersCount(window.getFramesCount())
				.writers(i -> new BufferWriter[] {
					new BufferWriter(uniformBuffer, uniformBuffer, 512 * i, 256),
					new BufferWriter(uniformBuffer, uniformBuffer, 512 * i + 256, 256)
				})
				.bindingsLayout(
					new ShaderLayoutBinding(VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, ShaderLayout.Type.UNIFORM_BUFFER, 1, List.of(
						ShaderVariable.PROJECTION,
						ShaderVariable.MODEL
					)),
					new ShaderLayoutBinding(VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderLayout.Type.UNIFORM_BUFFER, 1, List.of(
						ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, LIGHT_POS),
						ShaderVariable.of(ShaderInputType.FLOAT, LIGHT_INTENSITY),
						ShaderVariable.of(ShaderInputType.FLOAT, SPACE_FOG_INTENSITY)
					))
				)
				.build();
			renderer.setBuffer(0, vertexBuffer);
			renderer.setBuffer(1, instanceVertexBuffer);
			renderer.setIndexBuffer(indexBuffer);
			
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
				}
			}
		}
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
			if (isModified)
			{
				isModified = false;
				println("Resize", () -> {
					graphicsPipeline.close();
					graphicsPipeline = graphicsPipelineTemplate.createPipeline(window.getDevice(), shaderProgram, window, pipelineLayout, renderPass, 0);
					renderer.changePipeline(graphicsPipeline, null);
					
					for (int i = 0; i < window.getFramesCount(); i++)
					{
						BufferLayout bufferLayout = renderer.getBufferLayout(i)[0];
						bufferLayout.set(PROJECTION, buildProjectionMatrix(window));
						bufferLayout.flush();
					}
				}, TestProperties.PRINT_LOADING_TIMES);
			}
			
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
			
			commandBuffer.beginRecording();
			/* TODO: fallback to memory transfers
			if (localUniformBuffer != null)
			{
				bufferLayout.transfer(commandBuffer);
				//barrier
			}
			*/
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, window.getFrameIndex(), instanceCount);
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
		staticBigBuffer.close();
		uniformBuffer.close();
		if (localUniformBuffer != null) localUniformBuffer.close();
		RenderEngine.terminate();
	}
}
