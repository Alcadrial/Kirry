package com.alcadrial.renderengine.render.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

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
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

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
import com.alcadrial.renderengine.render.Computer;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.AbstractBigVulkanBuffer.PhysicalDeviceTransferState;
import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.RenderingDataUtils;
import com.alcadrial.renderengine.render.util.SphereDataCollector;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class ComputerTest extends RenderTest {
	
	private static final String SHADER = "shader3";
	private static final String PROJECTION = "PROJECTION";
	//	private static final String PARTICLE_RADIUS = "PARTICLE_RADIUS";
	private static final String MIN_BOUND = "MIN_BOUND";
	private static final String MAX_BOUND = "MAX_BOUND";
	private static final String ENLAPSED_TIME = "ENLAPSED_TIME";
	
	public static void main(String[] args)
	{
		mainTest(args, ComputerTest::start, () -> defaultLoop(ComputerTest::loop), ComputerTest::end, false);
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
	
	private static RenderPass renderPass;
	private static Renderer renderer;
	private static Computer computer;
	private static StaticBigBuffer staticBigBuffer;
	private static VulkanBuffer vertexBuffer;
	private static VulkanBuffer vertexStorageBuffer;
	private static VulkanBuffer storageBuffer0;
	private static VulkanBuffer storageBuffer1;
	private static VulkanBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static VulkanBuffer localUniformBuffer;
	private static BufferLayout computeUniformBufferLayout;
	private static BufferLayout computeStorageBuffer0Layout;
	private static BufferLayout computeStorageBuffer1Layout;
	private static CommandBuffer commandBuffer;
	
	private static Vector3f cameraPosition;
	private static Vector3f cameraMovement;
	private static Angle cameraXRotation;
	private static Angle cameraYRotation;
	private static boolean isCameraUnlocked;
	
	private static boolean rebuildProjectionMatrix;
	
	private static Matrix4f buildProjectionMatrix(VulkanWindow window)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().perspective(Angle.of(120, AngleUnit.DEGREES), 1000, 0.0001F, (float) window.getWidth() / window.getHeight()));
		matrix.multiply(new Matrix4f().view(cameraPosition, cameraXRotation.asFloat(AngleUnit.DEGREES), cameraYRotation.asFloat(AngleUnit.DEGREES)));
		return matrix.transpose();
	}
	
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
			rebuildProjectionMatrix = true;
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
					case GLFW.GLFW_KEY_R -> {/*
												renderer.updateUniforms(uniformBuffer, l -> {
												l.set(PARTICLE_RADIUS, uniformBuffer, 0.01F);
												});*/
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
			int size = PARTICLE_COUNT * 12 * 4 * 4;
			staticBigBuffer = new StaticBigBuffer(device, 1 << 19);
			vertexBuffer = staticBigBuffer.allocate(242 * 3 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
			indexBuffer = staticBigBuffer.allocate(2064 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
			uniformBuffer = staticBigBuffer.allocate(Math.max(minOffsetAlignment * 3, (16 + 16 + 3 + 1 + 1) * 4), VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
			localUniformBuffer = VulkanBuffer.asLocal(uniformBuffer);
			localUniformBuffer.getMemory().mapMemory();
			vertexStorageBuffer = staticBigBuffer.allocate(2 * size, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
			storageBuffer0 = new VulkanBuffer(vertexStorageBuffer, 0, size);
			storageBuffer1 = new VulkanBuffer(vertexStorageBuffer, size, size);
			
			staticBigBuffer.setName("StaticBigBuffer");
			vertexBuffer.setName("VertexBuffer");
			indexBuffer.setName("IndexBuffer");
			uniformBuffer.setName("UniformBuffer");
			localUniformBuffer.setName("LocalUniformBuffer");
			vertexStorageBuffer.setName("CertexStorageBuffer");
			storageBuffer0.setName("StorageBuffer0");
			storageBuffer1.setName("StorageBuffer1");
		}
		
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
		
		try (PhysicalDeviceTransferState transfert = staticBigBuffer.startLocalBuffering())
		{
			BufferWriter vertexBufferWriter = transfert.beginTransfer(vertexBuffer);
			BufferWriter storageBuffer0Writer = transfert.beginTransfer(storageBuffer0);
			BufferWriter indexBufferWriter = transfert.beginTransfer(indexBuffer);
			
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
						storageBuffer0Writer.put(c.r() / 255).put(c.g() / 255).put(c.b() / 255).put(c.a() / 255);
					}
				}
			}
		}
		{
			VulkanBuffer srcBuffer = new VulkanBuffer(storageBuffer0, 0, storageBuffer0.getSize(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
			VulkanBuffer dstBuffer = new VulkanBuffer(storageBuffer1, 0, storageBuffer1.getSize(), VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), buffer -> buffer.copyBuffer(srcBuffer, 0, dstBuffer, 0, storageBuffer0.getSize()));
		}
		
		commandBuffer = device.createMultiUseCommandBuffer();
		renderPass = RenderPassTemplate.initColorDepthTextureTemplate(window.getSurface().getFormat().format()).createRenderPass(device);
		
		Builder builder = Renderer.create();
		builder.shaderProgram(shaderProgram);
		builder.depthTestEnable(true);
		builder.cullMode(VK10.VK_CULL_MODE_NONE);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		builder.bufferBindings(List.of(
				new BufferWriter(localUniformBuffer, uniformBuffer, 0, 256),
				new BufferWriter(localUniformBuffer, uniformBuffer, 256, 256)
		));
		builder.minDepth(-1000F).maxDepth(1000F);
		
		renderer = builder.build(device, window.getSurface().getSize(), renderPass, ComputeCustomGraphicsPipeline::new);
		renderer.setBuffer(0, vertexBuffer);
		renderer.setBuffer(1, storageBuffer0);
		renderer.setIndexBuffer(indexBuffer);
		rebuildProjectionMatrix = true;
		
		computer = new Computer(device, computeShader);
		computeUniformBufferLayout = computer.setBinding(0, new BufferWriter(localUniformBuffer, uniformBuffer, 512, 256));
		computeStorageBuffer0Layout = computer.setBinding(1, storageBuffer0);
		computeStorageBuffer1Layout = computer.setBinding(2, storageBuffer1);
		computer.init();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			commandBuffer.beginRecording();
			computeUniformBufferLayout.set(MIN_BOUND, MIN_BOUND_VALUE);
			computeUniformBufferLayout.set(MAX_BOUND, MAX_BOUND_VALUE);
			computeUniformBufferLayout.set(ENLAPSED_TIME, 1F);
			computeUniformBufferLayout.flush(commandBuffer);
			commandBuffer.endRecording();
			device.submitAndResetMultiUseCommandBuffer(stack, commandBuffer).join();
		}
		
		RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, 0, 16, true, "PPPP");
		RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, 128, 12, true, "xyz*");
		
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
			Debug.printState("Computer", computer);
			Debug.setDebugOutput(System.out);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace(System.out);
		}
	}
	
	@SuppressWarnings("null")
	public static void loop(long time, long enlapsed) throws VulkanException
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
			
			cameraPosition.add(movement.multiplyAll(enlapsed / 1000000000F));
			rebuildProjectionMatrix = true;
		}
		
		window.pollEvents();
		if (!window.isIconified())
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
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
				
				computeUniformBufferLayout.set(ENLAPSED_TIME, enlapsed / 1000000000F);
				commandBuffer.beginRecording();
				if (rebuildProjectionMatrix)
				{
					RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, 0, 32, true, "PPPP");
					RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, 128, 12, true, "xyz*");
					renderer.updateUniform(PROJECTION, l -> l.set(buildProjectionMatrix(window)), commandBuffer);
					rebuildProjectionMatrix = false;
				}
				commandBuffer.setScissor(0, 0, window.getWidth(), window.getHeight());
				commandBuffer.setViewport(0, window.getHeight(), window.getWidth(), -window.getHeight(), -1000, 1000);
				commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
				renderer.draw(commandBuffer, PARTICLE_COUNT);
				commandBuffer.endRenderPass();
				computer.dispatch(commandBuffer, PARTICLE_COUNT, 1, 1);
				computeUniformBufferLayout.flush(commandBuffer);
				commandBuffer.endRecording();
				
				window.swapBuffers(commandBuffer, () -> {
					
					VulkanBuffer t = storageBuffer0;
					storageBuffer0 = storageBuffer1;
					storageBuffer1 = t;
					computeStorageBuffer0Layout.update(sets -> sets.get(0).pBufferInfo().buffer(storageBuffer0.getVk()));
					computeStorageBuffer1Layout.update(sets -> sets.get(0).pBufferInfo().buffer(storageBuffer1.getVk()));
					commandBuffer.reset();
				});
			}
		}
		window.update();
	}
	
	public static void end()
	{
		computer.close();
		renderer.close();
		RenderEngine.getVulkan().getWindow().hide();
		synchronized (MONITOR)
		{
			MONITOR.notify();
		}
		RenderEngine.terminate();
	}
}
