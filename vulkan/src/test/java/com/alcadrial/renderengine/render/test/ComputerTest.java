package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.axis.Axis;
import com.alcadrial.renderengine.core.cursor.CursorInputMode;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Computer;
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
import com.alcadrial.renderengine.render.pipeline.ComputePipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderUtils;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.binding.BindingInfo;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.util.SphereDataCollector;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class ComputerTest extends RenderTest {
	
	private static final String SHADER = "shader3";
	
	public static void main(String[] args)
	{
		mainTest(args, ComputerTest::start, () -> defaultAnimationLoop(ComputerTest::loop), ComputerTest::end, false);
	}
	
	private static final int DIM = 8;
	private static final int PARTICLE_COUNT = 8 * DIM * DIM * DIM;
	private static final Vector3f MIN_BOUND_VALUE = Vector3f.of(-2, -2, -2);
	private static final Vector3f MAX_BOUND_VALUE = Vector3f.of(2, 2, 2);
	
	private static final Vector3f SOUTH = Axis.Z;
	private static final Vector3f NORTH = Axis.NEGATIVE_Z;
	private static final Vector3f EAST = Axis.X;
	private static final Vector3f WEST = Axis.NEGATIVE_X;
	private static final Vector3f UP = Axis.Y;
	private static final Vector3f DOWN = Axis.NEGATIVE_Y;
	
	private static final float XRotationSensivity = 0.005F;
	private static final float YRotationSensivity = 0.01F;
	
	private static RenderPass renderPass;
	private static Renderer renderer;
	private static Computer computer;
	private static StaticBigBuffer staticBigBuffer;
	private static VulkanBuffer[] storageBuffers;
	private static VulkanBuffer uniformBuffer;
	
	private static Vector3f cameraPosition;
	private static Vector3f cameraMovement;
	private static float cameraXRotation;
	private static float cameraYRotation;
	private static boolean isCameraUnlocked;
	
	private static boolean rebuildProjectionMatrix;
	private static PipelineLayout pipelineLayout;
	private static GraphicsPipelineTemplate graphicsPipelineTemplate;
	private static GraphicsPipeline graphicsPipeline;
	private static PipelineLayout computeLayout;
	private static ComputePipeline computePipeline;
	
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
			RenderEngine.terminate();
			return;
		}
		
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = VulkanWindow.defaultBuilder()
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.build(1080, 720, "Test");
		
		window.setCallback((GLFWWindowSizeCallbackI) (_, _, _) -> {
			rebuildProjectionMatrix = true;
		});
		
		cameraPosition = Vector3f.of(0, 0, 1);
		cameraMovement = Vector3f.of(0, 0, 0);
		cameraXRotation = 0;
		cameraYRotation = 0;
		isCameraUnlocked = false;
		
		Vector2f oldMousePos = Vector2f.of(0, 0);
		boolean[] pressing = new boolean[2];
		window.setCallback((GLFWMouseButtonCallbackI) (_, button, action, _) -> {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
			{
				if (pressing[0] = action == GLFW.GLFW_PRESS)
				{
					window.getMousePos(oldMousePos);
				}
			}
		});
		window.setCallback((GLFWCursorPosCallbackI) (_, x, y) -> {
			if (isCameraUnlocked)
			{
				cameraXRotation += (y - oldMousePos.y()) * YRotationSensivity;
				if (cameraXRotation > AngleUnit.RADIANS.fromDegrees(90)) cameraXRotation = AngleUnit.RADIANS.fromDegrees(90);
				else if (cameraXRotation < AngleUnit.RADIANS.fromDegrees(-90)) cameraXRotation = -AngleUnit.RADIANS.fromDegrees(90);
				cameraYRotation += (x - oldMousePos.x()) * XRotationSensivity;
				rebuildProjectionMatrix = true;
			}
			window.getMousePos(oldMousePos);
		});
		
		window.setCallback((GLFWKeyCallbackI) (_, k, _, a, _) -> {
			
			if (isCameraUnlocked)
			{
				Vector3f v = switch (k)
				{
					case GLFW.GLFW_KEY_W -> NORTH;
					case GLFW.GLFW_KEY_A -> WEST;
					case GLFW.GLFW_KEY_S -> SOUTH;
					case GLFW.GLFW_KEY_D -> EAST;
					case GLFW.GLFW_KEY_SPACE -> UP;
					case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_CONTROL -> DOWN;
					default -> Vector3f.origin();
				};
				
				switch (a)
				{
					case GLFW.GLFW_PRESS -> cameraMovement.add(v);
					case GLFW.GLFW_RELEASE -> cameraMovement.subtract(v);
				}
			}
			
			if (a == GLFW.GLFW_RELEASE)
			{
				switch (k)
				{
					case GLFW.GLFW_KEY_U -> {
						isCameraUnlocked = !isCameraUnlocked;
						window.setCursorInputMode(isCameraUnlocked ? CursorInputMode.DISABLED : CursorInputMode.NORMAL);
					}
					case GLFW.GLFW_KEY_P -> {
						cameraPosition.setAll(0, 0, 0);
						rebuildProjectionMatrix = true;
					}
				}
			}
		});
		
		particleRadius = ShaderVariable.of(ShaderInputType.FLOAT, "PARTICLE_RADIUS");
		minBound = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "MIN_BOUND");
		maxBound = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "MAX_BOUND");
		enlapsedTime = ShaderVariable.of(ShaderInputType.FLOAT, "ENLAPSED_TIME");
		Device device = window.getDevice();
		
		int size = PARTICLE_COUNT * 8 * 4;
		staticBigBuffer = new StaticBigBuffer(device, 1 << 20);
		VulkanBuffer vertexBuffer = staticBigBuffer.allocate(242 * 3 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		VulkanBuffer indexBuffer = staticBigBuffer.allocate(2064 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 3 * window.getFramesCount());
		uniformBuffer.getMemory().mapMemory();
		VulkanBuffer colorBuffer = staticBigBuffer.allocate(PARTICLE_COUNT * 4 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		VulkanBuffer vertexStorageBuffer = staticBigBuffer.allocate(window.getFramesCount() * size, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
		storageBuffers = new VulkanBuffer[window.getFramesCount()];
		for (int i = 0; i < storageBuffers.length; i++)
		{
			VulkanBuffer storageBuffer = new VulkanBuffer(vertexStorageBuffer, size * i, size);
			storageBuffer.setName("StorageBuffer" + i);
			storageBuffers[i] = storageBuffer;
		}
		
		staticBigBuffer.setName("StaticBigBuffer");
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
		
		try (PhysicalDeviceTransferState transfert = staticBigBuffer.startLocalBuffering())
		{
			BufferWriter vertexBufferWriter = transfert.beginTransfer(vertexBuffer);
			BufferWriter storageBuffer0Writer = transfert.beginTransfer(storageBuffers[0]);
			BufferWriter indexBufferWriter = transfert.beginTransfer(indexBuffer);
			BufferWriter colorBufferWriter = transfert.beginTransfer(colorBuffer);
			
			Random random = new Random();
			
			SphereDataCollector.collect(vertexBufferWriter, indexBufferWriter, Vector3f.origin(), 0.06F, 8);
			
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
				.maxSets(2 * window.getFramesCount())
				.pPoolSizes(VkDescriptorPoolSize.malloc(2, stack)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(3)
					)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
						.descriptorCount(2)
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
					.addAttribute(0, 0, 0)
					.addAttribute(1, 1, 0)
					.addAttribute(2, 2, 0)
				)
				.build();
			graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, window, pipelineLayout, renderPass, 0);
			
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSets)
				.buffersCount(window.getFramesCount())
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 2 * i, 1), VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, List.of(
						ShaderVariable.PROJECTION
					)),
					BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 2 * i + 1, 1), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, List.of(
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
					writer.flushAndInvalidate();
				});
			}
			rebuildProjectionMatrix = true;
			
			VkPipelineShaderStageCreateInfo shaderCreateInfo = ShaderUtils.createPipelineShaderStageCreateInfoBuffer(stack, device, computeShader);
			
			try
			{
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
				
				VkComputePipelineCreateInfo computeCreateInfo = VkComputePipelineCreateInfo.malloc(stack)
					.sType(VK10.VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.stage(shaderCreateInfo)
					.layout(computeLayout.getVk())
					.basePipelineHandle(0)
					.basePipelineIndex(0);
				
				computePipeline = new ComputePipeline(device, computeLayout, computeCreateInfo);
				descriptorSets = descriptorPool.allocates(descriptorSetLayout, window.getFramesCount());
				computer = Computer.builder()
					.computePipeline(computePipeline)
					.descriptorSets(descriptorSets)
					.buffersCount(window.getFramesCount())
					.groupCount(Vector3i.of(256, 1, 1))
					.bindings(i -> new BindingInfo[] {
						BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, i + 2 * window.getFramesCount(), 1), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
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
			}
			finally
			{
				VK10.vkDestroyShaderModule(device.getVk(), shaderCreateInfo.module(), null);
			}
			
			for (int i = 0; i < window.getFramesCount(); i++)
			{
				computer.useBufferLayoutWriter(i, 0, writer -> {
					writer.set(minBound, MIN_BOUND_VALUE);
					writer.set(maxBound, MAX_BOUND_VALUE);
					writer.set(enlapsedTime, 1F);
					writer.flushAndInvalidate();
				});
			}
		}
		oldComputeIndex = 0;
		computeIndex = 1;
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
			
			if (!cameraMovement.equals(Vector3f.origin()))
			{
				float sin = Math.sin(cameraYRotation);
				float cos = Math.cos(cameraYRotation);
				Vector3f movement = Vector3f.of(0, 0, 0);
				if (cameraMovement.z() == 1) movement.add(-sin, 0, cos);
				if (cameraMovement.x() == -1) movement.add(-cos, 0, -sin);
				if (cameraMovement.z() == -1) movement.add(sin, 0, -cos);
				if (cameraMovement.x() == 1) movement.add(cos, 0, sin);
				movement.add(0, cameraMovement.y(), 0);
				
				cameraPosition.add(movement.multiplyAll(enlapsed / 1000000000F));
				rebuildProjectionMatrix = true;
			}
			
			int frameIndex = window.getFrameIndex();
			
			Long oldBufferVk = storageBuffers[oldComputeIndex].getVk();
			Long newBufferVk = storageBuffers[computeIndex].getVk();
			computer.useBufferLayoutWriter(computeIndex, 0, writer -> {
				writer.set(enlapsedTime, enlapsed / 1000000000F);
				writer.flushAndInvalidate();
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
			
			if (rebuildProjectionMatrix)
			{
				Matrix4f matrix = new Matrix4f();
				matrix.multiply(new Matrix4f().projectPositiveZ(AngleUnit.RADIANS.fromDegrees(120), 10, 0.1F, (float) window.getWidth() / window.getHeight()));
				matrix.multiply(new Matrix4f().scale(Vector3f.of(1, -1, 1)));
				matrix.multiply(new Matrix4f().view(cameraPosition, cameraXRotation, cameraYRotation));
				
				for (int i = 0; i < window.getFramesCount(); i++)
				{
					renderer.useBufferLayoutWriter(i, 0, writer -> {
						writer.set(ShaderVariable.PROJECTION, matrix);
						writer.flushAndInvalidate();
					});
				}
				rebuildProjectionMatrix = false;
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
		staticBigBuffer.close();
		uniformBuffer.close();
		
		RenderEngine.terminate();
	}
}
