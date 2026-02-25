package com.alcadrial.renderengine.render.test;

import java.awt.image.BufferedImage;
import java.math.RoundingMode;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDepthRangeUnrestricted;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
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
import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.shader.PreparedShader;
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
import com.alcadrial.renderengine.render.texture.ImageWriter;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.util.VulkanFuture;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

@Test
public class TextureTest extends RenderTest {
	
	private static final String SHADER = "shader0";
	private static final String TRANSFORMATION_UNIFORM = "TRANSFORMATION";
	private static final String TEXTURE_SAMPLER_UNIFORM = "TEXTURE_SAMPLER";
	
	public static void main(String[] args)
	{
		mainTest(args, TextureTest::start, () -> defaultLoop(TextureTest::loop), TextureTest::end, false);
	}
	
	private static Renderer renderer;
	private static RenderPass renderPass;
	private static GraphicsPipelineTemplate graphicsPipelineTemplate;
	private static GraphicsPipeline graphicsPipeline;
	private static Texture texture;
	private static VulkanBuffer vertexBuffer;
	private static VulkanBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static Vector2i panelPos;
	
	private static boolean isModifiedMatrix;
	private static boolean isModified;
	private static boolean isRendererDirty;
	private static ShaderProgram shaderProgram;
	private static PipelineLayout pipelineLayout;
	
