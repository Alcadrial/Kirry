package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Random;

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
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBufferRange;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class Test2dPanel extends RenderTest {
	
	private static final String SHADER = "shader";
	private static final String TRANSFORMATION_UNIFORM = "TRANSFORMATION";
	
	public static void main(String[] args)
	{
		mainTest(args, Test2dPanel::start, () -> defaultLoop(Test2dPanel::loop), Test2dPanel::end);
	}
	
	private static RenderPass renderPass;
	private static Renderer renderer;
	private static VertexBuffer vertexBuffer;
	private static IndexBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static Vector2i panelPos;
	
	private static boolean isModifiedMatrix;
	private static boolean isModified;
	
	public static void start()
	{
		Vulkan vulkan = RenderEngine.getVulkan();
		
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = new VulkanWindow(1080, 720, "Test");
		vulkan.setWindow(window);
		window.setUseDepthTexture(false);
		if (!pickPhysicalDevice(window)) return;
		
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
		deviceFeatures.wideLines(true);
		Device device = vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);
		window.init(device);
		
		window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> {
			System.out.println(iconified ? "Iconified" : "Restored");
		});
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			System.out.println("Resized " + width + "x" + height);
			isModifiedMatrix = true;
		});
		window.setCallback((GLFWWindowRefreshCallbackI) w -> {
			System.out.println("Refresh");
			isModified = true;
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
		panelPos = Vector2i.of(0, 0);
		window.setCallback((GLFWKeyCallbackI) (w, k, s, a, m) -> {
			if (a == GLFW.GLFW_PRESS || a == GLFW.GLFW_REPEAT)
			{
				switch (k)
				{
					case 'W':
						panelPos.add(0, 1);
						isModified = isModifiedMatrix = true;
						break;
					case 'A':
						panelPos.add(-1, 0);
						isModified = isModifiedMatrix = true;
						break;
					case 'S':
						panelPos.add(0, -1);
						isModified = isModifiedMatrix = true;
						break;
					case 'D':
						panelPos.add(1, 0);
						isModified = isModifiedMatrix = true;
						break;
				}
			}
		});
		
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
		
		renderPass = RenderPassTemplate.initColorTextureTemplate(window.getSurface().getFormat().format()).createRenderPass(device);
		
		uniformBuffer = new VulkanBuffer(device, 64, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
		
		Builder builder = Renderer.create();
		builder.shaderProgram(shaderProgram);
		builder.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		builder.lineWidth(2F);
		builder.bufferBindings(List.of(new VulkanBufferRange(uniformBuffer)));
		renderer = builder.build(device, window.getSurface().getSize(), renderPass);
		
		vertexBuffer = new VertexBuffer(7 * 4 * 512 * 512);
		indexBuffer = new IndexBuffer(6 * 512 * 512);
		renderer.addVertexBuffer(0, vertexBuffer.getGpuBuffer());
		renderer.setIndexBuffer(indexBuffer.getGpuBuffer());
		
		Random random = new Random();
		FloatBuffer buffer = vertexBuffer.asFloatBuffer();
		IntBuffer buffer0 = indexBuffer.asIntBuffer();
		int k = 0;
		for (int i = -256; i < 256; i++)
		{
			for (int j = -256; j < 256; j++)
			{
				RenderingDataUtils.loadRectangleColor(buffer, buffer0, k, j, i, j + 1, i + 1, 0, random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
				k += 4;
			}
		}
		vertexBuffer.sendToPhysicalDevice();
		indexBuffer.sendToPhysicalDevice();
		
		isModifiedMatrix = true;
		isModified = true;
	}
	
	public static void loop(long time, long enlapsed)
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		Device device = window.getDevice();
		
		if (isModifiedMatrix)
		{
			isModifiedMatrix = false;
			renderer.updateUniforms(uniformBuffer, l -> {
				Matrix4f orthographic = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
				Matrix4f model = new Matrix4f().translateYTo(-panelPos.y()).translateXTo(-panelPos.x());
				l.set(TRANSFORMATION_UNIFORM, uniformBuffer, orthographic.multiply(model));
			});
		}
		if (isModified)
		{
			isModified = false;
			
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				device.configureSingleUseCommandBuffer(stack, device.getGraphicsQueue(), commandBuffer -> {
					
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
					commandBuffer.setViewport(0, window.getHeight(), window.getWidth(), -window.getHeight(), 0, 1);
					commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
					renderer.draw(commandBuffer);
					commandBuffer.endRenderPass();
					commandBuffer.endRecording();
					
					window.swapBuffers(commandBuffer);
				});
			}
			window.update();
		}
		window.pollEvents();
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
}
