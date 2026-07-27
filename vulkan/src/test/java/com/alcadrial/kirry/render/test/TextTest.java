package com.alcadrial.kirry.render.test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

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
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kirry.core.util.color.ColorRGB;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.buffer.writer.DeferredBufferWriter;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.component.RenderEngine;
import com.alcadrial.kirry.render.font.Font;
import com.alcadrial.kirry.render.font.Font.GlyphData;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.memory.MemoryTransferState;
import com.alcadrial.kirry.render.pipeline.GraphicsPipeline;
import com.alcadrial.kirry.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.kirry.render.pipeline.PipelineLayout;
import com.alcadrial.kirry.render.resources.FileManager;
import com.alcadrial.kirry.render.resources.ShaderResource;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.shader.binding.BindingInfo;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorPool;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSet;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.kirry.render.test.util.RecompileShaderResource;
import com.alcadrial.kirry.render.test.util.RenderTest;
import com.alcadrial.kirry.render.test.util.TestProperties;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.texture.TextureSampler;
import com.alcadrial.kirry.render.util.exception.FreeTypeException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.future.VulkanFuture;
import com.alcadrial.kirry.render.window.VulkanWindow;

public class TextTest extends RenderTest {
	
	public static final String MESSAGE = "Alkaisr Tuilla";
	public static final ColorRGB COLOR = ColorRGB.LIGHT_YELLOW;
	public static final Vector3f POS = Vector3f.of();
	public static final float MAX_WIDTH = 800;
	public static final float MAX_HEIGHT = 500;
	
	private static MemoryManager memoryManager = null;
	
