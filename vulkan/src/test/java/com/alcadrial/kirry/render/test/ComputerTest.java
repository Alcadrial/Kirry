package com.alcadrial.kirry.render.test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.AngleUnit;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kirry.core.util.color.Color;
import com.alcadrial.kirry.render.Computer;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.buffer.writer.DeferredBufferWriter;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.memory.MemoryTransferState;
import com.alcadrial.kirry.render.pipeline.ComputePipeline;
import com.alcadrial.kirry.render.pipeline.GraphicsPipeline;
import com.alcadrial.kirry.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.kirry.render.pipeline.PipelineLayout;
import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.renderpass.RenderPassTemplate;
import com.alcadrial.kirry.render.shader.PreparedShader;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.shader.binding.BindingInfo;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorPool;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSet;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.kirry.render.test.util.RenderTest;
import com.alcadrial.kirry.render.test.util.TestProperties;
import com.alcadrial.kirry.render.util.SphereDataCollector;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.future.VulkanFuture;
import com.alcadrial.kirry.render.window.VulkanWindow;

public class ComputerTest extends RenderTest {
	
	private static final String SHADER = "shaders/shader3";
	
	public static void main(String[] args)
	{
		mainTest(args, ComputerTest::start, () -> defaultAnimationLoop(ComputerTest::loop), ComputerTest::end);
	}
	
	private static final int DIM = 8;
	private static final int PARTICLE_COUNT = 8 * DIM * DIM * DIM;
	private static final Vector3fr MIN_BOUND_VALUE = Vector3fr.of(-2, -2, -2);
	private static final Vector3fr MAX_BOUND_VALUE = Vector3fr.of(2, 2, 2);
	
	private static RenderPass renderPass;
	private static Renderer renderer;
	private static Computer computer;
	private static MemoryManager memoryManager;
	private static VulkanBuffer[] storageBuffers;
	private static VulkanBuffer uniformBuffer;
	
	private static PipelineLayout pipelineLayout;
	private static GraphicsPipelineTemplate graphicsPipelineTemplate;
	private static GraphicsPipeline graphicsPipeline;
	private static PipelineLayout computeLayout;
	private static ComputePipeline computePipeline;
	
	private static RenderTest.SpatialMovementData spatialMovementData;
	private static ShaderVariable particleRadius;
	private static ShaderVariable minBound;
	private static ShaderVariable maxBound;
	private static ShaderVariable enlapsedTime;
	
	private static int oldComputeIndex;
	private static int computeIndex;
	
