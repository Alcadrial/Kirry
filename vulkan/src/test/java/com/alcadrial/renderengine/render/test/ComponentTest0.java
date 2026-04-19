package com.alcadrial.renderengine.render.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
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

import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.annotation.Angle;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.core.component.Entity;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.buffer.AbstractBigVulkanBuffer.PhysicalDeviceTransferState;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.component.MeshComponent;
import com.alcadrial.renderengine.render.component.MeshComponent.Properties;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.binding.BindingInfo;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.texture.Atlas;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureBuffer;
import com.alcadrial.renderengine.render.texture.TextureRegion;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.util.OverflowException;
import com.alcadrial.renderengine.render.util.VulkanFuture;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class ComponentTest0 extends RenderTest {
	
	private static final String SHADER = "shader5";
	
	public static void main(String[] args)
	{
		mainTest(args, ComponentTest0::start, () -> defaultAnimationLoop(ComponentTest0::loop), ComponentTest0::end);
	}
	
	private static MutableList<Entity> entities;
	
	private static RenderPass renderPass;
	private static Renderer renderer;
	private static ShaderProgram shaderProgram;
	private static PipelineLayout pipelineLayout;
	private static GraphicsPipelineTemplate graphicsPipelineTemplate;
	private static GraphicsPipeline graphicsPipeline;
	private static StaticBigBuffer staticBigBuffer;
	private static VulkanBuffer vertexBuffer;
	private static VulkanBuffer indexBuffer;
	private static VulkanBuffer uniformBuffer;
	private static Atlas atlas;
	
	private static ShaderVariable lightPos = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "LIGHT_POS");
	private static ShaderVariable lightIntensity = ShaderVariable.of(ShaderInputType.FLOAT, "LIGHT_INTENSITY");
	private static ShaderVariable spaceFogIntensity = ShaderVariable.of(ShaderInputType.FLOAT, "SPACE_FOG_INTENSITY");
	
	private static @Angle(AngleUnit.RADIANS) float xRotation;
	private static @Angle(AngleUnit.RADIANS) float yRotation;
	private static @Angle(AngleUnit.RADIANS) float zRotation;
	private static boolean isModified;
	
	private static Matrix4f buildModelMatrix(@Angle(AngleUnit.RADIANS) float xRotation, @Angle(AngleUnit.RADIANS) float yRotation, @Angle(AngleUnit.RADIANS) float zRotation)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().rotateX(xRotation));
		matrix.multiply(new Matrix4f().rotateY(yRotation));
		matrix.multiply(new Matrix4f().rotateZ(zRotation));
		matrix.multiply(new Matrix4f().translate(Vector3f.of(-0.5F, -0.5F, -0.5F)));
		matrix.multiply(new Matrix4f().scale(Vector3f.of(1, 1, 1).divideAll((float) Math.sqrt(3))));
		return matrix;
	}
	
	public static void start() throws VulkanException
	{
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
		
		entities = Lists.mutable.of();
		for (int i = 0; i < 10; i++)
		{
			Entity e = Entity.of(MeshComponent.Type.INSTANCE.getId());
			e.setComponent(MeshComponent.Type.INSTANCE, MeshComponent.ofFileObj(getFile("models/model" + i + ".obj")));
			entities.add(e);
		}
		
		VulkanFuture<?> entitiesLoading = RenderEngine.submitAsync(() -> {
			FastList<CompletableFuture<?>> futures = new FastList<>();
			for (Entity e : entities)
			{
				futures.add(e.load().exceptionally(ex -> {
					System.out.println("Failed to load model " + e.getComponent(MeshComponent.Type.INSTANCE));
					if (!FileNotFoundException.class.isAssignableFrom(ex.getCause().getClass())) ex.getCause().printStackTrace();
					return null;
				}));
			}
			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
		});
		
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = VulkanWindow.defaultBuilder()
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.deviceFeatures(f -> f
				.samplerAnisotropy(true)
			)
			.build(1080, 720, "Test");
		Device device = window.getDevice();
		
		window.setCallback((GLFWWindowSizeCallbackI) (_, _, _) -> {
			isModified = true;
		});
		
		registerRotating((x, y) -> {
			xRotation = x;
			yRotation = y;
		});
		
		renderPass = window.getRenderPass();
		
		VulkanFuture<TextureRegion[]> texturesLoading = RenderEngine.submitAsync(() -> {
			atlas = new Atlas(device, 48, 32, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			List<VulkanFuture<TextureBuffer>> futures = new ArrayList<>(10);
			for (int i = 0; i < 10; i++)
			{
				try
				{
					futures.add(Texture.loadToBufferFromFile(device, getFile("textures/model" + i + ".png")));
				}
				catch (Exception e)
				{
					System.out.println("Failed to load model texture");
				}
			}
			TextureRegion[] regions = new TextureRegion[10];
			for (int i = 0; i < 10; i++)
			{
				VulkanFuture<TextureBuffer> future = futures.get(i);
				if (future == null) continue;
				try
				{
					regions[i] = atlas.add(future.get());
				}
				catch (InterruptedException _)
				{
					return regions;
				}
				catch (Exception _)
				{
					System.out.println("Failed to load model texture: " + getFile("textures/model" + i + ".png"));
				}
			}
			atlas.tryFit();
			atlas.draw();
			return regions;
		});
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.maxSets(3)
				.pPoolSizes(VkDescriptorPoolSize.malloc(2, stack)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(2)
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
				.pBindings(VkDescriptorSetLayoutBinding.malloc(3, stack)
					.apply(b -> b
						.binding(0)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT)
						.pImmutableSamplers(null)
					)
					.apply(b -> b
						.binding(1)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
						.pImmutableSamplers(null)
					)
					.apply(b -> b
						.binding(2)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
						.pImmutableSamplers(null)
					)
					.rewind()
				);
			DescriptorPool descriptorPool = new DescriptorPool(device, descriptorPoolCreateInfo);
			DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device, descriptorSetLayoutCreateInfo);
			DescriptorSet[] descriptorSets = descriptorPool.allocates(descriptorSetLayout, 3);
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
			graphicsPipelineTemplate = GraphicsPipelineTemplate.create3dRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 32, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(1, 0, ShaderInputType.VECTOR_FLOAT_2, 12)
					.addAttribute(2, 0, ShaderInputType.VECTOR_FLOAT_3, 20)
				)
				.configureRasterizationState(s -> s.cullMode(VK10.VK_CULL_MODE_FRONT_BIT))
				.build();
			graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, window, pipelineLayout, renderPass, 0);
			uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 6);
			uniformBuffer.getMemory().mapMemory();
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.buffersCount(window.getFramesCount())
				.descriptorSets(descriptorSets)
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 2 * i, 1), 1, List.of(
						ShaderVariable.PROJECTION,
						ShaderVariable.MODEL
					)),
					BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 2 * i + 1, 1), 1, List.of(
						ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "LIGHT_POS"),
						ShaderVariable.of(ShaderInputType.FLOAT, "LIGHT_INTENSITY"),
						ShaderVariable.of(ShaderInputType.FLOAT, "SPACE_FOG_INTENSITY")
					)),
					BindingInfo.ofTexture(atlas.getTexture(), new TextureSampler(device), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderVariable.TEXTURE)
				})
				.build();
			
			Matrix4f projectionMatrix = buildFullSceneMatrix(window, (float) (Math.PI * 2 / 3));
			Matrix4f modelMatrix = buildModelMatrix(xRotation, yRotation, zRotation);
			
			for (int i = 0; i < window.getFramesCount(); i++)
			{
				renderer.useBufferLayoutWriter(i, 0, writer -> {
					writer.set(ShaderVariable.MODEL, modelMatrix);
					writer.set(ShaderVariable.PROJECTION, projectionMatrix);
					writer.flushAndInvalidate();
				});
				renderer.useBufferLayoutWriter(i, 1, writer -> {
					writer.set(lightPos, Vector3f.of(0.3F, 1, 0));
					writer.set(lightIntensity, 0.75F);
					writer.set(spaceFogIntensity, 0.1F);
					writer.flushAndInvalidate();
				});
			}
			
		}
		
		entitiesLoading.join();
		
		int vertexBufferSize = 0;
		int indexBufferSize = 0;
		
		for (Entity e : entities)
		{
			MeshComponent mesh = (MeshComponent) e.getComponent(MeshComponent.Type.INSTANCE);
			if (mesh == null) continue;
			Properties properties = mesh.getProperties();
			vertexBufferSize += properties.vertexesSize();
			vertexBufferSize += properties.textureUvSize();
			vertexBufferSize += properties.normalsSize();
			indexBufferSize += properties.indicesSize();
		}
		
		if (vertexBufferSize == 0 || indexBufferSize == 0)
		{
			System.out.println("No data to render");
			RenderEngine.terminate();
			return;
		}
		
		staticBigBuffer = new StaticBigBuffer(device, vertexBufferSize + indexBufferSize);
		
		vertexBuffer = staticBigBuffer.allocate(vertexBufferSize, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		indexBuffer = staticBigBuffer.allocate(indexBufferSize, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		
		try (PhysicalDeviceTransferState transfert = staticBigBuffer.startLocalBuffering())
		{
			BufferWriter vertexBufferWriter = transfert.beginTransfer(vertexBuffer);
			BufferWriter indexBufferWriter = transfert.beginTransfer(indexBuffer);
			
			VulkanBuffer localVertexBuffer = vertexBufferWriter.getSrcBuffer();
			VulkanBuffer localIndexBuffer = indexBufferWriter.getSrcBuffer();
			
			int vertexBufferOffset = 0;
			int indexBufferOffset = 0;
			
			for (Entity e : entities)
			{
				MeshComponent mesh = (MeshComponent) e.getComponent(MeshComponent.Type.INSTANCE);
				if (mesh == null) continue;
				
				Properties properties = mesh.getProperties();
				int vertexSize = properties.vertexesSize() + properties.textureUvSize() + properties.normalsSize();
				
				VulkanBuffer vertexView = VulkanBuffer.view(localVertexBuffer, vertexBufferOffset, vertexSize);
				VulkanBuffer indexView = VulkanBuffer.view(localIndexBuffer, indexBufferOffset, properties.indicesSize());
				mesh.init(new MeshComponent.Params(8, vertexView, 0, indexView, vertexView, 3, vertexView, 5));
				
				vertexBufferOffset += vertexSize;
				indexBufferOffset += properties.indicesSize();
			}
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
				commandBuffer.copyBuffer(localVertexBuffer, 0, vertexBuffer, 0, vertexBuffer.getSize());
				commandBuffer.copyBuffer(localIndexBuffer, 0, indexBuffer, 0, indexBuffer.getSize());
			});
		}
		catch (OverflowException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		renderer.setBuffer(0, vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
		
		try
		{
			TextureRegion[] regions = texturesLoading.get();
			
			for (int i = 0; i < window.getFramesCount(); i++) renderer.updateDescriptorSet(i, s -> s
				.get(2)
				.pImageInfo()
				.imageView(atlas.getTexture().getView().getVk())
			);
		}
		catch (InterruptedException _)
		{
			RenderEngine.terminate();
			return;
		}
		
		Debug.runOnDebug(() -> {
			renderPass.setName("Renderpass");
			staticBigBuffer.setName("Big buffer");
			vertexBuffer.setName("Vertex buffer");
			indexBuffer.setName("Index buffer");
			uniformBuffer.setName("Uniform buffer");
		});
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanWindow window = RenderEngine.getVulkan().getWindow();
			switch (window.startPresentation())
			{
				case EMPTY_SURFACE:
					return;
				case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
					throw new VulkanException("Presentaton error");
			}
			if (isModified)
			{
				isModified = false;
				println("Resize", () -> {
					graphicsPipeline.close();
					graphicsPipeline = graphicsPipelineTemplate.createPipeline(window.getDevice(), shaderProgram, window, pipelineLayout, renderPass, 0);
					renderer.changePipeline(graphicsPipeline, null);
					
					Matrix4f projectionMatrix = buildFullSceneMatrix(window, (float) (Math.PI * 2 / 3));
					for (int i = 0; i < window.getFramesCount(); i++)
					{
						renderer.useBufferLayoutWriter(i, 0, writer -> {
							writer.set(ShaderVariable.PROJECTION, projectionMatrix);
							writer.flushAndInvalidate();
						});
					}
				}, TestProperties.PRINT_LOADING_TIMES);
			}
			
			CommandBuffer commandBuffer = window.getCurrentCommandBuffer();
			
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
			
			renderer.useBufferLayoutWriter(window.getFrameIndex(), 0, writer -> {
				writer.set(ShaderVariable.MODEL, buildModelMatrix(xRotation, yRotation, zRotation));
				writer.flushAndInvalidate();
			});
			
			commandBuffer.beginRecording();
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, window.getFrameIndex());
			commandBuffer.endRenderPass();
			commandBuffer.endRecording();
			window.endPresentation(commandBuffer);
		}
	}
	
	public static void end()
	{
		graphicsPipeline.close();
		graphicsPipelineTemplate.close();
		pipelineLayout.close();
		uniformBuffer.close();
		staticBigBuffer.close();
		RenderEngine.terminate();
	}
}