	public static void start() throws VulkanException
	{
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = VulkanWindow.builder()
			.deviceExtensions(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME, EXTDepthRangeUnrestricted.VK_EXT_DEPTH_RANGE_UNRESTRICTED_EXTENSION_NAME)
			.deviceFeatures(f -> f
				.samplerAnisotropy(true)
			)
			.useDepthTexture(false)
			.size(1080, 720)
			.title("Test")
			.build();
		window.setName("Window");
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			isModifiedMatrix = true;
			isRendererDirty = true;
		});
		window.setCallback((GLFWWindowRefreshCallbackI) w -> {
			isModified = true;
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
		
		VulkanFuture<BufferedImage> imageFuture = RenderEngine.submitAsync(() -> ImageIO.read(getFile("icon.png")));
		VulkanFuture<ShaderProgram> shaderProgramFuture = RenderEngine.submitAsync(() -> {
			PreparedShader vertexShader = loadShader(SHADER + ".vert");
			PreparedShader fragmentShader = loadShader(SHADER + ".frag");
			return ShaderProgram.create().add(vertexShader, fragmentShader).build();
		});
		
		Device device = window.getDevice();
		
		vertexBuffer = new VulkanBuffer(device, 28 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		indexBuffer = new VulkanBuffer(device, 6 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		uniformBuffer = new VulkanBuffer(device, 256, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
		uniformBuffer.getMemory().mapMemory();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanBuffer localVertexBuffer = new VulkanBuffer(device, vertexBuffer.getSize(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
			VulkanBuffer localIndexBuffer = new VulkanBuffer(device, indexBuffer.getSize(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
			
			localVertexBuffer.getMemory().mapMemory();
			localIndexBuffer.getMemory().mapMemory();
			
			BufferWriter vertexBufferWriter = new BufferWriter(localVertexBuffer, vertexBuffer);
			BufferWriter indexBufferWriter = new BufferWriter(localIndexBuffer, indexBuffer);
			
			RenderingDataUtils.loadRectangleUv(vertexBufferWriter, indexBufferWriter, 0, -1, 1, 1, -1, 0, 0, 0, 1, 1);
			vertexBufferWriter.close();
			indexBufferWriter.close();
			
			renderPass = window.getRenderPass();
			
			CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
			
			BufferedImage image = imageFuture.forceGet();
			texture = Texture.create2dTexture(device, image.getWidth(), image.getHeight(), VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			
			VulkanBuffer localTextureBuffer = new VulkanBuffer(device, texture.getLength() * 4, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
			localTextureBuffer.getMemory().mapMemory();
			ImageWriter textureBufferWriter = new ImageWriter(localTextureBuffer, 0, texture, 0, 0, 0, texture.getWidth(), texture.getHeight(), texture.getDepth(), commandBuffer);
			textureBufferWriter.put(image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()));
			textureBufferWriter.close();
			
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			
			commandBuffer.endRecording();
			
			VulkanFuture<?> textureFuture = device.submitSingleUseCommandBuffer(stack, commandBuffer);
			localVertexBuffer.getMemory().unmapMemory();
			localIndexBuffer.getMemory().unmapMemory();
			localTextureBuffer.getMemory().unmapMemory();
			
			VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.maxSets(1)
				.pPoolSizes(VkDescriptorPoolSize.malloc(2, stack)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
					)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.descriptorCount(1)
					)
					.rewind()
				);
			
			VkDescriptorSetLayoutCreateInfo descriptorSetLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.malloc(stack)
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
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
						.pImmutableSamplers(null))
					.rewind()
				);
			
			DescriptorPool descriptorPool = new DescriptorPool(device, descriptorPoolCreateInfo);
			DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device, descriptorSetLayoutCreateInfo);
			DescriptorSet descriptorSet = descriptorPool.allocate(descriptorSetLayout);
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			graphicsPipelineTemplate = GraphicsPipelineTemplate.createScreenRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 20, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(0, 0, 0)
					.addAttribute(1, 0, 12)
				)
				.build();
			shaderProgram = shaderProgramFuture.forceGet();
			graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, window, pipelineLayout, renderPass, 0);
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSet)
				.buffersCount(1)
				.writers(i -> new BufferWriter[] {
					new BufferWriter(uniformBuffer, uniformBuffer, 0, 256),
					BufferWriter.nullWriter()//texture
				})
				.bindingsLayout(
					new ShaderLayoutBinding(VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, ShaderLayout.Type.UNIFORM_BUFFER, 1, List.of(
						ShaderVariable.TRANSFORMATION
					)),
					new ShaderLayoutBinding(VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderLayout.Type.COMBINED_IMAGE_SAMPLER, 1, List.of(
						ShaderVariable.of(ShaderInputType.SAMPLER_2D, TEXTURE_SAMPLER_UNIFORM)
					))
				)
				.build();
			
			renderer.setBuffer(0, vertexBuffer);
			renderer.setIndexBuffer(indexBuffer);
			isRendererDirty = false;
			
			BufferLayout bufferLayout = renderer.getBufferLayout(0)[1];
			bufferLayout.set(TEXTURE_SAMPLER_UNIFORM, texture.getView(), new TextureSampler(device));
			bufferLayout.updateDescriptorSets();
			
			Matrix4f transformation = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
			Matrix4f model = new Matrix4f().translateYTo(-10 * panelPos.y()).translateXTo(-10 * panelPos.x());
			transformation.multiply(model);
			transformation.identity();
			bufferLayout = renderer.getBufferLayout(0)[0];
			bufferLayout.set(TRANSFORMATION_UNIFORM, transformation);
			bufferLayout.flushAndInvalidate();
			
			isModifiedMatrix = true;
			isModified = true;
			textureFuture.join();
			localTextureBuffer.close();
			localVertexBuffer.close();
			localIndexBuffer.close();
		}
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		if (isModified)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				isModified = false;
				if (isModifiedMatrix)
				{
					isModifiedMatrix = false;
					/*
					Matrix4f transformation = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
					Matrix4f model = new Matrix4f().translateYTo(-10 * panelPos.y()).translateXTo(-10 * panelPos.x());
					transformation.multiply(model);
					BufferLayout bufferLayout = renderer.getBufferLayout(0)[0];
					bufferLayout.set(TRANSFORMATION_UNIFORM, transformation);
					bufferLayout.flushAndInvalidate();
					*/
				}
				switch (window.startPresentation())
				{
					case EMPTY_SURFACE:
						return;
					case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
						throw new VulkanException("Presentaton error");
				}
				if (isRendererDirty)
				{
					isRendererDirty = false;
					println("Rebuild graphics pipeline", () -> {
						graphicsPipeline.close();
						graphicsPipeline = graphicsPipelineTemplate.createPipeline(window.getDevice(), shaderProgram, window, pipelineLayout, renderPass, 0);
						renderer.changePipeline(graphicsPipeline, null);
					}, TestProperties.PRINT_LOADING_TIMES);
				}
				
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
				
				commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
				renderer.draw(commandBuffer, 0);
				commandBuffer.endRenderPass();
				commandBuffer.endRecording();
				
				window.endPresentation(commandBuffer);
			}
		}
		window.pollEvents();
	}
	
	public static void end()
	{
		graphicsPipeline.close();
		graphicsPipelineTemplate.close();
		RenderEngine.getVulkan().getWindow().hide();
		synchronized (MONITOR)
		{
			MONITOR.notify();
		}
		RenderEngine.terminate();
	}
}
