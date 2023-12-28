package com.alcadrial.renderengine.render.test;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import com.alcadrial.kector.angle.Angle;
import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.axis.Axis;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Renderer.DrawResult;
import com.alcadrial.renderengine.render.VertexAccumulator.VertexResult;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.ComputePipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
import com.alcadrial.renderengine.render.test.RendererUtils.OrthoCubeVertexResult;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class ComputeTest extends RenderTest {
	
	private static final String SHADER = "shader3";
	private static final String PROJECTION = "PROJECTION";
	private static final String MODEL = "MODEL";
	private static final String LIGHT_POS = "LIGHT_POS";
	private static final String LIGHT_INTENSITY = "LIGHT_INTENSITY";
	private static final String SPACE_FOG_INTENSITY = "SPACE_FOG_INTENSITY";
	
	public static void main(String[] args)
	{
		TestProperties.init(args);
		
		for (Entry<String, String> e : System.getenv().entrySet()) System.out.println(e.getKey() + "->" + e.getValue());
		
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
	
	private static Renderer renderer;
	private static ComputePipeline computePipeline;
	private static VertexBuffer vertexBuffer;
	private static VertexBuffer vertexBuffer0;
	private static IndexBuffer indexBuffer;
	private static Texture depthTexture;
	private static Vector3f lightPos;
	private static double f;
	
	private static Matrix4f buildProjectionMatrix(VulkanWindow window)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().perspective(Angle.of(90, AngleUnit.DEGREES), 0.09F, -0.1F, (float) window.getWidth() / window.getHeight()));
		matrix.multiply(new Matrix4f().view(Vector3f.of(0, 0, -1), Angle.V0_DEG, Angle.V0_DEG));
		return matrix.transpose();
	}
	
	private static Matrix4f buildModelMatrix()
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().rotate(Axis.Z, Angle.of(f / 11, AngleUnit.DEGREES)));
		matrix.multiply(new Matrix4f().rotate(Axis.Y, Angle.of(f / 17, AngleUnit.DEGREES)));
		matrix.multiply(new Matrix4f().rotate(Axis.X, Angle.of(f / 13, AngleUnit.DEGREES)));
		matrix.multiply(new Matrix4f().scale(Vector3f.of(1, 1, 1).divideAll(2)));
		return matrix;
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
		vulkan.createDevice(deviceFeatures);
		Device device = vulkan.getDevice();
		
		window.assignDevice(device);
		
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			renderer.updateUniformBuffers((b, l) -> l.set(PROJECTION, b, buildProjectionMatrix(window)));
		});
		
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
				window.getMousePos(oldMousePos[0]);
				pressing[1] = false;
			}
		});
		
		window.setCallback((GLFWKeyCallbackI) (w, k, s, a, m) -> {
			if (k == GLFW.GLFW_KEY_R)
			{
				renderer.updateUniformBuffers((b, l) -> {
					l.set(PROJECTION, b, buildProjectionMatrix(window));
					l.set(MODEL, b, buildModelMatrix());
					l.set(LIGHT_POS, b, lightPos);
					l.set(LIGHT_INTENSITY, b, 0.8F);
					l.set(SPACE_FOG_INTENSITY, b, 0.4F);
				});
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
		
		vertexBuffer = new VertexBuffer(240, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
		vertexBuffer0 = new VertexBuffer(2400, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
		
		int minOffsetAlignment = (int) device.getPhysicalDevice().getLimits().minUniformBufferOffsetAlignment();
		
		Builder builder = Renderer.create();
		builder.vertexShader(vertexShader, vertexShaderInfo).fragmentShader(fragmentShader, fragmentShaderInfo)/* .computeShader(computeShader, computeShaderInfo) */;
		builder.depthTexture(depthTexture);
		builder.depthTestEnable(true);
		builder.cullMode(VK10.VK_CULL_MODE_NONE);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		builder.uniformBuffer(new VulkanBuffer(device, Math.max(minOffsetAlignment * 3, (16 + 16 + 3 + 1 + 1) * 4), VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT));
		builder.storageBuffer(vertexBuffer);
		
		renderer = builder.build(window, device, CustomGraphicsPipeline::new);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkDescriptorPoolCreateInfo poolInfo = VulkanUtils.descriptorPoolCreateInfo(stack, VulkanUtils.descriptorPoolSize(stack, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 4));// static
			poolInfo.maxSets(3);
			
			long[] p0 = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorPool(device.getVk(), poolInfo, null, p0), "Failed to create descriptor pool!");
			long descriptorPool = p0[0];
			
			VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = VkDescriptorSetLayoutBinding.malloc(3, stack);// static
			
			VkDescriptorSetLayoutBinding descriptorSetLayoutBinding = descriptorSetLayoutBindings.get();
			descriptorSetLayoutBinding.binding(0);
			descriptorSetLayoutBinding.descriptorCount(1);
			descriptorSetLayoutBinding.descriptorType(ShaderLayout.Type.UNIFORM_BUFFER.getType());
			descriptorSetLayoutBinding.pImmutableSamplers(null);
			descriptorSetLayoutBinding.stageFlags(VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
			
			descriptorSetLayoutBinding = descriptorSetLayoutBindings.get();
			descriptorSetLayoutBinding.binding(1);
			descriptorSetLayoutBinding.descriptorCount(1);
			descriptorSetLayoutBinding.descriptorType(ShaderLayout.Type.STORAGE_BUFFER.getType());
			descriptorSetLayoutBinding.pImmutableSamplers(null);
			descriptorSetLayoutBinding.stageFlags(VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
			
			descriptorSetLayoutBinding = descriptorSetLayoutBindings.get();
			descriptorSetLayoutBinding.binding(2);
			descriptorSetLayoutBinding.descriptorCount(1);
			descriptorSetLayoutBinding.descriptorType(ShaderLayout.Type.STORAGE_BUFFER.getType());
			descriptorSetLayoutBinding.pImmutableSamplers(null);
			descriptorSetLayoutBinding.stageFlags(VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
			
			descriptorSetLayoutBindings.rewind();
			
			LongBuffer p = stack.callocLong(2);
			
			List<Long> descriptorSetLayouts = new ArrayList<>(2);
			for (int i = 0; i < 2; i++)
			{
				VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorSetLayout(device.getVk(), VulkanUtils.descriptorSetLayoutInfo(stack, 0, descriptorSetLayoutBindings), null, p), "Failed to create descriptor set layout!");
				
				descriptorSetLayouts.add(p.get());
			}
			p.rewind();
			
			long[] p1 = new long[2];
			
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.getVk(), VulkanUtils.descriptorSetAllocateInfo(stack, descriptorPool, p), p1), "Failed to allocate descriptor sets!");
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayouts.get(0), descriptorSetLayouts.get(1)));
			computePipeline = new ComputePipeline(vulkan.getDevice(), new PipelineLayout(device, pipelineLayoutInfo), true, new PreparedShader(computeShader, computeShaderInfo));
			
			System.out.println("SUCCESS");
		}
		lightPos = Vector3f.of(0.3F, 1, 0);
		f = 0;
		
		indexBuffer = new IndexBuffer(240);
		renderer.addVertexBuffer(0, vertexBuffer);
		renderer.addVertexBuffer(1, vertexBuffer0);
		renderer.setIndexBuffer(indexBuffer);
		renderer.updateUniformBuffers((b, l) -> {
			l.set(PROJECTION, b, buildProjectionMatrix(window));
			l.set(MODEL, b, buildModelMatrix());
			l.set(LIGHT_POS, b, lightPos);
			l.set(LIGHT_INTENSITY, b, 0.8F);
			l.set(SPACE_FOG_INTENSITY, b, 0.4F);
		});
		
		Color[] colors = new Color[] {
			Color.VERY_LIGHT_YELLOW,
			Color.VERY_LIGHT_RED,
			Color.VERY_LIGHT_MAGENTA,
			Color.VERY_LIGHT_GREEN,
			Color.VERY_LIGHT_CYAN,
			Color.VERY_LIGHT_BLUE
		};
		
		OrthoCubeVertexResult result = RendererUtils.loadOrthoCube(renderer, vertexBuffer, Vector3f.of(-1, -1, -1), Vector3f.of(1, 1, 1), (i, j) -> colors[i]);
		
		System.out.println(Arrays.toString(vertexBuffer.getAllFloats()));
		
		IntBuffer buffer = indexBuffer.asIntBuffer();
		result.foreach(rResult -> {
			VertexResult result0 = rResult.getR0();
			VertexResult result1 = rResult.getR1();
			VertexResult result2 = rResult.getR2();
			VertexResult result3 = rResult.getR3();
			
			buffer.put(new int[] {
				result0.getIndex(), result1.getIndex(), result3.getIndex(),
				result1.getIndex(), result2.getIndex(), result3.getIndex(),
			});
		});
		indexBuffer.sendToPhysicalDevice();
	}
	
	private static long time;
	
	public static void loop()
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		renderer.setClearColor(Color.of(0, 0, 0, 0));
		
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
				window.setTitle("Test fps=" + frame);
				frame = 0;
				highMilestone += (enlapsed - highMilestone + firstHighMilestone) / firstHighMilestone * firstHighMilestone;
			}
			println("Frame " + frame, () -> {
				DrawResult result;
				do
				{
					window.pollEvents();
					result = renderer.draw();
					window.update();
				} while (result == DrawResult.FAILED);
				renderer.updateUniformBuffers((b, l) -> l.set(MODEL, b, buildModelMatrix()));
				f += (firstMilestone - pauseTime) / 1000000D;
			}, TestProperties.PRINT_FRAME_TIMES);
			frame++;
		}
	}
	
	public static void end()
	{
		renderer.close();
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
