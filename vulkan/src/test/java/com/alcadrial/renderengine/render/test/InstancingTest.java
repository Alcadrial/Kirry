package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowIconifyCallbackI;
import org.lwjgl.glfw.GLFWWindowMaximizeCallbackI;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.angle.Angle;
import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.axis.Axis;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.buffer.AbstractBigVulkanBuffer.PhysicalDeviceTranferState;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.RenderingDataUtils;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.util.OverflowException;
import com.alcadrial.renderengine.render.util.VulkanFuture;
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
		mainTest(args, InstancingTest::start, () -> defaultLoop(InstancingTest::loop), InstancingTest::end, false);
	}
	
	private static RenderPass renderPass;
	private static Renderer[] renderers;
	private static List<CommandBuffer> commandBuffers;
	private static StaticBigBuffer staticBigBuffer;
	private static VulkanBuffer vertexBuffer;
	private static VulkanBuffer vertexBuffer0;
	private static VulkanBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static VulkanBuffer localUniformBuffer;
	private static Vector3f lightPos;
	private static double f;
	private static int instanceCount;
	
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
		matrix.multiply(buildScaleMatrix());
		return matrix;
	}
	
	private static Matrix4f buildScaleMatrix()
	{
		return new Matrix4f().scale(Vector3f.of(1, 1, 1).divideAll(16));
	}
	
	public static void start() throws VulkanException
	{
		ShaderProgram shaderProgram;
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
		
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = VulkanWindow.builder()
			.physicalDeviceFilter(PhysicalDevice.Filter.withSurfaceGraphicsAndCompute())
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.deviceFeatures(v -> v
				.wideLines(true)
				.samplerAnisotropy(true)
			)
			.deviceExtensions(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
			.asDefaultPrimary()
			.size(1080, 720)
			.title("Test")
			.build();
		
		Device device = window.getDevice();
		
		window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> {
			System.out.println(iconified ? "Iconified" : "Restored");
		});
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			System.out.println("Resized " + width + "x" + height);
			for (Renderer renderer : renderers)
			{
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
					renderer.updateUniform(PROJECTION, l -> l.set(buildProjectionMatrix(window)), commandBuffer);
					commandBuffer.endRecording();
					device.submitSingleUseCommandBuffer(stack, commandBuffer);
				}
				catch (VulkanException e)
				{
					e.printStackTrace(System.out);
					RenderEngine.terminate();
					return;
				}
			}
		});
		window.setCallback((GLFWWindowRefreshCallbackI) w -> {
			System.out.println("Refresh");
		});
		window.setCallback((GLFWWindowCloseCallbackI) w -> {
			System.out.println("Closed");
		});
		window.setCallback((GLFWWindowMaximizeCallbackI) (w, maximized) -> {
			System.out.println(maximized ? "Maximized" : "Restored");
		});
		
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
				window.getMousePos(oldMousePos[0]);
				pressing[1] = false;
			}
		});
		
		window.setCallback((GLFWKeyCallbackI) (w, k, s, a, m) -> {
			if (k == GLFW.GLFW_KEY_R)
			{
				for (Renderer renderer : renderers)
				{
					try (MemoryStack stack = MemoryStack.stackPush())
					{
						CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
						renderer.updateUniform(PROJECTION, l -> l.set(buildProjectionMatrix(window)), commandBuffer);
						renderer.updateUniform(MODEL, l -> l.set(buildModelMatrix()), commandBuffer);
						renderer.updateUniform(LIGHT_POS, l -> l.set(buildScaleMatrix().multiply(lightPos).xyz()), commandBuffer);
						renderer.updateUniform(LIGHT_INTENSITY, l -> l.set(0.75F), commandBuffer);
						renderer.updateUniform(SPACE_FOG_INTENSITY, l -> l.set(0.1F), commandBuffer);
						commandBuffer.endRecording();
						device.submitSingleUseCommandBuffer(stack, commandBuffer);
					}
					catch (VulkanException e)
					{
						e.printStackTrace(System.out);
						RenderEngine.terminate();
						return;
					}
				}
			}
		});
		
		renderPass = RenderPassTemplate.initColorDepthTextureTemplate(window.getSurface().getFormat().format()).createRenderPass(device);
		
		int size = 20;
		int density = 2;
		int a = ((2 * size + 1) / density + 1);
		instanceCount = a * a;
		int renderersCount = 3;
		
		staticBigBuffer = new StaticBigBuffer(device, 65536);
		
		vertexBuffer = staticBigBuffer.allocate(240 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		vertexBuffer0 = staticBigBuffer.allocate(instanceCount * renderersCount * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		indexBuffer = staticBigBuffer.allocate(36 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		uniformBuffer = staticBigBuffer.allocate(256 * 2 * renderersCount * 4, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
		localUniformBuffer = VulkanBuffer.asLocal(uniformBuffer);
		localUniformBuffer.getMemory().mapMemory();
		
		staticBigBuffer.setName("StaticBigBuffer");
		vertexBuffer.setName("VertexBuffer");
		vertexBuffer0.setName("VertexBuffer0");
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
		
		try (PhysicalDeviceTranferState transfert = staticBigBuffer.startLocalBuffering())
		{
			BufferWriter vertexBufferWriter = transfert.beginTransfer(vertexBuffer);
			BufferWriter vertexBuffer0Writer = transfert.beginTransfer(vertexBuffer0);
			BufferWriter indexBufferWriter = transfert.beginTransfer(indexBuffer);
			
			for (int j = -size; j <= size; j += density) for (int k = -size; k <= size; k += density)
			{
				vertexBuffer0Writer.put(Vector3f.of(j, k, 0));
			}
			
			RenderingDataUtils.loadOrthoCube(vertexBufferWriter, 0, indexBufferWriter, 0, 0, Vector3f.of(-1, -1, -1), Vector3f.of(0, 0, 0), (i, j) -> colors[i]);
		}
		catch (OverflowException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		lightPos = Vector3f.of(5, 20, 0);
		f = 0;
		
		renderers = new Renderer[renderersCount];
		
		Builder builder = Renderer.create();
		builder.shaderProgram(shaderProgram);
		builder.depthTestEnable(true);
		builder.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		
		commandBuffers = device.createMultiUseCommandBuffer(renderersCount);
		
		ArrayList<VulkanFuture<?>> uniformUpdateFutures = new ArrayList<>(renderersCount);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			for (int i = 0; i < renderersCount; i++)
			{
				builder.bufferBindings(
						new BufferWriter(localUniformBuffer, uniformBuffer, i * 512 + 0, 256),
						new BufferWriter(localUniformBuffer, uniformBuffer, i * 512 + 256, 256)
				);
				Renderer renderer = builder.build(device, window.getSurface().getSize(), renderPass, InstancingCustomGraphicsPipeline::new);
				renderer.setVertexBuffers(0, vertexBuffer);
				renderer.setVertexBuffers(1, vertexBuffer0);
				renderer.setIndexBuffer(indexBuffer);
				
				CommandBuffer commandBuffer = commandBuffers.get(i);
				commandBuffer.beginRecording();
				renderer.updateUniform(PROJECTION, l -> l.set(buildProjectionMatrix(window)), commandBuffer);
				renderer.updateUniform(MODEL, l -> l.set(buildModelMatrix()), commandBuffer);
				renderer.updateUniform(LIGHT_POS, l -> l.set(buildScaleMatrix().multiply(lightPos).xyz()), commandBuffer);
				renderer.updateUniform(LIGHT_INTENSITY, l -> l.set(0.75F), commandBuffer);
				renderer.updateUniform(SPACE_FOG_INTENSITY, l -> l.set(0.1F), commandBuffer);
				commandBuffer.endRecording();
				uniformUpdateFutures.add(device.submitMultiUseCommandBuffer(stack, commandBuffer));
				
				renderers[i] = renderer;
				
				RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, i * 128 + 0, 16, true, "PPPP");
				RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, i * 128 + 16, 16, true, "MMMM");
				RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, i * 128 + 64 + 0, 3, true, "xyz");
				RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, i * 128 + 64 + 3, 1, true, "I");
				RenderingDataUtils.printBufferContent(staticBigBuffer, uniformBuffer, i * 128 + 64 + 4, 1, true, "F");
			}
		}
		for (int i = 0; i < renderersCount; i++)
		{
			uniformUpdateFutures.get(i).join();
			commandBuffers.get(i).reset();
		}
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
			CommandBuffer commandBuffer = commandBuffers.get(window.getFrameIndex());
			Renderer renderer = renderers[window.getFrameIndex()];
			
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
			
			println("Update Uniforms", () -> renderer.updateUniform(MODEL, l -> l.set(buildModelMatrix()), commandBuffer), TestProperties.PRINT_FRAME_TIMES);
			
			commandBuffer.setScissor(0, 0, window.getWidth(), window.getHeight());
			commandBuffer.setViewport(0, window.getHeight(), window.getWidth(), -window.getHeight(), 0, 1);
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, instanceCount);
			commandBuffer.endRenderPass();
			commandBuffer.endRecording();
			
			println("swapBuffers", () -> window.swapBuffers(commandBuffer), TestProperties.PRINT_FRAME_TIMES);
			commandBuffer.reset();
		}
		println("pollEvents", () -> window.pollEvents(), TestProperties.PRINT_FRAME_TIMES);
		window.update();
		f = time / 1000000D;
	}
	
	public static void end()
	{
		for (Renderer renderer : renderers) renderer.close();
		synchronized (MONITOR)
		{
			MONITOR.notify();
		}
		RenderEngine.terminate();
	}
}
