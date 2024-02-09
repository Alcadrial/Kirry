package com.alcadrial.renderengine.render.test;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.HexFormat;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.windows.Kernel32;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
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
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.ComputePipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.SphereDataCollector;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.window.Presenter;
import com.alcadrial.renderengine.render.window.Presenter.Result;
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
		TestProperties.init(args);
		System.out.println(Kernel32.GetCurrentThreadId());
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		println("Render Engine initialization", () -> {
			if (!RenderEngine.create("Test", new Version(0, 0, 1, 0)).messenger(ComputeTest::debugCallback).initialize()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		}, TestProperties.PRINT_LOADING_TIMES);
		
		println("Start", ComputeTest::start, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		println("Loop", ComputeTest::loop, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		println("End", ComputeTest::end, TestProperties.PRINT_LOADING_TIMES);
	}
	
	private static final int DIM = 8;
	private static final int PARTICLE_COUNT = DIM * DIM * DIM;
	private static final Vector3f MIN_BOUND_VALUE = Vector3f.of(-2, -2, -2);
	private static final Vector3f MAX_BOUND_VALUE = Vector3f.of(2, 2, 2);
	
	private static final Vector3f NORTH = Axis.Z;
	private static final Vector3f SOUTH = Axis.NEGATIVE_Z;
	private static final Vector3f WEST = Axis.X;
	private static final Vector3f EAST = Axis.NEGATIVE_X;
	private static final Vector3f UP = Axis.Y;
	private static final Vector3f DOWN = Axis.NEGATIVE_Y;
	
	private static long fps;
	private static Presenter presenter;
	private static Renderer renderer;
	private static VertexBuffer vertexBuffer;
	private static VertexBuffer vertexBuffer0;
	private static VertexBuffer vertexBuffer1;
	private static IndexBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static Texture depthTexture;
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
		Vulkan vulkan = RenderEngine.getVulkan();
		
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, false);
		
		println("Window creation", () -> vulkan.setWindow(new VulkanWindow(1080, 720, "Test")), TestProperties.PRINT_LOADING_TIMES);
		VulkanWindow window = vulkan.getWindow();
		
		vulkan.setPhysicalDeviceFilter(physicalDevice -> physicalDevice.getQueueFamily(VK10.VK_QUEUE_GRAPHICS_BIT | VK10.VK_QUEUE_COMPUTE_BIT, window.getSurface()) != null);
		if (TestProperties.DEVICE.isDefault()) vulkan.pickPhysicalDevices(PhysicalDevice.typeBestMatcher());
		else vulkan.pickPhysicalDevices(TestProperties.DEVICE.getValue());
		
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
		deviceFeatures.wideLines(true);
		deviceFeatures.samplerAnisotropy(true);
		Device device = vulkan.createDevice(deviceFeatures);
		
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
					case GLFW.GLFW_KEY_R ->
					{
						renderer.updateUniformBuffers((b, l) -> {
							l.set(PARTICLE_RADIUS, b, 0.01F);
						});
					}
					case GLFW.GLFW_KEY_U ->
					{
						isCameraUnlocked = !isCameraUnlocked;
						window.setCursorInputMode(isCameraUnlocked ? CursorInputMode.DISABLED : CursorInputMode.NORMAL);
					}
					case GLFW.GLFW_KEY_P ->
					{
						cameraPosition.setAll(0, 0, 0);
						rebuildProjectionMatrix = true;
					}
				}
			}
		});
		
		Shader vertexShader;
		ShaderInfo vertexShaderInfo;
		Shader fragmentShader;
		ShaderInfo fragmentShaderInfo;
		Shader computeShader;
		ShaderInfo computeShaderInfo;
		try
		{
			vertexShader = new Shader(getFile(SHADER + ".vert"));
			vertexShaderInfo = ShaderInfo.fromJson(getFile(SHADER + ".vert.json"));
			vertexShader.compile(vertexShaderInfo);
			if (!vertexShader.isCompilationSuccessfull()) throw new IOException("Shader " + vertexShader.getFile() + " has errors");
			
			fragmentShader = new Shader(getFile(SHADER + ".frag"));
			fragmentShaderInfo = ShaderInfo.fromJson(getFile(SHADER + ".frag.json"));
			fragmentShader.compile(fragmentShaderInfo);
			if (!fragmentShader.isCompilationSuccessfull()) throw new IOException("Shader " + fragmentShader.getFile() + " has errors");
			
			computeShader = new Shader(getFile(SHADER + ".comp"));
			computeShaderInfo = ShaderInfo.fromJson(getFile(SHADER + ".comp.json"));
			computeShader.compile(computeShaderInfo);
			if (!computeShader.isCompilationSuccessfull()) throw new IOException("Shader " + computeShader.getFile() + " has errors");
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		depthTexture = Texture.create2dTexture(device, window.getWidth(), window.getHeight(), VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
		depthTexture.changeLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		
		int minOffsetAlignment = (int) device.getPhysicalDevice().getLimits().minUniformBufferOffsetAlignment();
		System.out.println("MinOffset = " + minOffsetAlignment);
		
		vertexBuffer = new VertexBuffer(242 * 3);
		vertexBuffer0 = new VertexBuffer(PARTICLE_COUNT * 12 * 4, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, true);
		vertexBuffer1 = new VertexBuffer(PARTICLE_COUNT * 12 * 4, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, true);
		uniformBuffer = new VulkanBuffer(device, Math.max(minOffsetAlignment * 3, (16 + 16 + 3 + 1 + 1) * 4), VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
		
		presenter = new Presenter(device, window.getSurface(), depthTexture, 3);
		
		Builder builder = Renderer.create();
		builder.vertexShader(vertexShader, vertexShaderInfo).fragmentShader(fragmentShader, fragmentShaderInfo)/* .computeShader(computeShader, computeShaderInfo) */;
		builder.depthTexture(depthTexture);
		builder.depthTestEnable(true);
		builder.cullMode(VK10.VK_CULL_MODE_NONE);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		builder.uniformBuffer(uniformBuffer);
		builder.minDepth(-1000F).maxDepth(1000F);
		
		renderer = builder.build(window, device, presenter.getRenderPass(), 3, ComputeCustomGraphicsPipeline::new);
		
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
			System.out.println("computeDescriptorSet " + HexFormat.of().toHexDigits(computeDescriptorSet, 16));
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			pipelineLayoutInfo.pSetLayouts(p);
			computePipeline = new ComputePipeline(device, new PipelineLayout(device, pipelineLayoutInfo), true, new PreparedShader(computeShader, computeShaderInfo));
			
			VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.malloc(3);
			
			computeUniformBufferLayout = new BufferLayout(device, writeDescriptorSets.slice(0, 1), 0, computeDescriptorSetLock);
			computeStorageBufferLayout = new BufferLayout(device, writeDescriptorSets.slice(1, 2), 0, computeDescriptorSetLock);
			
			System.out.println(computeUniformBufferLayout);
			System.out.println(computeStorageBufferLayout);
			
			VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
			writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
			writeDescriptorSet.pNext(0);
			writeDescriptorSet.dstSet(p1[0]);
			writeDescriptorSet.dstBinding(0);
			writeDescriptorSet.dstArrayElement(0);
			writeDescriptorSet.descriptorCount(1);
			writeDescriptorSet.descriptorType(ShaderLayout.Type.UNIFORM_BUFFER.getType());
			
			int offset = 512;
			
			VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
			descriptorBufferInfo.buffer(uniformBuffer.getVk());
			descriptorBufferInfo.offset(offset);
			for (ShaderVariable variable : new ShaderVariable[] {
				ShaderVariable.of(ShaderInputType.FLOAT, ENLAPSED_TIME),
				ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, MIN_BOUND),
				ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, MAX_BOUND),
			})
			{
				System.out.println(variable + " offset " + offset / 4);
				computeUniformBufferLayout.mark(variable, offset / 4);
				offset += 16;
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
			descriptorBufferInfo.buffer(vertexBuffer0.getGpuVk());
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
			descriptorBufferInfo.buffer(vertexBuffer1.getGpuVk());
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
			
			// VkCommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			
			System.out.println("SUCCESS");
		}
		
		indexBuffer = new IndexBuffer(2064);
		renderer.addVertexBuffer(0, vertexBuffer);
		renderer.addVertexBuffer(1, vertexBuffer0);
		renderer.setIndexBuffer(indexBuffer);
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
		
		FloatBuffer cursor = vertexBuffer0.asFloatBuffer();
		for (float i = 0; i < DIM; i++)
		{
			for (float j = 0; j < DIM; j++)
			{
				for (float k = 0; k < DIM; k++)
				{
					Color c = colors[random.nextInt(colors.length)];
					cursor.put(i / 10).put(j / 10).put(k / 10).put(0F);
					cursor.put(random.nextFloat(-1 / 20F, 1 / 20F)).put(random.nextFloat(-1 / 20F, 1 / 20F)).put(random.nextFloat(-1 / 20F, 1 / 20F)).put(0F);
					cursor.put(c.r() / 255).put(c.g() / 255).put(c.b() / 255).put(c.a() / 255);
				}
			}
		}
		
		try
		{
			vertexBuffer.sendToPhysicalDevice().get();
			indexBuffer.sendToPhysicalDevice().get();
			vertexBuffer0.sendToPhysicalDevice().get();
			vertexBuffer0.sendToPhysicalDevice(vertexBuffer1.getGpuVk()).get();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
	}
	
	private static void printVB0(VertexBuffer vertexBuffer)
	{
		float[] data = vertexBuffer.getAllFloats(0, 12 * PARTICLE_COUNT);
		
		for (int i = 0; i < data.length; i += 12)
		{
			System.out.print("{[");
			System.out.print(data[i]);
			System.out.print(", ");
			System.out.print(data[i + 1]);
			System.out.print(", ");
			System.out.print(data[i + 2]);
			System.out.print(" | ");
			System.out.print(data[i + 3]);
			System.out.print("]");
			System.out.print("[");
			System.out.print(data[i + 4]);
			System.out.print(", ");
			System.out.print(data[i + 5]);
			System.out.print(", ");
			System.out.print(data[i + 6]);
			System.out.print(" | ");
			System.out.print(data[i + 7]);
			System.out.print("]");
			System.out.print("[");
			System.out.print(data[i + 8]);
			System.out.print(", ");
			System.out.print(data[i + 9]);
			System.out.print(", ");
			System.out.print(data[i + 10]);
			System.out.print(", ");
			System.out.print(data[i + 11]);
			System.out.println("]}");
		}
		System.out.println();
	}
	
	private static long time;
	
	public static void loop()
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		presenter.setClearColor(Color.of(0, 0, 0, 0));
		
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
		
		computeStorageBufferLayout.update(sets -> {
			RenderEngine.getVulkan().getDevice().getGraphicsQueue().waitAsyncIdle(() -> {
				sets.get(I++ % 2).pBufferInfo().buffer(vertexBuffer0.getGpuVk());
				sets.get(I % 2).pBufferInfo().buffer(vertexBuffer1.getGpuVk());
			});
			computeUniformBufferLayout.set(ENLAPSED_TIME, uniformBuffer, (float) enlapsedTimeSeconds);
			computeUniformBufferLayout.updateDescriptorSets();
		});
		
		if (isCameraUnlocked)
		{
			window.setTitle("Test fps=" + fps + ", pos=" + cameraPosition + ", vel=" + cameraMovement + ", angles={" + cameraXRotation.asFloatBounded(AngleUnit.DEGREES) + ", " + cameraYRotation.asFloatBounded(AngleUnit.DEGREES) + "}");
		}
		else
		{
			window.setTitle("Test fps=" + fps);
		}
		if (rebuildProjectionMatrix)
		{
			renderer.updateUniformBuffers((b, l) -> l.set(PROJECTION, b, buildProjectionMatrix(window)));
		}
		
		window.pollEvents();
		if (!window.isIconified())
		{
			while (presenter.present((commandBuffer, frameIndex) -> {
				presenter.beginRenderPass(commandBuffer);
				renderer.setUniformBufferIndex(frameIndex);
				renderer.draw(commandBuffer, PARTICLE_COUNT / 2);
				commandBuffer.endRenderPass();
				commandBuffer.bindPipeline(computePipeline);
				computeDescriptorSetLock.lock();
				commandBuffer.bindDescriptorSets(computePipeline, new long[] {computeDescriptorSet}, new int[0]);
				commandBuffer.dispatch(1, 1, 1);
			}, () -> computeDescriptorSetLock.unlock()) == Result.RECREATED_SWAPCHAIN);
			
		}
		window.update();
	}
	
	public static void end()
	{
		renderer.close();
		presenter.close();
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
	
	public static File getFile(String path)
	{
		return new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/" + path);
	}
}