	public static void main(String[] args)
	{
		try
		{
			fileManager = FileManager.of(
				"src/test/resources",
				"src/main/resources",
				FileManager.loaderJarResourceUri(ASSETS)
			);
			fileManager.registerResourceType(ShaderResource.TYPE, RecompileShaderResource::new);
			fileManager.startWatchingResources();
			
			ShaderResource fontVertexShaderResource = fileManager.getResource(RenderEngine.FONT_VERTEX_SHADER_PATHNAME, ShaderResource.TYPE);
			ShaderResource fontGeometryShaderResource = fileManager.getResource(RenderEngine.FONT_GEOMETRY_SHADER_PATHNAME, ShaderResource.TYPE);
			ShaderResource fontFragmentShaderResource = fileManager.getResource(RenderEngine.FONT_FRAGMENT_SHADER_PATHNAME, ShaderResource.TYPE);
			
			TestProperties.init(args);
			KirryVulkan.enableDebug();
			RenderTest.initKirry().multiThreading(false).useFont(true).initialize();
			
			VulkanFuture<ShaderProgram> shaderProgramFuture = VulkanFuture.submitAsync(() -> {
				
				CompletableFuture.allOf(fontVertexShaderResource.load(), fontGeometryShaderResource.load(), fontFragmentShaderResource.load()).join();
				
				return ShaderProgram.create()
					.add(fontVertexShaderResource.getShader())
					.add(fontGeometryShaderResource.getShader())
					.add(fontFragmentShaderResource.getShader())
					.build();
			});
			
			KirryVulkan.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			KirryVulkan.setWindowHint(GLFW.GLFW_VISIBLE, false);
			VulkanWindow window = VulkanWindow.defaultBuilder()
				.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
				.deviceFeatures(f -> f
					.geometryShader(true)
					.samplerAnisotropy(true)
				)
				.useDepthTexture(false)
				.build((int) MAX_WIDTH, (int) MAX_HEIGHT, "Test");
			registerDragging();
			
			Device device = window.getDevice();
			memoryManager = MemoryManager.getDefault(device);
			Font font = new Font(device, getPath(FONTS + "ITCBLKAD.TTF"), 0, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, memoryManager);
			//Font font = new Font(window.getDevice(), getPath(FONTS + "ARIALI.TTF"), 0, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, memoryManager);
			
			font.setSize(120);
			int nonEmptyCharCount = 0;
			for (char c : MESSAGE.toCharArray())
			{
				try
				{
					if (!font.getData(c).isEmpty()) nonEmptyCharCount++;
				}
				catch (FreeTypeException e)
				{
					System.out.println("Failed to load char: " + c);
				}
			}
			
			//font.getAtlas().tryFit();
			System.out.println("Font Atlas size: " + font.getAtlas().getWidth() + "x" + font.getAtlas().getHeight());
			
			VulkanBuffer fontDataStorageBuffer = font.getData();
			
			VulkanBuffer uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 2, (16 + 16 + 4) * 4);
			VulkanBuffer vertexBuffer = memoryManager.allocate(MESSAGE.length() * (1 + 3) * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
			
			CountDownLatch textureAtlasBuilt = new CountDownLatch(1);
			
			POS.setY(font.getHeight());
			
			VulkanFuture<?> dataReady = device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
				font.getAtlas().draw(commandBuffer);
				textureAtlasBuilt.countDown();
				
				try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, vertexBuffer))
				{
					DeferredBufferWriter vertexWriter = transfer.getWriter(vertexBuffer);
					
					Vector3f pos = Vector3f.of(POS);
					for (char c : MESSAGE.toCharArray())
					{
						try
						{
							GlyphData glyphData = font.getData(c);
							if (!glyphData.isEmpty())
							{
								vertexWriter.put(pos);
								vertexWriter.put(glyphData.getIndex());
							}
							pos.add(glyphData.getAdvanceX(), 0);
							if (pos.x() + glyphData.getHorizontalBearingX() + glyphData.getWidth() >= MAX_WIDTH)
							{
								pos.x(POS.x());
								pos.y(pos.y() + font.getHeight());
							}
							if (pos.x() >= MAX_WIDTH || pos.y() >= MAX_HEIGHT)
							{
								break;
							}
						}
						catch (FreeTypeException _)
						{}
					}
				}
			});
			
			DescriptorPool pool;
			DescriptorSetLayout descriptorSetLayout;
			DescriptorSet descriptorSet;
			PipelineLayout pipelineLayout;
			GraphicsPipeline graphicsPipeline;
			Renderer renderer;
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				pool = new DescriptorPool(device, VkDescriptorPoolCreateInfo.malloc(stack)
					.sType$Default()
					.pNext(0)
					.flags(0)
					.maxSets(1)
					.pPoolSizes(VkDescriptorPoolSize.malloc(3, stack)
						.apply(s -> s
							.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
							.descriptorCount(2))
						.apply(s -> s
							.type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
							.descriptorCount(1))
						.apply(s -> s
							.type(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
							.descriptorCount(1))
						.rewind())
				);
				
				descriptorSetLayout = new DescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.malloc(stack)
					.sType$Default()
					.pNext(0)
					.flags(0)
					.pBindings(VkDescriptorSetLayoutBinding.malloc(4, stack)
						.apply(s -> s
							.binding(0)
							.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
							.descriptorCount(1)
							.stageFlags(VK10.VK_SHADER_STAGE_GEOMETRY_BIT)
							.pImmutableSamplers(null))
						.apply(s -> s
							.binding(1)
							.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
							.descriptorCount(1)
							.stageFlags(VK10.VK_SHADER_STAGE_GEOMETRY_BIT)
							.pImmutableSamplers(null))
						.apply(s -> s
							.binding(2)
							.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
							.descriptorCount(1)
							.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
							.pImmutableSamplers(null))
						.apply(s -> s
							.binding(3)
							.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
							.descriptorCount(1)
							.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
							.pImmutableSamplers(null))
						.rewind())
				);
				
				descriptorSet = pool.allocate(descriptorSetLayout);
				
				pipelineLayout = new PipelineLayout(device, VkPipelineLayoutCreateInfo.malloc(stack)
					.sType$Default()
					.pNext(0)
					.flags(0)
					.pSetLayouts(stack.longs(descriptorSetLayout.getVk()))
					.pPushConstantRanges(null)
				);
				
				try (GraphicsPipelineTemplate template = GraphicsPipelineTemplate.createFontRenderingPipeline()
					.configureVertexInputState(s -> s
						.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
						.addAttribute(1, 0, ShaderInputType.UNSIGNED_INT, 12)
						.addBinding(0, 16, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					)
					.build())
				{
					graphicsPipeline = template.createPipeline(device, shaderProgramFuture.get(), window, pipelineLayout, window.getRenderPass(), 0);
				}
				
				textureAtlasBuilt.await();
				renderer = Renderer.builder()
					.graphicsPipeline(graphicsPipeline)
					.descriptorSets(descriptorSet)
					.bindings(
						BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 0, 1, (16 + 16) * 4), VK10.VK_PIPELINE_STAGE_GEOMETRY_SHADER_BIT, List.of(
							ShaderVariable.PROJECTION,
							ShaderVariable.MODEL
						)),
						BindingInfo.ofStorage(fontDataStorageBuffer, VK10.VK_PIPELINE_STAGE_GEOMETRY_SHADER_BIT, List.of(
							Font.GLYPH_SIZE,
							ShaderVariable.PADDING_2,
							Font.GLYPH_BEARING,
							ShaderVariable.PADDING_2,
							Font.GLYPH_UV_START,
							Font.GLYPH_UV_END
						)),
						BindingInfo.ofTexture(font.getAtlas().getTexture(), TextureSampler.ofLinear(device), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderVariable.ATLAS),
						BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 1, 1, (16 * 2 + 4) * 4), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, List.of(
							ShaderVariable.PROJECTION,
							ShaderVariable.MODEL,
							ShaderVariable.COLOR
						))
					)
					.build();
				
				renderer.setBuffer(0, vertexBuffer);
			}
			
			dataReady.join();
			
			Texture texture = font.getAtlas().getTexture();
			
			int w = texture.getWidth();
			int h = texture.getHeight();
			
			renderer.useBufferLayoutWriter(0, 0, writer -> {
				writer.set(ShaderVariable.PROJECTION, new Matrix4f().orthographicPositive(MAX_WIDTH, MAX_HEIGHT));
				writer.set(ShaderVariable.MODEL, new Matrix4f());
			});
			renderer.useBufferLayoutWriter(0, 3, writer -> {
				writer.set(ShaderVariable.PROJECTION, new Matrix4f().orthographic(2 * w, 2 * h));
				writer.set(ShaderVariable.MODEL, new Matrix4f());
				writer.set(ShaderVariable.COLOR, Vector4f.of(COLOR.r(), COLOR.g(), COLOR.b(), COLOR.a()).divideAll(255));
			});
			
			if (!window.shouldClose())
			{
				draw(renderer, nonEmptyCharCount);
				window.show();
				window.waitEvents();
			}
			while (!window.shouldClose())
			{
				draw(renderer, nonEmptyCharCount);
				window.waitEvents();
			}
			font.close();
			memoryManager.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
			if (memoryManager != null) memoryManager.close();
		}
		finally
		{
			KirryVulkan.terminate();
		}
	}
	
	public static void draw(Renderer renderer, int count) throws VulkanException, VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanWindow window = KirryVulkan.getVulkan().getWindow();
			switch (window.startPresentation())
			{
				case EMPTY_SURFACE:
					return;
				case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
					throw new VulkanException("Presentaton error");
			}
			CommandBuffer commandBuffer = window.getCurrentCommandBuffer();
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
			renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
			renderPassBeginInfo.pNext(0);
			renderPassBeginInfo.renderPass(window.getRenderPass().getVk());
			
			FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
			renderPassBeginInfo.framebuffer(frameBuffer.getVk());
			
			VkRect2D renderArea = VkRect2D.calloc(stack);
			renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
			renderPassBeginInfo.renderArea(renderArea);
			
			VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
			clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
			clearValues.get().depthStencil().depth(1).stencil(0);
			renderPassBeginInfo.pClearValues(clearValues.rewind());
			
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, 0, 1, count);
			commandBuffer.endRenderPass();
			window.endPresentation(commandBuffer);
		}
	}
}