	public static void start() throws VulkanException
	{
		ShaderProgram shaderProgram;
		PreparedShader computeShader;
		try
		{
			PreparedShader vertexShader = loadShader(SHADER + ".vert");
			PreparedShader fragmentShader = loadShader(SHADER + ".frag");
			computeShader = loadShader(SHADER + ".comp");
			shaderProgram = ShaderProgram.create().add(vertexShader, fragmentShader).build();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			KirryVulkan.terminate();
			return;
		}
		
		KirryVulkan.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		KirryVulkan.setWindowHint(GLFW.GLFW_DECORATED, true);
		KirryVulkan.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = VulkanWindow.defaultBuilder()
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.build(1080, 720, "Test");
		
		window.setCallback((GLFWWindowSizeCallbackI) (_, _, _) -> {
			RenderTest.rebuildViewMatrix = true;
		});
		
		spatialMovementData = RenderTest.registerSpatialMovement(window, RenderTest.YRotationSensivity, RenderTest.XRotationSensivity);
		
		particleRadius = ShaderVariable.of(ShaderInputType.FLOAT, "PARTICLE_RADIUS");
		minBound = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "MIN_BOUND");
		maxBound = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "MAX_BOUND");
		enlapsedTime = ShaderVariable.of(ShaderInputType.FLOAT, "ENLAPSED_TIME");
		Device device = window.getDevice();
		
		int size = PARTICLE_COUNT * 8 * 4;
		memoryManager = MemoryManager.getDefault(device);
		
		VulkanBuffer vertexBuffer = memoryManager.allocate(242 * 3 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		VulkanBuffer indexBuffer = memoryManager.allocate(2064 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 3 * window.getFramesCount(), 16 * 4);
		VulkanBuffer colorBuffer = memoryManager.allocate(PARTICLE_COUNT * 4 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		VulkanBuffer vertexStorageBuffer = memoryManager.allocate(window.getFramesCount() * size, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
		storageBuffers = new VulkanBuffer[window.getFramesCount()];
		for (int i = 0; i < storageBuffers.length; i++)
		{
			VulkanBuffer storageBuffer = new VulkanBuffer(vertexStorageBuffer, size * i, size, i == 0 ? vertexStorageBuffer.getUsage() | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT : vertexStorageBuffer.getUsage());
			storageBuffer.setName("StorageBuffer" + i);
			storageBuffers[i] = storageBuffer;
		}
		
		vertexBuffer.setName("VertexBuffer");
		indexBuffer.setName("IndexBuffer");
		uniformBuffer.setName("UniformBuffer");
		colorBuffer.setName("ColorBuffer");
		vertexStorageBuffer.setName("VertexStorageBuffer");
		
		Color[] colors = new Color[] {
			Color.VERY_LIGHT_YELLOW, Color.VERY_LIGHT_RED, Color.VERY_LIGHT_MAGENTA, Color.VERY_LIGHT_GREEN, Color.VERY_LIGHT_CYAN, Color.VERY_LIGHT_BLUE,
			Color.LIGHT_YELLOW, Color.LIGHT_RED, Color.LIGHT_MAGENTA, Color.LIGHT_GREEN, Color.LIGHT_CYAN, Color.LIGHT_BLUE,
			Color.YELLOW, Color.RED, Color.MAGENTA, Color.GREEN, Color.CYAN, Color.BLUE,
			Color.DARK_YELLOW, Color.DARK_RED, Color.DARK_MAGENTA, Color.DARK_GREEN, Color.DARK_CYAN, Color.DARK_BLUE,
			Color.VERY_DARK_YELLOW, Color.VERY_DARK_RED, Color.VERY_DARK_MAGENTA, Color.VERY_DARK_GREEN, Color.VERY_DARK_CYAN, Color.VERY_DARK_BLUE
		};
		
		VulkanFuture<?> transferFuture = device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
			
			try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, vertexBuffer, storageBuffers[0], indexBuffer, colorBuffer))
			{
				DeferredBufferWriter vertexBufferWriter = transfer.getWriter(vertexBuffer);
				DeferredBufferWriter storageBuffer0Writer = transfer.getWriter(storageBuffers[0]);
				DeferredBufferWriter indexBufferWriter = transfer.getWriter(indexBuffer);
				DeferredBufferWriter colorBufferWriter = transfer.getWriter(colorBuffer);
				
				Random random = new Random();
				
				SphereDataCollector.collect(vertexBufferWriter, indexBufferWriter, Vector3fr.of(), 0.06F, 8);
				
				for (float i = -DIM; i < DIM; i++)
				{
					for (float j = -DIM; j < DIM; j++)
					{
						for (float k = -DIM; k < DIM; k++)
						{
							Color c = colors[random.nextInt(colors.length)];
							storageBuffer0Writer.put(i / 10).put(j / 10).put(k / 10).put(0);
							storageBuffer0Writer.put(random.nextFloat(-0.1F, 0.1F)).put(random.nextFloat(-0.1F, 0.1F)).put(random.nextFloat(-0.1F, 0.1F)).put(0);
							colorBufferWriter.put(c.r() / 255).put(c.g() / 255).put(c.b() / 255).put(c.a() / 255);
						}
					}
				}
			}
		});
		
		try (RenderPassTemplate template = RenderPassTemplate.initColorDepthTextureTemplate(window.getSurface().getData().format().format(), window.getDepthTexture().getFormat()))
		{
			renderPass = template.createRenderPass(device);
		}
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.maxSets(2 * window.getFramesCount())
				.pPoolSizes(VkDescriptorPoolSize.malloc(2, stack)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(3 * window.getFramesCount())
					)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
						.descriptorCount(2 * window.getFramesCount())
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
			DescriptorSet[] descriptorSets = descriptorPool.allocates(descriptorSetLayout, window.getFramesCount());
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			graphicsPipelineTemplate = GraphicsPipelineTemplate.create3dRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 12, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addBinding(1, 32, VK10.VK_VERTEX_INPUT_RATE_INSTANCE)
					.addBinding(2, 16, VK10.VK_VERTEX_INPUT_RATE_INSTANCE)
					.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(1, 1, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(2, 2, ShaderInputType.VECTOR_FLOAT_4, 0)
				)
				.configureRasterizationState(s -> s
					.frontFace(VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE)
				)
				.build();
			graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, window, pipelineLayout, renderPass, 0);
			
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSets)
				.buffersCount(window.getFramesCount())
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 2 * i, 1, 16 * 4), VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, List.of(
						ShaderVariable.PROJECTION
					)),
					BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 2 * i + 1, 1, 4), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, List.of(
						particleRadius
					))
				})
				.build();
			renderer.setBuffer(0, vertexBuffer);
			renderer.setBuffer(2, colorBuffer);
			renderer.setIndexBuffer(indexBuffer);
			
			for (int i = 0; i < window.getFramesCount(); i++)
			{
				renderer.useBufferLayoutWriter(i, 1, writer -> {
					writer.set(particleRadius, 0.1F);
					writer.flush();
				});
			}
			RenderTest.rebuildViewMatrix = true;
			
			createInfo = VkDescriptorSetLayoutCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.pBindings(VkDescriptorSetLayoutBinding.malloc(3, stack)
					.apply(b -> b
						.binding(0)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT)
						.pImmutableSamplers(null))
					.apply(b -> b
						.binding(1)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT)
						.pImmutableSamplers(null))
					.apply(b -> b
						.binding(2)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT)
						.pImmutableSamplers(null))
					.rewind()
				);
			
			descriptorSetLayout = new DescriptorSetLayout(device, createInfo);
			pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			computeLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			computePipeline = new ComputePipeline(device, computeLayout, computeShader);
			descriptorSets = descriptorPool.allocates(descriptorSetLayout, window.getFramesCount());
			computer = Computer.builder()
				.computePipeline(computePipeline)
				.descriptorSets(descriptorSets)
				.buffersCount(window.getFramesCount())
				.groupCount(Vector3i.of(256, 1, 1))
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, i + 2 * window.getFramesCount(), 1, 8 * 4), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						minBound,
						maxBound,
						enlapsedTime
					)),
					BindingInfo.ofStorage(VulkanBuffer.view(storageBuffers[0]), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						ShaderVariable.POSITION,
						ShaderVariable.VELOCITY,
						ShaderVariable.COLOR
					)),
					BindingInfo.ofStorage(VulkanBuffer.view(storageBuffers[1]), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						ShaderVariable.POSITION,
						ShaderVariable.VELOCITY,
						ShaderVariable.COLOR
					))
				})
				.build();
			
			for (int i = 0; i < window.getFramesCount(); i++)
			{
				computer.useBufferLayoutWriter(i, 0, writer -> {
					writer.set(minBound, MIN_BOUND_VALUE);
					writer.set(maxBound, MAX_BOUND_VALUE);
					writer.set(enlapsedTime, 1F);
					writer.flush();
				});
			}
		}
		oldComputeIndex = 0;
		computeIndex = 1;
		
		transferFuture.join();
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanWindow window = KirryVulkan.getVulkan().getWindow();
			switch (window.startPresentation())
			{
				case EMPTY_SURFACE:
					return;
				case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
					throw new VulkanException("Presentaton error");
			}
			
			calculatePosition(spatialMovementData, enlapsed);
			
			int frameIndex = window.getFrameIndex();
			
			Long oldBufferVk = storageBuffers[oldComputeIndex].getVk();
			Long newBufferVk = storageBuffers[computeIndex].getVk();
			computer.useBufferLayoutWriter(computeIndex, 0, writer -> {
				writer.set(enlapsedTime, enlapsed / 1000000000F);
				writer.flush();
			});
			computer.updateDescriptorSet(computeIndex, sets -> {
				((VkDescriptorBufferInfo.@NonNull Buffer) sets.get(1).pBufferInfo()).buffer(oldBufferVk);
				((VkDescriptorBufferInfo.@NonNull Buffer) sets.get(2).pBufferInfo()).buffer(newBufferVk);
			});
			
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
			
			if (RenderTest.rebuildViewMatrix)
			{
				Matrix4f matrix = new Matrix4f();
				matrix.multiply(new Matrix4f().projectPositiveZ(AngleUnit.RADIANS.fromDegrees(120), 10, 0.1F, (float) window.getWidth() / window.getHeight()));
				matrix.multiply(new Matrix4f().scale(Vector3f.of(1, -1, 1)));
				matrix.multiply(new Matrix4f().view(spatialMovementData.position, spatialMovementData.xRotation, spatialMovementData.yRotation));
				
				for (int i = 0; i < window.getFramesCount(); i++)
				{
					renderer.useBufferLayoutWriter(i, 0, writer -> {
						writer.set(ShaderVariable.PROJECTION, matrix);
						writer.flush();
					});
				}
				RenderTest.rebuildViewMatrix = false;
			}
			
			CommandBuffer commandBuffer = window.getCurrentCommandBuffer();
			commandBuffer.beginRecording();
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.setBuffer(1, storageBuffers[computeIndex]);
			renderer.draw(commandBuffer, frameIndex, PARTICLE_COUNT);
			commandBuffer.endRenderPass();
			computer.dispatch(commandBuffer, computeIndex, PARTICLE_COUNT, 1, 1);
			commandBuffer.endRecording();
			
			oldComputeIndex = computeIndex++;
			computeIndex = computeIndex % storageBuffers.length;
			window.endPresentation(commandBuffer);
		}
	}
	
	public static void end()
	{
		graphicsPipeline.close();
		graphicsPipelineTemplate.close();
		pipelineLayout.close();
		computePipeline.close();
		computeLayout.close();
		memoryManager.close();
		uniformBuffer.close();
		
		KirryVulkan.terminate();
	}
}
