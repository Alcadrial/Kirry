package com.alcadrial.renderengine.render.test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

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
import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.texture.ImageWriter;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class TextureTest extends RenderTest {
	
	private static final String SHADER = "shader0";
	private static final String TRANSFORMATION_UNIFORM = "TRANSFORMATION";
	private static final String TEXTURE_SAMPLER_UNIFORM = "TEXTURE_SAMPLER";
	
	public static void main(String[] args)
	{
		mainTest(args, TextureTest::start, () -> defaultLoop(TextureTest::loop), TextureTest::end);
	}
	
	private static Renderer renderer;
	private static RenderPass renderPass;
	private static Texture texture;
	private static VulkanBuffer vertexBuffer;
	private static VulkanBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static VulkanBuffer localUniformBuffer;
	private static Vector2i panelPos;
	
	private static boolean isModifiedMatrix;
	private static boolean isModified;
	
	public static void start() throws VulkanException
	{
		ShaderProgram shaderProgram;
		try
		{
			PreparedShader vertexShader = loadShader(SHADER + ".vert");
			PreparedShader fragmentShader = loadShader(SHADER + ".frag");
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
		window.setUseDepthTexture(false);
		vulkan.setWindow(window);
		if (!pickPhysicalDevice(window))
		{
			return;
		}
		
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
		deviceFeatures.samplerAnisotropy(true);
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
		
		vertexBuffer = new VulkanBuffer(device, 28 * 4 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		indexBuffer = new VulkanBuffer(device, 6 * 4 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		uniformBuffer = new VulkanBuffer(device, 64, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		localUniformBuffer = new VulkanBuffer(device, uniformBuffer.getSize(), VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
		localUniformBuffer.getMemory().mapMemory();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			Future<BufferedImage> future = RenderEngine.getExecutor().submit(() -> ImageIO.read(getFile("KhishweTransparent.png")));
			
			VulkanBuffer localVertexBuffer = new VulkanBuffer(device, vertexBuffer.getSize(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
			VulkanBuffer localIndexBuffer = new VulkanBuffer(device, indexBuffer.getSize(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
			
			localVertexBuffer.getMemory().mapMemory();
			localIndexBuffer.getMemory().mapMemory();
			
			BufferWriter vertexBufferWriter = new BufferWriter(localVertexBuffer, 0, vertexBuffer, 0, vertexBuffer.getSize(), null);
			BufferWriter indexBufferWriter = new BufferWriter(localIndexBuffer, 0, indexBuffer, 0, indexBuffer.getSize(), null);
			
			RenderingDataUtils.loadRectangleUv(vertexBufferWriter, indexBufferWriter, 0, -256, -256, 256, 256, 0, 0, 0, 1, 1);
			vertexBufferWriter.close();
			indexBufferWriter.close();
			
			renderPass = RenderPassTemplate.initColorTextureTemplate(window.getSurface().getFormat().format()).createRenderPass(device);
			
			CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
			
			BufferedImage image = future.get();
			texture = Texture.create2dTexture(device, image.getWidth(), image.getHeight(), VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			
			VulkanBuffer localTextureBuffer = new VulkanBuffer(device, texture.getLength() * 4, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
			localTextureBuffer.getMemory().mapMemory();
			ImageWriter textureBufferwriter = new ImageWriter(localTextureBuffer, 0, texture, 0, 0, 0, texture.getWidth(), texture.getHeight(), texture.getDepth(), commandBuffer);
			textureBufferwriter.put(image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()));
			textureBufferwriter.close();
			
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			
			device.submitSingleUseCommandBuffer(stack, commandBuffer)
				.thenRunAsync(localTextureBuffer::close)
				.thenRunAsync(localVertexBuffer::close)
				.thenRunAsync(localIndexBuffer::close);
			localVertexBuffer.getMemory().unmapMemory();
			localIndexBuffer.getMemory().unmapMemory();
			localTextureBuffer.getMemory().unmapMemory();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		Builder builder = Renderer.create();
		builder.shaderProgram(shaderProgram);
		builder.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		builder.bufferBindings(
				new BufferWriter(localUniformBuffer, 0, uniformBuffer, 0, uniformBuffer.getSize()),
				new BufferWriter(localUniformBuffer, 0, uniformBuffer, 0, 0)//texture sampler
		);
		builder.dynamicViewport(false).dynamicScissor(false);
		renderer = builder.build(device, window.getSurface().getSize(), renderPass);
		renderer.setVertexBuffers(0, vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
		renderer.updateUniforms(l -> l.set(TEXTURE_SAMPLER_UNIFORM, texture.getView(), new TextureSampler(device)));
		
		window.addSurfaceUpdateActions(surface -> renderer.startModificationPlan().size(surface.getSize()).submit());
		isModifiedMatrix = true;
		isModified = true;
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		if (isModified)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				isModified = false;
				
				FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
				CommandBuffer commandBuffer = window.getDevice().createSingleUseCommandBuffer();
				
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
				
				if (isModifiedMatrix)
				{
					isModifiedMatrix = false;
					renderer.updateUniforms(l -> {
						Matrix4f orthographic = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
						Matrix4f model = new Matrix4f().translateYTo(-10 * panelPos.y()).translateXTo(-10 * panelPos.x());
						l.set(TRANSFORMATION_UNIFORM, orthographic.multiply(model).transpose());
						l.flush(commandBuffer);
					});
				}
				
				commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
				renderer.draw(commandBuffer);
				commandBuffer.endRenderPass();
				commandBuffer.endRecording();
				
				window.swapBuffers(commandBuffer);
				window.update();
			}
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
