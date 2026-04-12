package com.alcadrial.renderengine.render.test;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
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
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.binding.BindingInfo;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.RenderingDataUtils;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.texture.Atlas;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureBuffer;
import com.alcadrial.renderengine.render.texture.TextureRegion;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.util.VulkanFuture;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class AtlasTest {
	
	public static void main(String[] args)
	{
		try
		{
			TestProperties.init(args);
			RenderEngine.enableDebug();
			RenderTest.initRenderEngine();

			RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			VulkanWindow window = VulkanWindow.defaultBuilder()
				.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
				.deviceFeatures(f -> f
					.samplerAnisotropy(true)
				)
				.useDepthTexture(false)
				.build(64, 64, "Test");
			
			Atlas atlas = new Atlas(window.getDevice(), 1024, 512, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, true);
			
			List<TextureBuffer> textures = new ArrayList<>(32);
			
			for (int i = 0; i < 32; i++)
			{
				try
				{
					textures.add(Texture.loadToBufferFromFile(window.getDevice(), RenderTest.getFile("images/icon" + i + ".png")).get());
				}
				catch (Exception e)
				{
					System.out.println(e.getCause().getMessage());
				}
			}
			
			List<TextureRegion> regions = atlas.addAll(textures.toArray(TextureBuffer[]::new));
			
			atlas.draw();
			
			System.out.println("Size: " + atlas.getWidth() + "x" + atlas.getHeight());
			for (TextureRegion region : regions)
			{
				System.out.println("Region: " + region.getUvStart() + " -> " + region.getUvEnd());
			}
			
			int w = atlas.getWidth();
			int h = atlas.getHeight();
			float scale = 1024F / w;
			
			window.setSize((int) (w * scale), (int) (h * scale));
			window.pollEvents();
			window.getSurface().reinit();
			
			renderTexture(window, atlas.getTexture());
			
			while (!window.shouldClose())
			{
				Thread.sleep(50);
				window.pollEvents();
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
		finally {
			RenderEngine.terminate();
		}
	}
	
	private static final String SHADER = "shader0";
	
	public static void renderTexture(VulkanWindow window, Texture texture) throws VulkanException
	{
		VulkanFuture<ShaderProgram> shaderProgramFuture = RenderEngine.submitAsync(() -> {
			PreparedShader vertexShader = RenderTest.loadShader(SHADER + ".vert");
			PreparedShader fragmentShader = RenderTest.loadShader(SHADER + ".frag");
			return ShaderProgram.create().add(vertexShader, fragmentShader).build();
		});
		
		Device device = window.getDevice();
		
		VulkanBuffer vertexBuffer = new VulkanBuffer(device , 28 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		VulkanBuffer indexBuffer = new VulkanBuffer(device, 6 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		VulkanBuffer uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 1);
		uniformBuffer.getMemory().mapMemory();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanBuffer localVertexBuffer = VulkanBuffer.asLocal(vertexBuffer);
			VulkanBuffer localIndexBuffer = VulkanBuffer.asLocal(indexBuffer);
			
			localVertexBuffer.getMemory().mapMemory();
			localIndexBuffer.getMemory().mapMemory();
			
			CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
			
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			BufferWriter vertexBufferWriter = new BufferWriter(localVertexBuffer, vertexBuffer);
			vertexBufferWriter.setCommandBuffer(commandBuffer);
			BufferWriter indexBufferWriter = new BufferWriter(localIndexBuffer, indexBuffer);
			indexBufferWriter.setCommandBuffer(commandBuffer);
			
			RenderingDataUtils.loadRectangleUv(vertexBufferWriter, indexBufferWriter, 0, -1, -1, 1, 1, 1, 0, 0, 1, 1);
			vertexBufferWriter.close();
			indexBufferWriter.close();
			commandBuffer.endRecording();
			
			device.submitSingleUseCommandBuffer(stack, commandBuffer).join();
			
			localVertexBuffer.getMemory().unmapMemory();
			localIndexBuffer.getMemory().unmapMemory();
			localVertexBuffer.close();
			localIndexBuffer.close();
			
			RenderPass renderPass = window.getRenderPass();
			
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
						.descriptorCount(2)
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
						.descriptorCount(2)
						.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
						.pImmutableSamplers(null))
					.rewind()
				);
			
			DescriptorPool descriptorPool = new DescriptorPool(device, descriptorPoolCreateInfo);
			DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device, descriptorSetLayoutCreateInfo);
			DescriptorSet descriptorSet = descriptorPool.allocate(descriptorSetLayout);
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			PipelineLayout pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			GraphicsPipelineTemplate graphicsPipelineTemplate = GraphicsPipelineTemplate.createScreenRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 20, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(1, 0, ShaderInputType.VECTOR_FLOAT_2, 12)
				)
				.build();
			ShaderProgram shaderProgram = shaderProgramFuture.forceGet();
			GraphicsPipeline graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, window, pipelineLayout, renderPass, 0);
			Renderer renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSet)
				.bindings(
					BindingInfo.ofUniform(uniformBuffer, VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, List.of(
						ShaderVariable.TRANSFORMATION
					)),
					BindingInfo.ofTextures(VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
						.add(texture, new TextureSampler(device), ShaderVariable.TEXTURE)
						.add(texture, new TextureSampler(device), ShaderVariable.ATLAS)
						.build()
				)
				.build();
			
			renderer.setBuffer(0, vertexBuffer);
			renderer.setIndexBuffer(indexBuffer);
			
			float textureAspectRatio = (float)texture.getHeight() / texture.getWidth();
			float scale = Math.min(window.getWidth(), window.getHeight());
			Matrix4f transformation = new Matrix4f().scale(Vector3f.of(scale / window.getWidth() / textureAspectRatio, -scale / window.getHeight(), 0.02F));
			
			renderer.useBufferLayoutWriter(0, 0, writer -> {
				writer.set(ShaderVariable.TRANSFORMATION, transformation);
				writer.flushAndInvalidate();
			});
			
			switch (window.startPresentation())
			{
				case EMPTY_SURFACE:
					return;
				case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
					throw new VulkanException("Presentaton error");
			}
			
			FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
			commandBuffer = window.getCurrentCommandBuffer();
			
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
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, 0);
			commandBuffer.endRenderPass();
			commandBuffer.endRecording();
			
			window.endPresentation(commandBuffer);
			graphicsPipeline.close();
			graphicsPipelineTemplate.close();
			pipelineLayout.close();
		}
	}
}
