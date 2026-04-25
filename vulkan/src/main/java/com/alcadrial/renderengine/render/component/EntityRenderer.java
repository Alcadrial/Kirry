package com.alcadrial.renderengine.render.component;

import java.util.Arrays;
import java.util.List;

import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.core.component.EngineInitParams;
import com.alcadrial.renderengine.core.component.EngineProperties;
import com.alcadrial.renderengine.core.component.EngineUpdateParams;
import com.alcadrial.renderengine.core.component.Entity;
import com.alcadrial.renderengine.core.component.EntityEngine;
import com.alcadrial.renderengine.render.Computer;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.buffer.AbstractBigVulkanBuffer;
import com.alcadrial.renderengine.render.buffer.AbstractBigVulkanBuffer.PhysicalDeviceTransferState;
import com.alcadrial.renderengine.render.buffer.BufferLayoutWriter;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.pipeline.ComputePipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.binding.BindingInfo;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.texture.Atlas;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureRegion;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanBiConsumer;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class EntityRenderer implements EntityEngine<EntityRenderer.InitParams, EntityRenderer.UpdateParams, EntityRenderer.Properties, VulkanException> {
	
	public static final String INIT_COMPUTE_SHADER_NAME = "uvInitShader.comp";
	public static final String TRANSFORM_COMPUTE_SHADER_NAME = "uvTransformShader.comp";
	
	public record InitParams(
		ShaderProgram program,
		PreparedShader initComputeShader,
		PreparedShader transformComputeShader,
		AbstractBigVulkanBuffer<?> bigBuffer,
		PipelineOutputTarget target,
		MutableList<ShaderVariable> vertexStageUniforms,
		MutableList<ShaderVariable> fragmentStageUniforms
	) implements EngineInitParams {}
	
	public record UpdateParams(
		CommandBuffer commandBuffer,
		int frameIndex
	) implements EngineUpdateParams {}
	
	public record Properties(
		int bufferSize
	) implements EngineProperties {}
	
	public static sealed interface PipelineOutputTarget permits SurfaceTarget, TextureTarget {}
	
	public record SurfaceTarget(VulkanWindow window) implements PipelineOutputTarget {}
	
	public record TextureTarget(Texture texture) implements PipelineOutputTarget {}
	
	private record Data(
		AbstractBigVulkanBuffer<?> bigBuffer,
		VulkanBuffer vertexBuffer,
		VulkanBuffer textureUvBuffer,
		VulkanBuffer uniformBuffer,
		PipelineLayout pipelineLayout,
		GraphicsPipeline graphicsPipeline,
		Renderer renderer,
		Computer initComputer,
		Computer transformComputer,
		Atlas atlas,
		boolean[] isFrameUniformsDirty,
		int vertexCount
	) {}
	
	private Device device;
	private MutableList<Entity> entities;
	private int[] reloadCounters;
	private @Nullable VulkanBiConsumer<Integer, BufferLayoutWriter> uniformUpdates;
	private @Nullable Data data;
	
	public EntityRenderer(Device device, MutableList<Entity> entities)
	{
		this.device = device;
		this.entities = entities;
		reloadCounters = new int[entities.size()];
	}
	
	public void init(InitParams params) throws VulkanException
	{
		ShaderProgram program = params.program;
		PreparedShader initComputeShader = params.initComputeShader;
		PreparedShader transformComputeShader = params.transformComputeShader;
		MutableList<ShaderVariable> vertexStageUniforms = params.vertexStageUniforms;
		MutableList<ShaderVariable> fragmentStageUniforms = params.fragmentStageUniforms;
		AbstractBigVulkanBuffer<?> bigBuffer = params.bigBuffer;
		RenderPass renderPass;
		Vector2i outputSize;
		int buffersCount;
		switch (params.target)
		{
			case SurfaceTarget target -> {
				VulkanWindow window = target.window;
				renderPass = window.getRenderPass();
				outputSize = window.getSurface().getData().size();
				buffersCount = window.getFramesCount();
			}
			case TextureTarget target -> {
				Texture texture = target.texture;
				try (RenderPassTemplate template = RenderPassTemplate.transformColorDepthTextureTemplate(texture.getFormat(), texture.getSamples()))
				{
					renderPass = template.createRenderPass(device);
				}
				outputSize = Vector2i.of(texture.getWidth(), texture.getHeight());
				buffersCount = 1;
			}
		}
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			DescriptorPool descriptorPool = new DescriptorPool(device, VkDescriptorPoolCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.maxSets(buffersCount + entities.size())
				.pPoolSizes(VkDescriptorPoolSize.malloc(3, stack)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(2)
					)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.descriptorCount(1)
					)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
						.descriptorCount(1)
					)
					.rewind()
				));
			DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.malloc(stack)
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
				));
			DescriptorSet[] renderingDescriptorSets = descriptorPool.allocates(descriptorSetLayout, buffersCount);
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			
			PipelineLayout pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
			GraphicsPipelineTemplate graphicsPipelineTemplate = GraphicsPipelineTemplate.create3dRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 24, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(2, 0, ShaderInputType.VECTOR_FLOAT_3, 12)
					.addBinding(1, 8, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(1, 1, ShaderInputType.VECTOR_FLOAT_2, 0)
				)
				.build();
			GraphicsPipeline graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, program, outputSize, pipelineLayout, renderPass, 0);
			graphicsPipelineTemplate.close();
			
			descriptorSetLayout = new DescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.pBindings(VkDescriptorSetLayoutBinding.malloc(2, stack)
					.apply(b -> b
						.binding(0)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT)
						.pImmutableSamplers(null))
					.apply(b -> b
						.binding(1)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT)
						.pImmutableSamplers(null))
					.rewind()
				));
			DescriptorSet[] computingDescriptorSets = descriptorPool.allocates(descriptorSetLayout, entities.size());
			pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			PipelineLayout computeLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			ComputePipeline initComputePipeline = new ComputePipeline(device, computeLayout, initComputeShader);
			ComputePipeline transformComputePipeline = new ComputePipeline(device, computeLayout, transformComputeShader);
			
			int minUniformBufferOffsetAlignment = (int) device.getPhysicalDevice().getLimits().minUniformBufferOffsetAlignment();
			
			int vertexBindingSize = (int) vertexStageUniforms.sumOfInt(v -> v.getType().getLength());
			int fragmentBindingSize = (int) fragmentStageUniforms.sumOfInt(v -> v.getType().getLength());
			int initComputeBindingSize = ShaderInputType.VECTOR_FLOAT_2.getLength() * 2;
			int transformComputeBindingSize = ShaderInputType.VECTOR_FLOAT_2.getLength() * 3;
			
			int minVertexSize = Math.ceilDiv(vertexBindingSize, minUniformBufferOffsetAlignment) * minUniformBufferOffsetAlignment;
			int minFragmentSize = Math.ceilDiv(fragmentBindingSize, minUniformBufferOffsetAlignment) * minUniformBufferOffsetAlignment;
			int minInitComputeSize = Math.ceilDiv(initComputeBindingSize, minUniformBufferOffsetAlignment) * minUniformBufferOffsetAlignment;
			int minTransformComputeSize = Math.ceilDiv(transformComputeBindingSize, minUniformBufferOffsetAlignment) * minUniformBufferOffsetAlignment;
			int minComputeSize = Math.max(minInitComputeSize, minTransformComputeSize);
			
			int renderingUniformSize = minVertexSize + minFragmentSize;
			
			int bufferSize = renderingUniformSize * buffersCount + minComputeSize * entities.size();
			VulkanBuffer uniformBuffer = VulkanBuffer.ofUniforms(device, bufferSize);
			
			Atlas atlas = new Atlas(device, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			
			Renderer renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.buffersCount(buffersCount)
				.descriptorSets(renderingDescriptorSets)
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, renderingUniformSize * i, minVertexSize), VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, vertexStageUniforms),
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, renderingUniformSize * i + minVertexSize, fragmentBindingSize), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, fragmentStageUniforms),
					BindingInfo.ofTexture(atlas.getTexture(), new TextureSampler(device), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderVariable.TEXTURE)
				})
				.build();
			
			int vertexBufferSize = 0;
			int textureUvBufferSize = 0;
			int vertexCount = 0;
			int[] textureUvSizes = new int[entities.size()];
			
			for (int i = 0; i < entities.size(); i++)
			{
				Entity e = entities.get(i);
				MeshComponent mesh = (MeshComponent) e.getComponent(MeshComponent.Type.INSTANCE);
				if (mesh == null) continue;
				MeshComponent.Properties properties = mesh.getProperties();
				vertexBufferSize += properties.vertexesSize();
				textureUvBufferSize += properties.textureUvSize();
				vertexBufferSize += properties.normalsSize();
				vertexCount += properties.vertexCount();
				textureUvSizes[i] = Math.max(properties.textureUvSize(), 1);
			}
			//TODO: handle missing uv
			if (vertexBufferSize == 0 || textureUvBufferSize == 0)
			{
				data = null;
				return;
			}
			int minStorageBufferOffsetAlignment = (int) device.getPhysicalDevice().getLimits().minStorageBufferOffsetAlignment();
			
			int[] textureUvOffsets = new int[entities.size()];
			textureUvOffsets[0] = 0;
			for (int i = 1; i < textureUvOffsets.length; i++) textureUvOffsets[i] = textureUvOffsets[i - 1] + Math.ceilDiv(textureUvSizes[i - 1], minStorageBufferOffsetAlignment) * minStorageBufferOffsetAlignment;
			
			int textureUvOffset = Math.max(8, minStorageBufferOffsetAlignment);
			textureUvBufferSize = vertexCount * textureUvOffset;
			
			VulkanBuffer vertexBuffer = bigBuffer.allocate(vertexBufferSize, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
			VulkanBuffer textureUvBuffer = bigBuffer.allocate(textureUvBufferSize, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
			
			renderer.setBuffer(0, vertexBuffer);
			renderer.setBuffer(1, textureUvBuffer);
			
			int offset = renderingUniformSize * buffersCount;
				
			Computer initComputer = Computer.builder()
				.computePipeline(initComputePipeline)
				.descriptorSets(computingDescriptorSets)
				.buffersCount(entities.size())
				.groupCount(Vector3i.of(256, 1, 1))
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, offset + minComputeSize * i, initComputeBindingSize), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, List.of(
						ShaderVariable.UV,
						ShaderVariable.UV_EXTRA_1
					)),
					BindingInfo.ofStorage(VulkanBuffer.view(textureUvBuffer, textureUvOffsets[i], textureUvSizes[i]), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						ShaderVariable.UV
					))
				})
				.build();
			
			
			Computer transformComputer = Computer.builder()
				.computePipeline(transformComputePipeline)
				.descriptorSets(computingDescriptorSets)
				.buffersCount(entities.size())
				.groupCount(Vector3i.of(256, 1, 1))
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, offset + minComputeSize * i, transformComputeBindingSize), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, List.of(
						ShaderVariable.UV,
						ShaderVariable.UV_EXTRA_1,
						ShaderVariable.UV_EXTRA_2
					)),
					BindingInfo.ofStorage(VulkanBuffer.view(textureUvBuffer, textureUvOffsets[i], textureUvSizes[i]), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						ShaderVariable.UV
					))
				})
				.build();
			
			boolean[] isFrameUniformsDirty = new boolean[buffersCount];
			if (uniformUpdates != null) Arrays.fill(isFrameUniformsDirty, true);
			data = new Data(bigBuffer, vertexBuffer, textureUvBuffer, uniformBuffer, pipelineLayout, graphicsPipeline, renderer, initComputer, transformComputer, atlas, isFrameUniformsDirty, vertexCount);
			
			boolean useTextureUv = false;
			try (PhysicalDeviceTransferState transfert = bigBuffer.startLocalBuffering())
			{
				VulkanBuffer localVertexBuffer = transfert.beginTransfer(vertexBuffer).getSrcBuffer();
				VulkanBuffer localTextureUvBuffer = transfert.beginTransfer(textureUvBuffer).getSrcBuffer();
				int vertexBufferOffset = 0;
				int textureUvBufferOffset = 0;
				
				for (Entity entity : entities)
				{
					MeshComponent mesh = entity.getComponent(MeshComponent.Type.INSTANCE);
					if (mesh == null) continue;
					if (!mesh.isLoaded()) continue;
					
					MeshComponent.Properties meshProperties = mesh.getProperties();
					
					int vertexSize = (meshProperties.vertexesSize() + meshProperties.normalsSize()) / meshProperties.indicesSize();
					int textureUvSize = 2;
					
					mesh.init(new MeshComponent.Params(localVertexBuffer, vertexSize, vertexBufferOffset, localTextureUvBuffer, textureUvSize, textureUvBufferOffset, localVertexBuffer, vertexSize, vertexBufferOffset + meshProperties.vertexesSize() / meshProperties.vertexCount() / 4, null));
					
					vertexBufferOffset += vertexSize * meshProperties.vertexCount();
					textureUvBufferOffset += textureUvSize * meshProperties.vertexCount();
					
					if (!meshProperties.useTextureUv()) continue;
					useTextureUv = true;
					
					ColorTextureComponent colorTexture = entity.getComponent(ColorTextureComponent.Type.INSTANCE);
					if (colorTexture == null) continue;
					colorTexture.init(new ColorTextureComponent.Params(atlas));
				}
				device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
					commandBuffer.copyBuffer(localVertexBuffer, 0, vertexBuffer, 0, vertexBuffer.getSize());
					commandBuffer.copyBuffer(localTextureUvBuffer, 0, textureUvBuffer, 0, textureUvBuffer.getSize());
				}).join();
			}
			
			if (useTextureUv)
			{
				computeInitData(initComputer, uniformBuffer);
			}
			atlas.draw();
			long atlasViewVk = atlas.getTexture().getView().getVk();
			for (int i = 0; i < buffersCount; i++) renderer.updateDescriptorSet(i, b -> {
				((VkDescriptorImageInfo.@NonNull Buffer) b.get(2).pImageInfo()).imageView(atlasViewVk);
			});
		}
	}
	
	public void setUniforms(VulkanBiConsumer<Integer, BufferLayoutWriter> action)
	{
		uniformUpdates = action;
		Data data = this.data;
		if (data == null) return;
		Arrays.fill(data.isFrameUniformsDirty, true);
	}
	
	@Override
	public void update(UpdateParams params) throws VulkanException
	{
		Data data = getData();
		//TODO: support entity removal
		if (reloadCounters.length < entities.size()) reloadCounters = Arrays.copyOf(reloadCounters, entities.size());
		
		int reloadsCount = 0;
		boolean[] meshReloads = new boolean[reloadCounters.length];
		for (int i = 0; i < reloadCounters.length; i++)
		{
			Entity entity = entities.get(i);
			MeshComponent mesh = entity.getComponent(MeshComponent.Type.INSTANCE);
			if (mesh == null || !mesh.isLoaded()) continue;
			MeshComponent.Properties meshProperties = mesh.getProperties();
			if (meshProperties.vertexCount() == 0) continue;
			int meshReloadCount = mesh.reloadCount();
			if (meshReloadCount != reloadCounters[i])
			{
				reloadCounters[i] = meshReloadCount;
				meshReloads[i] = true;
				reloadsCount++;
			}
		}
		
		if (reloadsCount > 0)
		{
			try (PhysicalDeviceTransferState transfert = data.bigBuffer.startLocalBuffering())
			{
				VulkanBuffer localVertexBuffer = transfert.beginTransfer(data.vertexBuffer).getSrcBuffer();
				VulkanBuffer localTextureUvBuffer = transfert.beginTransfer(data.textureUvBuffer).getSrcBuffer();
				int vertexBufferOffset = 0;
				int textureUvBufferOffset = 0;
				
				for (int i = 0; i < reloadCounters.length; i++)
				{
					Entity entity = entities.get(i);
					MeshComponent mesh = entity.getComponent(MeshComponent.Type.INSTANCE);
					if (mesh == null || !meshReloads[i]) continue;
					
					MeshComponent.Properties meshProperties = mesh.getProperties();
					
					int vertexSize = (meshProperties.vertexesSize() + meshProperties.normalsSize()) / meshProperties.indicesSize();
					int textureUvSize = 2;
					
					mesh.reinit(new MeshComponent.Params(localVertexBuffer, vertexSize, vertexBufferOffset, localTextureUvBuffer, textureUvSize, textureUvBufferOffset, localVertexBuffer, vertexSize, vertexBufferOffset + meshProperties.vertexesSize() / meshProperties.vertexCount(), null));
					
					vertexBufferOffset += vertexSize;
					textureUvBufferOffset += textureUvSize;
				}/*
				device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
					commandBuffer.copyBuffer(localVertexBuffer, 0, data.vertexBuffer, 0, data.vertexBuffer.getSize());
					commandBuffer.copyBuffer(localTextureUvBuffer, 0, data.textureUvBuffer, 0, data.textureUvBuffer.getSize());
				}).join();*/
			}
		}
		
		boolean reloadAtlas = false;
		for (int i = 0; i < reloadCounters.length; i++)
		{
			Entity entity = entities.get(i);
			MeshComponent mesh = entity.getComponent(MeshComponent.Type.INSTANCE);
			if (mesh == null || !mesh.isLoaded()) continue;
			if (meshReloads[i])
			{
				//notify other components
				SpatialComponent spatial = entity.getComponent(SpatialComponent.Type.INSTANCE);
				if (spatial != null)
				{
					//compute vertex data difference from gpu
					Vector3f position = Vector3f.of(0, 0, 0);
					
					spatial.reinit(new SpatialComponent.Params(position, null));
				}
			}
			ColorTextureComponent colorTexture = entity.getComponent(ColorTextureComponent.Type.INSTANCE);
			if (colorTexture == null) continue;
			if (colorTexture.isLoaded())
			{
				colorTexture.reinit(new ColorTextureComponent.Params(data.atlas));
				reloadAtlas = true;
			}
		}
		
		if (reloadAtlas)
		{
			/*
			data.atlas.draw();
			long atlasViewVk = data.atlas.getTexture().getView().getVk();
			for (int i = 0; i < data.isFrameUniformsDirty.length; i++) data.renderer.updateDescriptorSet(i, b -> {
				((VkDescriptorImageInfo.@NonNull Buffer) b.get(2).pImageInfo()).imageView(atlasViewVk);
			});
			device.configureAndSubmitSingleUseCommandBuffer(device.getComputeQueue(), commandBuffer -> {
				for (int i = 0; i < reloadCounters.length; i++)
				{
					Entity entity = entities.get(i);
					MeshComponent mesh = entity.getComponent(MeshComponent.Type.INSTANCE);
					if (mesh == null) continue;
					MeshComponent.Properties meshProperties = mesh.getProperties();
					if (!meshReloads[i]) continue;
					
					data.transformComputer.dispatch(commandBuffer, i++, meshProperties.vertexCount(), 1, 1);
				}
			}).join();*/
		}
		
		int frameIndex = params.frameIndex;
		if (data.isFrameUniformsDirty[frameIndex])
		{
			data.isFrameUniformsDirty[frameIndex] = false;
			VulkanBiConsumer<Integer, BufferLayoutWriter> action = uniformUpdates;
			if (action != null)
			{
				data.uniformBuffer.getMemory().mapMemory();
				for (int i = 0; i < 2; i++)
				{
					int binding = i;
					data.renderer.useBufferLayoutWriter(frameIndex, binding, writer -> action.accept(binding, writer));
				}
				data.uniformBuffer.getMemory().unmapMemory();
			}
		}
		
		data.renderer.draw(params.commandBuffer, params.frameIndex, 1, data.vertexCount);
	}
	
	public Properties getProperties()
	{
		int textureUvSize = 0;
		int bufferSize = 0;
		int vertexCount = 0;
		
		for (Entity e : entities)
		{
			MeshComponent mesh = (MeshComponent) e.getComponent(MeshComponent.Type.INSTANCE);
			if (mesh == null) continue;
			MeshComponent.Properties properties = mesh.getProperties();
			bufferSize += properties.vertexesSize();
			textureUvSize += properties.textureUvSize();
			bufferSize += properties.normalsSize();
			vertexCount += properties.vertexCount();
		}
		int minStorageBufferOffsetAlignment = (int) device.getPhysicalDevice().getLimits().minStorageBufferOffsetAlignment();
		int textureUvOffset = Math.max(8, minStorageBufferOffsetAlignment);
		textureUvSize = vertexCount * textureUvOffset;
		return new Properties(bufferSize + textureUvSize);
	}
	
	@Override
	public void close()
	{
		
	}
	
	private Data getData()
	{
		return VulkanErrorUtils.nonNull(data, "Renderer was not initialized");
	}
	
	private void computeInitData(Computer initComputer, VulkanBuffer uniform) throws VulkanException
	{
		uniform.getMemory().mapMemory();
		for (int i = 0; i < reloadCounters.length; i++)
		{
			Entity entity = entities.get(i);
			MeshComponent mesh = entity.getComponent(MeshComponent.Type.INSTANCE);
			if (mesh == null) continue;
			MeshComponent.Properties meshProperties = mesh.getProperties();
			if (!meshProperties.useTextureUv()) continue;
			ColorTextureComponent colorTexture = entity.getComponent(ColorTextureComponent.Type.INSTANCE);
			if (colorTexture == null) continue;
			ColorTextureComponent.Properties colorTextureProperties = colorTexture.getProperties();
			TextureRegion region = colorTextureProperties.region();
			if (region == null) continue;
			
			initComputer.useBufferLayoutWriter(i, 0, writer -> {
				writer.set(ShaderVariable.UV, region.getUvStart());
				writer.set(ShaderVariable.UV_EXTRA_1, region.getUvEnd());
			});
		}
		uniform.getMemory().unmapMemory();
		device.configureAndSubmitSingleUseCommandBuffer(device.getComputeQueue(), commandBuffer -> {
			for (int i = 0; i < reloadCounters.length; i++)
			{
				Entity entity = entities.get(i);
				MeshComponent mesh = entity.getComponent(MeshComponent.Type.INSTANCE);
				if (mesh == null) continue;
				MeshComponent.Properties meshProperties = mesh.getProperties();
				if (!meshProperties.useTextureUv()) continue;
				ColorTextureComponent colorTexture = entity.getComponent(ColorTextureComponent.Type.INSTANCE);
				if (colorTexture == null) continue;
				
				initComputer.dispatch(commandBuffer, i, meshProperties.vertexCount(), 1, 1);
			}
		}).join();
	}
}
