package com.alcadrial.renderengine.render.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.math.RoundingMode;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDepthRangeUnrestricted;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.angle.Angle;
import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.axis.Axis;
import com.alcadrial.renderengine.core.cursor.CursorInputMode;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.ComputePipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
import com.alcadrial.renderengine.render.util.SphereDataCollector;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class ComputeTest extends RenderTest {
	
	private static final String SHADER = "shader3";
	private static final String PROJECTION = "PROJECTION";
	private static final String PARTICLE_RADIUS = "PARTICLE_RADIUS";
	private static final String MIN_BOUND = "MIN_BOUND";
	private static final String MAX_BOUND = "MAX_BOUND";
	private static final String ENLAPSED_TIME = "ENLAPSED_TIME";
	
	public static void main(String[] args)
	{
		mainTest(args, ComputeTest::start, ComputeTest::loop, ComputeTest::end);
	}
	
	private static final int DIM = 8;
	private static final int PARTICLE_COUNT = 2 * DIM * DIM * DIM;
	private static final Vector3f MIN_BOUND_VALUE = Vector3f.of(-2, -2, -2);
	private static final Vector3f MAX_BOUND_VALUE = Vector3f.of(2, 2, 2);
	
	private static final Vector3f NORTH = Axis.Z;
	private static final Vector3f SOUTH = Axis.NEGATIVE_Z;
	private static final Vector3f WEST = Axis.X;
	private static final Vector3f EAST = Axis.NEGATIVE_X;
	private static final Vector3f UP = Axis.Y;
	private static final Vector3f DOWN = Axis.NEGATIVE_Y;
	
	private static long fps;
	private static RenderPass renderPass;
	private static Renderer renderer;
	private static VertexBuffer vertexBuffer;
	private static VertexBuffer vertexStorageBuffer;
	private static VertexBuffer storageBuffer0;
	private static VertexBuffer storageBuffer1;
	private static IndexBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static int I;
	
	private static long computeDescriptorSetLayout;
	private static long computeDescriptorSet;
	private static long computeDescriptorPool;
	private static Lock computeDescriptorSetLock;
	private static BufferLayout computeUniformBufferLayout;
	private static BufferLayout computeStorageBufferLayout;
	private static ComputePipeline computePipeline;
	
	private static Vector3f cameraPosition;
	private static Vector3f cameraMovement;
	private static Angle cameraXRotation;
	private static Angle cameraYRotation;
	private static boolean isCameraUnlocked;
	private static double enlapsedTimeMillis;
	private static double enlapsedTimeSeconds;
	
	private static boolean rebuildProjectionMatrix;
	
	private static Matrix4f buildProjectionMatrix(VulkanWindow window)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().perspective(Angle.of(120, AngleUnit.DEGREES), 1000, 0.0001F, (float) window.getWidth() / window.getHeight()));
		matrix.multiply(new Matrix4f().view(cameraPosition, cameraXRotation.asFloat(AngleUnit.DEGREES), cameraYRotation.asFloat(AngleUnit.DEGREES)));
		return matrix.transpose();
	}
	
	public static void start()
	{
		ShaderProgram shaderProgram;
		PreparedShader computeShader;
		try
		{
			shaderProgram = ShaderProgram.create().add(loadShader(SHADER + ".vert"), loadShader(SHADER + ".frag")).build();
			computeShader = loadShader(SHADER + ".comp");
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		Debug.onDebug(() -> System.out.println("Debug active"));
		
		Vulkan vulkan = RenderEngine.getVulkan();
		
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = new VulkanWindow(1080, 720, "Test");
		vulkan.setWindow(window);
		
		if (!pickPhysicalDevice(window)) return;
		
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
		deviceFeatures.wideLines(true);
		deviceFeatures.samplerAnisotropy(true);
		Device device = vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME, EXTDepthRangeUnrestricted.VK_EXT_DEPTH_RANGE_UNRESTRICTED_EXTENSION_NAME);
		window.init(device);
		
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			renderer.updateUniformBuffers((b, l) -> l.set(PROJECTION, b, buildProjectionMatrix(window)));
		});
		
		cameraPosition = Vector3f.of(0, 0, -1);
		cameraMovement = Vector3f.of(0, 0, 0);
		cameraXRotation = Angle.of(0, AngleUnit.RADIANS);
		cameraYRotation = Angle.of(0, AngleUnit.RADIANS);
		isCameraUnlocked = false;
		
		Vector2i[] oldPos = new Vector2i[] {Vector2i.of(0, 0)};
		Vector2f[] oldMousePos = new Vector2f[] {Vector2f.of(0, 0), Vector2f.of(0, 0)};
		boolean[] pressing = new boolean[2];
		window.setCallback((GLFWMouseButtonCallbackI) (w, button, action, mods) -> {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) if (pressing[0] = action == GLFW.GLFW_PRESS) window.getMousePos(oldMousePos[0]);
		});
		window.setCallback((GLFWCursorPosCallbackI) (w, x, y) -> {
			if (pressing[0] && !pressing[1])
			{
				pressing[1] = true;
				window.getPos(oldPos[0]);
				window.setPos(Vector2i.of((float) x - oldMousePos[0].x() + oldPos[0].x(), (float) y - oldMousePos[0].y() + oldPos[0].y(), RoundingMode.FLOOR));
				pressing[1] = false;
			}
			if (isCameraUnlocked)
			{
				cameraXRotation.add(Angle.of(y - oldMousePos[0].y(), AngleUnit.DEGREES));
				if (cameraXRotation.asDouble(AngleUnit.DEGREES) > 90) cameraXRotation.setValue(AngleUnit.DEGREES, 90);
				else if (cameraXRotation.asDouble(AngleUnit.DEGREES) < -90) cameraXRotation.setValue(AngleUnit.DEGREES, -90);
				cameraYRotation.add(Angle.of(x - oldMousePos[0].x(), AngleUnit.DEGREES));
				rebuildProjectionMatrix = true;
			}
			window.getMousePos(oldMousePos[0]);
		});
		
		window.setCallback((GLFWKeyCallbackI) (w, k, s, a, m) -> {
			
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
					case GLFW.GLFW_KEY_R -> {
						renderer.updateUniformBuffers((b, l) -> {
							l.set(PARTICLE_RADIUS, b, 0.01F);
						});
					}
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
		
		int minOffsetAlignment = (int) device.getPhysicalDevice().getLimits().minUniformBufferOffsetAlignment();
		System.out.println("MinOffset = " + minOffsetAlignment);
		
		{
			int size = PARTICLE_COUNT * 12 * 4;
			vertexBuffer = new VertexBuffer(242 * 3);
			vertexStorageBuffer = new VertexBuffer(2 * size, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, true);
			storageBuffer0 = new VertexBuffer(vertexStorageBuffer, 0, size);
			storageBuffer1 = new VertexBuffer(vertexStorageBuffer, size, size);
			uniformBuffer = new VulkanBuffer(device, Math.max(minOffsetAlignment * 3, (16 + 16 + 3 + 1 + 1) * 4), VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
		}
		renderPass = RenderPassTemplate.initColorDepthTextureTemplate(window.getSurface().getFormat().format()).createRenderPass(device);
		
		Renderer.Builder builder = Renderer.create();
		builder.shaderProgram(shaderProgram);
		builder.depthTestEnable(true);
		builder.cullMode(VK10.VK_CULL_MODE_NONE);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		builder.uniformBuffer(uniformBuffer);
		
		renderer = builder.build(device, window.getSurface().getSize(), renderPass, 3, ComputeCustomGraphicsPipeline::new);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkDescriptorPoolCreateInfo poolInfo = VulkanUtils.descriptorPoolCreateInfo(stack, VulkanUtils.descriptorPoolSize(stack, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 4));// static
			poolInfo.maxSets(3);
			
			long[] p0 = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorPool(device.getVk(), poolInfo, null, p0), "Failed to create descriptor pool!");
			computeDescriptorPool = p0[0];
			
			VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = VkDescriptorSetLayoutBinding.malloc(3, stack);// static
			
			VkDescriptorSetLayoutBinding descriptorSetLayoutBinding = descriptorSetLayoutBindings.get();
			descriptorSetLayoutBinding.binding(0);
			descriptorSetLayoutBinding.descriptorCount(1);
			descriptorSetLayoutBinding.descriptorType(ShaderLayout.Type.UNIFORM_BUFFER.getType());
			descriptorSetLayoutBinding.pImmutableSamplers(null);
			descriptorSetLayoutBinding.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT);
			
			descriptorSetLayoutBinding = descriptorSetLayoutBindings.get();
			descriptorSetLayoutBinding.binding(1);
			descriptorSetLayoutBinding.descriptorCount(1);
			descriptorSetLayoutBinding.descriptorType(ShaderLayout.Type.STORAGE_BUFFER.getType());
			descriptorSetLayoutBinding.pImmutableSamplers(null);
			descriptorSetLayoutBinding.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT);
			
			descriptorSetLayoutBinding = descriptorSetLayoutBindings.get();
			descriptorSetLayoutBinding.binding(2);
			descriptorSetLayoutBinding.descriptorCount(1);
			descriptorSetLayoutBinding.descriptorType(ShaderLayout.Type.STORAGE_BUFFER.getType());
			descriptorSetLayoutBinding.pImmutableSamplers(null);
			descriptorSetLayoutBinding.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT);
			
			descriptorSetLayoutBindings.rewind();
			
			LongBuffer p = stack.callocLong(1);
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorSetLayout(device.getVk(), VulkanUtils.descriptorSetLayoutInfo(stack, 0, descriptorSetLayoutBindings), null, p), "Failed to create descriptor set layout!");
			
			p.rewind();
			
			computeDescriptorSetLayout = p.get(0);
			
			long[] p1 = new long[1];
			
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.getVk(), VulkanUtils.descriptorSetAllocateInfo(stack, computeDescriptorPool, p), p1), "Failed to allocate descriptor sets!");
			
			computeDescriptorSet = p1[0];
			computeDescriptorSetLock = new ReentrantLock();
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			pipelineLayoutInfo.pSetLayouts(p);
			computePipeline = new ComputePipeline(device, new PipelineLayout(device, pipelineLayoutInfo), true, computeShader);
			
			VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.malloc(3);
			
			computeUniformBufferLayout = new BufferLayout(device, writeDescriptorSets.slice(0, 1), 0, computeDescriptorSetLock);
			computeStorageBufferLayout = new BufferLayout(device, writeDescriptorSets.slice(1, 2), 0, computeDescriptorSetLock);
			
			VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
			writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
			writeDescriptorSet.pNext(0);
			writeDescriptorSet.dstSet(p1[0]);
			writeDescriptorSet.dstBinding(0);
			writeDescriptorSet.dstArrayElement(0);
			writeDescriptorSet.descriptorCount(1);
			writeDescriptorSet.descriptorType(ShaderLayout.Type.UNIFORM_BUFFER.getType());
			
			int offset = 512;
			int partialOffset = 0;
			
			VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
			descriptorBufferInfo.buffer(uniformBuffer.getVk());
			descriptorBufferInfo.offset(offset);
			for (ShaderVariable variable : new ShaderVariable[] {
				ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, MIN_BOUND),
				ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, MAX_BOUND),
				ShaderVariable.of(ShaderInputType.FLOAT, ENLAPSED_TIME),
			})
			{
				int size = variable.getType().getLength();
				if (partialOffset > 0 && partialOffset + size > 16) offset += 16 - partialOffset;
				computeUniformBufferLayout.mark(variable, offset / 4);
				offset += size;
				partialOffset = offset % 16;
			}
			descriptorBufferInfo.range(offset - descriptorBufferInfo.offset());
			
			writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
			writeDescriptorSet.pImageInfo(null);
			writeDescriptorSet.pTexelBufferView(null);
			
			writeDescriptorSet = writeDescriptorSets.get();
			writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
			writeDescriptorSet.pNext(0);
			writeDescriptorSet.dstSet(p1[0]);
			writeDescriptorSet.dstBinding(1);
			writeDescriptorSet.dstArrayElement(0);
			writeDescriptorSet.descriptorCount(1);
			writeDescriptorSet.descriptorType(ShaderLayout.Type.STORAGE_BUFFER.getType());
			descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
			descriptorBufferInfo.buffer(storageBuffer0.getGpuBuffer().getVk());
			descriptorBufferInfo.offset(0);
			descriptorBufferInfo.range(4 * 12 * PARTICLE_COUNT);
			writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
			writeDescriptorSet.pImageInfo(null);
			writeDescriptorSet.pTexelBufferView(null);
			
			writeDescriptorSet = writeDescriptorSets.get();
			writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
			writeDescriptorSet.pNext(0);
			writeDescriptorSet.dstSet(p1[0]);
			writeDescriptorSet.dstBinding(2);
			writeDescriptorSet.dstArrayElement(0);
			writeDescriptorSet.descriptorCount(1);
			writeDescriptorSet.descriptorType(ShaderLayout.Type.STORAGE_BUFFER.getType());
			descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
			descriptorBufferInfo.buffer(storageBuffer1.getGpuBuffer().getVk());
			descriptorBufferInfo.offset(0);
			descriptorBufferInfo.range(4 * 12 * PARTICLE_COUNT);
			writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
			writeDescriptorSet.pImageInfo(null);
			writeDescriptorSet.pTexelBufferView(null);
			
			writeDescriptorSets.rewind();
			
			computeUniformBufferLayout.set(MIN_BOUND, uniformBuffer, MIN_BOUND_VALUE);
			computeUniformBufferLayout.set(MAX_BOUND, uniformBuffer, MAX_BOUND_VALUE);
			computeUniformBufferLayout.set(ENLAPSED_TIME, uniformBuffer, 0F);
			
			computeUniformBufferLayout.updateDescriptorSets();
			computeStorageBufferLayout.updateDescriptorSets();
			
			System.out.println("SUCCESS");
		}
		
		indexBuffer = new IndexBuffer(2064);
		renderer.addVertexBuffer(0, vertexBuffer.getGpuBuffer());
		renderer.addVertexBuffer(1, storageBuffer0.getGpuBuffer());
		renderer.setIndexBuffer(indexBuffer.getGpuBuffer());
		renderer.updateUniformBuffers((b, l) -> {
			l.set(PROJECTION, b, buildProjectionMatrix(window));
		});
		
		SphereDataCollector.collect(vertexBuffer, indexBuffer, Vector3f.origin(), 0.06F, 8);
		
		Color[] colors = new Color[] {
			Color.VERY_LIGHT_YELLOW,
			Color.VERY_LIGHT_RED,
			Color.VERY_LIGHT_MAGENTA,
			Color.VERY_LIGHT_GREEN,
			Color.VERY_LIGHT_CYAN,
			Color.VERY_LIGHT_BLUE,
			Color.LIGHT_YELLOW,
			Color.LIGHT_RED,
			Color.LIGHT_MAGENTA,
			Color.LIGHT_GREEN,
			Color.LIGHT_CYAN,
			Color.LIGHT_BLUE,
			Color.YELLOW,
			Color.RED,
			Color.MAGENTA,
			Color.GREEN,
			Color.CYAN,
			Color.BLUE,
			Color.DARK_YELLOW,
			Color.DARK_RED,
			Color.DARK_MAGENTA,
			Color.DARK_GREEN,
			Color.DARK_CYAN,
			Color.DARK_BLUE,
			Color.VERY_DARK_YELLOW,
			Color.VERY_DARK_RED,
			Color.VERY_DARK_MAGENTA,
			Color.VERY_DARK_GREEN,
			Color.VERY_DARK_CYAN,
			Color.VERY_DARK_BLUE
		};
		
		Random random = new Random();
		
		FloatBuffer cursor = storageBuffer0.asFloatBuffer();
		for (float i = -DIM; i < DIM; i++)
		{
			for (float j = -DIM; j < DIM; j++)
			{
				for (float k = -DIM; k < DIM; k++)
				{
					Color c = colors[random.nextInt(colors.length)];
					cursor.put(i / 10).put(j / 10).put(k / 10).put(0);
					cursor.put(random.nextFloat(-0.1F, 0.1F)).put(random.nextFloat(-0.1F, 0.1F)).put(random.nextFloat(-0.1F, 0.1F)).put(0);
					cursor.put(c.r() / 255).put(c.g() / 255).put(c.b() / 255).put(c.a() / 255);
				}
			}
		}
		
		try
		{
			vertexBuffer.sendToPhysicalDevice().get();
			indexBuffer.sendToPhysicalDevice().get();
			storageBuffer0.sendToPhysicalDevice().get();
			storageBuffer0.sendToPhysicalDevice(storageBuffer1.getGpuBuffer()).get();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		printState();
	}
	
	private static int stateIndex = 1000;
	
	private static void printState()
	{
		System.out.println("PrintState" + stateIndex);
		try (PrintStream stream = new PrintStream("state/state" + stateIndex++ + ".txt");)
		{
			Debug.setDebugOutput(stream);
			Debug.printState();
			Debug.printState("RenderPass", renderPass);
			Debug.printState("Renderer", renderer);
			
			Debug.print("ComputePipeline ", computePipeline);
			Debug.print("GroupCount: " + 256 + "x" + 1 + "x" + 1);
			
			Debug.print("InputBuffer", storageBuffer0);
			//Debug.printState("InputLayout", inputLayout);
			Debug.print("OutputBuffer", storageBuffer1);
			//Debug.printState("OutputLayout", outputLayout);
			Debug.print("UniformBuffer", uniformBuffer);
			Debug.print("DescriptorPool: " + Debug.address(computeDescriptorPool));
			Debug.print("DescriptorSetLayout: " + Debug.address(computeDescriptorSetLayout));
			Debug.print("DescriptorSet: " + Debug.address(computeDescriptorSet));
			Debug.printState("ComputeUniformLayout", computeUniformBufferLayout);
			Debug.printState("ComputeStorageLayout", computeStorageBufferLayout);
			Debug.setDebugOutput(System.out);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace(System.out);
		}
	}
	
	private static long time;
	
	public static void loop()
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
		long firstHighMilestone = 1000000000;
		long firstMilestone = 1000000000 / maxfps;
		
		time = System.nanoTime();
		long current;
		long frame = 0;
		long milestone = firstMilestone;
		long highMilestone = firstHighMilestone;
		
		while (!window.shouldClose())
		{
			current = System.nanoTime();
			long enlapsed = current - time;
			long pauseTime = milestone - enlapsed;
			if (pauseTime > 0)
			{
				try
				{
					synchronized (window)
					{
						window.wait(pauseTime / 1000000, (int) (pauseTime % 1000000));
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace(System.out);
				}
				continue;
			}
			milestone += (1 - pauseTime / firstMilestone) * firstMilestone;
			if (enlapsed >= highMilestone)
			{
				fps = frame;
				frame = 0;
				highMilestone += (enlapsed - highMilestone + firstHighMilestone) / firstHighMilestone * firstHighMilestone;
			}
			enlapsedTimeMillis = (firstMilestone - pauseTime) / 1000000D;
			enlapsedTimeSeconds = enlapsedTimeMillis / 1000;
			println("Frame " + frame, ComputeTest::frame, TestProperties.PRINT_FRAME_TIMES);
			frame++;
		}
	}
	
	@SuppressWarnings("null")
	public static void frame()
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		if (!cameraMovement.equals(Vector3f.origin()))
		{
			float angle = cameraYRotation.asFloat(AngleUnit.RADIANS);
			float sin = Math.sin(angle);
			float cos = Math.cos(angle);
			Vector3f movement = Vector3f.of(0, 0, 0);
			if (cameraMovement.z() == 1) movement.add(sin, 0, -cos);
			if (cameraMovement.x() == -1) movement.add(cos, 0, sin);
			if (cameraMovement.z() == -1) movement.add(-sin, 0, cos);
			if (cameraMovement.x() == 1) movement.add(-cos, 0, -sin);
			movement.add(0, cameraMovement.y(), 0);
			
			cameraPosition.add(movement.multiplyAll((float) enlapsedTimeSeconds));
			rebuildProjectionMatrix = true;
		}
		String title = "Test fps=" + fps;
		if (isCameraUnlocked) title += ", pos=" + cameraPosition + ", vel=" + cameraMovement + ", angles={" + cameraXRotation.asFloatBounded(AngleUnit.DEGREES) + ", " + cameraYRotation.asFloatBounded(AngleUnit.DEGREES) + "}";
		window.setTitle(title);
		
		if (rebuildProjectionMatrix) renderer.updateUniformBuffers((b, l) -> l.set(PROJECTION, b, buildProjectionMatrix(window)));
		
		window.pollEvents();
		if (!window.isIconified())
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				CommandBuffer commandBuffer = window.getDevice().createSingleUseCommandBuffer(stack);
				
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
				
				commandBuffer.setScissor(0, 0, window.getWidth(), window.getHeight());
				commandBuffer.setViewport(0, window.getHeight(), window.getWidth(), -window.getHeight(), -1000, 1000);
				commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
				renderer.setUniformBufferIndex(window.getFrameIndex());
				renderer.draw(commandBuffer, PARTICLE_COUNT);
				commandBuffer.endRenderPass();
				commandBuffer.bindPipeline(computePipeline);
				commandBuffer.bindDescriptorSets(computePipeline, new long[] {computeDescriptorSet}, new int[0]);
				commandBuffer.dispatch(PARTICLE_COUNT / 256, 1, 1);
				commandBuffer.endRecording();
				
				window.swapBuffers(commandBuffer, () -> {
					computeStorageBufferLayout.update(sets -> {
						sets.get(I++ % 2).pBufferInfo().buffer(storageBuffer0.getGpuBuffer().getVk());
						sets.get(I % 2).pBufferInfo().buffer(storageBuffer1.getGpuBuffer().getVk());
					});
					computeUniformBufferLayout.set(ENLAPSED_TIME, uniformBuffer, (float) enlapsedTimeSeconds);
					computeUniformBufferLayout.updateDescriptorSets();
				});
				window.pollEvents();
				window.update();
			}
		}
		window.update();
	}
	
	public static void end()
	{
		renderer.close();
		computePipeline.close();
		VK10.vkDestroyDescriptorSetLayout(RenderEngine.getVulkan().getDevice().getVk(), computeDescriptorSetLayout, null);
		VK10.vkDestroyDescriptorPool(RenderEngine.getVulkan().getDevice().getVk(), computeDescriptorPool, null);
		RenderEngine.getVulkan().getWindow().hide();
		synchronized (MONITOR)
		{
			MONITOR.notify();
		}
		RenderEngine.terminate();
	}
}
