package com.alcadrial.kirry.render.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.multimap.list.MutableListMultimap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Multimaps;
import org.eclipse.collections.impl.utility.LazyIterate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferMemoryBarrier;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kirry.core.component.Entity;
import com.alcadrial.kirry.render.Computer;
import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.buffer.BufferLayoutWriter;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.component.SpatialComponent.State;
import com.alcadrial.kirry.render.memory.MemoryCacheBulkState;
import com.alcadrial.kirry.render.memory.MemoryCacheState;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.memory.MemoryTransferState;
import com.alcadrial.kirry.render.pipeline.ComputePipeline;
import com.alcadrial.kirry.render.pipeline.GraphicsPipeline;
import com.alcadrial.kirry.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.kirry.render.pipeline.PipelineLayout;
import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.renderpass.RenderPassTemplate;
import com.alcadrial.kirry.render.resources.Mesh;
import com.alcadrial.kirry.render.resources.Mesh.Material;
import com.alcadrial.kirry.render.resources.Mesh.Properties;
import com.alcadrial.kirry.render.resources.Mesh.TextureData;
import com.alcadrial.kirry.render.resources.ShaderResource;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.shader.binding.BindingInfo;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorPool;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSet;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.kirry.render.texture.Atlas;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.texture.TextureSampler;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.InvalidEntityException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.functional.VulkanBiConsumer;
import com.alcadrial.kirry.render.util.functional.VulkanFunction;
import com.alcadrial.kirry.render.window.VulkanWindow;

// TODO: implement a wrapper for renderdoc
public class RenderEngine implements AutoCloseable {
	
	public static final String SPATIAL_MOVE_COMPUTE_SHADER_NAME = "spatialMoveShader.comp";
	public static final String SPATIAL_MOVE_COMPUTE_SHADER_PATHNAME = "assets/kirry/shaders/spatialMoveShader.comp";
	public static final String INIT_COMPUTE_SHADER_NAME = "uvInitShader.comp";
	public static final String INIT_COMPUTE_SHADER_PATHNAME = "assets/kirry/shaders/uvInitShader.comp";
	public static final String TRANSFORM_COMPUTE_SHADER_NAME = "uvTransformShader.comp";
	public static final String TRANSFORM_COMPUTE_SHADER_PATHNAME = "assets/kirry/shaders/uvTransformShader.comp";
	
	public static sealed interface PipelineOutputTarget permits SurfaceTarget, TextureTarget {}
	
	public record SurfaceTarget(VulkanWindow window) implements PipelineOutputTarget {}
	
	public record TextureTarget(Texture texture) implements PipelineOutputTarget {}
	
	private class Data {
		
		private static class ProgramRenderer {
			
			ShaderProgram program;
			PipelineLayout pipelineLayout;
			GraphicsPipeline graphicsPipeline;
			Renderer renderer;
			int vertexCount;
			
			public ProgramRenderer(ShaderProgram program, PipelineLayout pipelineLayout, GraphicsPipeline graphicsPipeline, Renderer renderer)
			{
				this.program = program;
				this.pipelineLayout = pipelineLayout;
				this.graphicsPipeline = graphicsPipeline;
				this.renderer = renderer;
			}
		}
		
		private static class Cache {
			
			Map<Mesh, MemoryCacheState> meshes;
			
			public Cache()
			{
				meshes = Maps.mutable.of();
			}
		}
		
		private static final int MESH_RELOAD_INDEX = 0;
		private static final int SHADER_RELOAD_INDEX = 1;
		private static final int MAX_RELOAD_INDEX = 1;
		
		MemoryManager memoryManager;
		VulkanBuffer vertexBuffer;
		VulkanBuffer textureUvBuffer;
		VulkanBuffer uniformBuffer;
		Cache cache;
		
		RenderPass renderPass;
		Vector2ir outputSize;
		int buffersCount;
		
		DescriptorPool descriptorPool;
		GraphicsPipelineTemplate graphicsPipelineTemplate;
		
		/** null key entities use defaultRendering */
		MutableListMultimap<ShaderProgram, Entity> groupedEntities;
		int[] reloadCounters;
		
		@Nullable
		ProgramRenderer defaultRendering;
		MutableList<ProgramRenderer> renderings;
		
		PipelineLayout spatialMoveComputePipelineLayout;
		ComputePipeline spatialMoveComputePipeline;
		Computer spatialMoveComputer;
		
		PipelineLayout initComputePipelineLayout;
		ComputePipeline initComputePipeline;
		Computer initComputer;
		
		PipelineLayout transformComputePipelineLayout;
		ComputePipeline transformComputePipeline;
		Computer transformComputer;
		
		Atlas atlas;
		boolean[] isFrameUniformsDirty;
		int vertexCount;
		
		//@formatter:off
		public Data(MemoryManager memoryManager, VulkanBuffer vertexBuffer, VulkanBuffer textureUvBuffer, VulkanBuffer uniformBuffer, Cache cache,
			PipelineOutputTarget target, RenderPass renderPass, Vector2ir outputSize, int buffersCount, int[] reloadCounters,
			MutableListMultimap<ShaderProgram, Entity> groupedEntities2, @Nullable ProgramRenderer defaultRendering, MutableList<ProgramRenderer> renderings,
			DescriptorPool descriptorPool, GraphicsPipelineTemplate graphicsPipelineTemplate,
			PipelineLayout spatialMoveComputePipelineLayout, ComputePipeline spatialMoveComputePipeline, Computer spatialMoveComputer,
			PipelineLayout initComputePipelineLayout, ComputePipeline initComputePipeline, Computer initComputer,
			PipelineLayout transformComputePipelineLayout, ComputePipeline transformComputePipeline, Computer transformComputer,
			Atlas atlas, boolean[] isFrameUniformsDirty, int vertexCount)
		{
			//@formatter:on
			this.memoryManager = memoryManager;
			this.vertexBuffer = vertexBuffer;
			this.textureUvBuffer = textureUvBuffer;
			this.uniformBuffer = uniformBuffer;
			this.cache = cache;
			this.renderPass = renderPass;
			this.outputSize = outputSize;
			this.buffersCount = buffersCount;
			this.descriptorPool = descriptorPool;
			this.reloadCounters = reloadCounters;
			groupedEntities = groupedEntities2;
			this.defaultRendering = defaultRendering;
			this.renderings = renderings;
			this.graphicsPipelineTemplate = graphicsPipelineTemplate;
			this.spatialMoveComputePipelineLayout = spatialMoveComputePipelineLayout;
			this.spatialMoveComputePipeline = spatialMoveComputePipeline;
			this.spatialMoveComputer = spatialMoveComputer;
			this.initComputePipelineLayout = initComputePipelineLayout;
			this.initComputePipeline = initComputePipeline;
			this.initComputer = initComputer;
			this.transformComputePipelineLayout = transformComputePipelineLayout;
			this.transformComputePipeline = transformComputePipeline;
			this.transformComputer = transformComputer;
			this.atlas = atlas;
			this.isFrameUniformsDirty = isFrameUniformsDirty;
			this.vertexCount = vertexCount;
		}
	}
	
	private Device device;
	private @Nullable VulkanBiConsumer<Integer, BufferLayoutWriter> uniformUpdates;
	private @Nullable Data data;
	
	public RenderEngine(Device device)
	{
		this.device = device;
	}
	
	public void init(@Nullable ShaderProgram defaultProgram, ShaderResource spatialMoveComputeShader, ShaderResource initComputeShader, ShaderResource transformComputeShader, MemoryManager memoryManager, CommandBuffer commandBuffer, PipelineOutputTarget target, MutableList<ShaderVariable> vertexStageUniforms, MutableList<ShaderVariable> fragmentStageUniforms, Collection<Entity> entities) throws VulkanException
	{
		RenderPass renderPass;
		Vector2ir outputSize;
		int buffersCount;
		switch (target)
		{
			case SurfaceTarget t -> {
				VulkanWindow window = t.window;
				renderPass = window.getRenderPass();
				outputSize = window.getSurface().getData().size();
				buffersCount = window.getFramesCount();
			}
			case TextureTarget t -> {
				Texture texture = t.texture;
				try (RenderPassTemplate template = RenderPassTemplate.transformColorDepthTextureTemplate(texture.getFormat(), texture.getSamples()))
				{
					renderPass = template.createRenderPass(device);
				}
				outputSize = Vector2ir.of(texture.getWidth(), texture.getHeight());
				buffersCount = 1;
			}
		}
		CompletableFuture<?> computeShadersFuture = CompletableFuture.allOf(
			spatialMoveComputeShader.load(),
			initComputeShader.load(),
			transformComputeShader.load()
		);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			DescriptorPool descriptorPool = new DescriptorPool(device, VkDescriptorPoolCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.maxSets((buffersCount + 1 + 2) * entities.size())
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
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			
			DescriptorSetLayout computeDescriptorSetLayout = new DescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.malloc(stack)
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
			DescriptorSet[] computingDescriptorSets = descriptorPool.allocates(computeDescriptorSetLayout, 2 * entities.size());
			DescriptorSet[] spatialComputingDescriptorSets = Arrays.copyOfRange(computingDescriptorSets, 0, entities.size());
			DescriptorSet[] uvComputingDescriptorSets = Arrays.copyOfRange(computingDescriptorSets, entities.size(), 2 * entities.size());
			pipelineLayoutInfo.pSetLayouts(stack.longs(computeDescriptorSetLayout.getVk()));
			PipelineLayout computeLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			Atlas atlas = new Atlas(device, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			
			record MeshData(Mesh mesh, int size) {}
			MutableList<MeshData> meshSizes = Lists.mutable.withInitialCapacity(entities.size());
			
			int vertexBufferSize = 0;
			int textureUvBufferSize = 0;
			int vertexCount = 0;
			int[] textureUvSizes = new int[entities.size()];
			int entityMaterialCount = 0;
			
			{
				MutableSet<Mesh> meshes = Sets.mutable.of();
				int j = 0;
				for (Iterator<Entity> i = entities.iterator(); i.hasNext(); j++)
				{
					Entity e = i.next();
					MeshComponent mesh = (MeshComponent) e.getComponent(MeshComponent.TYPE);
					if (mesh == null) continue;
					MeshComponent.Properties properties = mesh.getProperties();
					
					vertexBufferSize += properties.vertexesSize();
					textureUvBufferSize += properties.textureUvSize();
					vertexBufferSize += properties.normalsSize();
					vertexCount += properties.vertexCount();
					textureUvSizes[j] = Math.max(properties.textureUvSize(), 1);
					
					if (!meshes.add(mesh.getMesh())) continue;
					meshSizes.add(new MeshData(mesh.getMesh(), properties.vertexesSize() + properties.textureUvSize() + properties.normalsSize()));
					
					mesh.init(new MeshComponent.Params(null, new MeshComponent.MaterialParams(memoryManager, commandBuffer, atlas)));
				}
				for (Entity e : entities)
				{
					MeshComponent mesh = (MeshComponent) e.getComponent(MeshComponent.TYPE);
					if (mesh == null) continue;
					entityMaterialCount += mesh.getMaterials().size();
				}
			}
			
			//TODO: handle missing uv
			if (vertexBufferSize == 0 || textureUvBufferSize == 0)
			{
				data = null;
				return;
			}
			int minStorageBufferOffsetAlignment = (int) device.getPhysicalDevice().getLimits().minStorageBufferOffsetAlignment();
			
			int[] textureUvOffsets = new int[entityMaterialCount];
			
			{
				int offset = 0;
				int j = 0, k = 0;
				for (Iterator<Entity> i = entities.iterator(); i.hasNext(); j++)
				{
					Entity e = i.next();
					MeshComponent mesh = (MeshComponent) e.getComponent(MeshComponent.TYPE);
					if (mesh == null) continue;
					
					int offset0 = 0;
					for (Material material : mesh.getMaterials())
					{
						TextureData colorTexture = material.colorTexture();
						if (colorTexture == null) continue;
						textureUvOffsets[k] = offset + offset0;
						offset0 += Math.align(colorTexture.vertexCount() * mesh.getTextureUvLength(), minStorageBufferOffsetAlignment);
						k++;
					}
					offset += textureUvSizes[j];
				}
			}
			
			int textureUvOffset = Math.max(8, minStorageBufferOffsetAlignment);
			textureUvBufferSize = vertexCount * textureUvOffset;
			
			Data.Cache cache = new Data.Cache();
			
			MemoryCacheBulkState meshCache = memoryManager.allocateCacheAll(meshSizes.asLazy().collectInt(MeshData::size).toArray(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
			VulkanBuffer vertexBuffer = memoryManager.allocate(vertexBufferSize, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
			VulkanBuffer textureUvBuffer = memoryManager.allocate(textureUvBufferSize, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
			Debug.runOnDebug(() -> {
				vertexBuffer.setName("RenderEngine VertexBuffer");
				textureUvBuffer.setName("RenderEngine TextureUvBuffer");
			});
			
			MutableListMultimap<ShaderProgram, Entity> groupedEntities = Multimaps.mutable.list.of();
			Map<ShaderProgram, VkPipelineShaderStageCreateInfo.Buffer> shaders = Maps.mutable.of();
			
			MutableMap<Mesh, MemoryCacheState> meshCaches = Maps.mutable.of();
			
			if (meshCache.isContinuous())
			{
				VulkanBuffer buffer = (@NonNull VulkanBuffer) meshCache.getBuffer();
				try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, buffer))
				{
					VulkanBuffer localBuffer = transfer.getLocal(buffer);
					int vertexBufferOffset = 0;
					
					int i = 0;
					for (MeshData d : meshSizes)
					{
						Mesh mesh = d.mesh;
						Properties meshProperties = mesh.getProperties();
						meshCaches.put(mesh, meshCache.getState(i++));
						
						int vertexBufferByteSize = meshProperties.vertexesSize() + meshProperties.normalsSize();
						int vertexSize = vertexBufferByteSize / meshProperties.indicesSize();
						int textureUvSize = 2;
						int textureUvBufferOffset = vertexBufferByteSize / 4 + vertexBufferOffset;
						
						mesh.flush(localBuffer, vertexSize, vertexBufferOffset, localBuffer, textureUvSize, textureUvBufferOffset, localBuffer, vertexSize, vertexBufferOffset + meshProperties.vertexesSize() / meshProperties.indicesSize(), null);
						
						vertexBufferOffset += (vertexBufferByteSize + meshProperties.textureUvSize()) / 4;
					}
					commandBuffer.copyBuffer(localBuffer, 0, buffer, 0, buffer.getSize());
				}
			}
			else
			{
				int i = 0;
				for (MemoryCacheState c : meshCache)
				{
					Mesh mesh = meshSizes.get(i).mesh;
					Properties meshProperties = mesh.getProperties();
					meshCaches.put(mesh, c);
					
					int vertexBufferByteSize = meshProperties.vertexesSize() + meshProperties.normalsSize();
					int vertexSize = vertexBufferByteSize / meshProperties.indicesSize();
					int textureUvSize = 2;
					int textureUvBufferOffset = vertexBufferByteSize / 4;
					
					VulkanBuffer buffer = c.getBuffer();
					try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, buffer))
					{
						VulkanBuffer localBuffer = transfer.getLocal(buffer);
						mesh.flush(localBuffer, vertexSize, 0, localBuffer, textureUvSize, textureUvBufferOffset, localBuffer, vertexSize, meshProperties.vertexesSize() / meshProperties.indicesSize(), null);
						commandBuffer.copyBuffer(localBuffer, 0, buffer, 0, buffer.getSize());
					}
				}
			}
			
			int[] vertexOffsets = new int[entities.size()];
			int[] vertexSizes = new int[entities.size()];
			boolean useTextureUv = false;
			boolean useSpatial = false;
			
			{
				ShaderComponent.Params shaderParams = new ShaderComponent.Params(device);
				
				int vertexBufferOffset = 0;
				int textureUvBufferOffset = 0;
				int i = 0;
				for (Entity entity : entities)
				{
					MeshComponent meshComponent = entity.getComponent(MeshComponent.TYPE);
					if (meshComponent == null) continue;
					if (!meshComponent.isLoaded()) continue;
					Mesh mesh = meshComponent.getMesh();
					Properties meshProperties = mesh.getProperties();
					MemoryCacheState cacheState = meshCaches.get(mesh);
					
					commandBuffer.copyBuffer(cacheState.getBuffer(), 0, vertexBuffer, vertexBufferOffset, meshProperties.vertexesSize() + meshProperties.normalsSize());
					commandBuffer.copyBuffer(cacheState.getBuffer(), meshProperties.vertexesSize() + meshProperties.normalsSize(), textureUvBuffer, textureUvBufferOffset, meshProperties.textureUvSize());
					commandBuffer.addCompletionAction(cacheState::release);
					
					vertexOffsets[i] = vertexBufferOffset;
					vertexSizes[i] = meshProperties.vertexesSize() + meshProperties.normalsSize();
					
					vertexBufferOffset += meshProperties.vertexesSize() + meshProperties.normalsSize();
					textureUvBufferOffset += meshProperties.textureUvSize();
					
					if (meshProperties.useTextureUv()) useTextureUv = true;
					
					ShaderComponent shader = entity.getComponent(ShaderComponent.TYPE);
					if (shader == null && defaultProgram == null) throw new InvalidEntityException("A graphical entity without shader cannot be initialized without a default program");
					if (shader != null)
					{
						shader.init(shaderParams);
						shaders.putIfAbsent(shader.getProgram(), shader.getStages());
					}
					groupedEntities.put(shader == null ? defaultProgram : shader.getProgram(), entity);
					
					SpatialComponent spatial = entity.getComponent(SpatialComponent.TYPE);
					if (spatial != null)
					{
						useSpatial = true;
					}
					i++;
				}
				commandBuffer.pipelineBarrier(VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK10.VK_PIPELINE_STAGE_ALL_GRAPHICS_BIT, 0, null, VkBufferMemoryBarrier.malloc(2, stack)
					.apply(b -> b
						.sType(VK10.VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER)
						.pNext(0)
						.srcAccessMask(VK10.VK_ACCESS_MEMORY_WRITE_BIT)
						.dstAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT)
						.srcQueueFamilyIndex(0)
						.dstQueueFamilyIndex(0)
						.buffer(textureUvBuffer.getVk())
						.offset(0)
						.size(textureUvBuffer.getSize())
					).apply(b -> b
						.sType(VK10.VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER)
						.pNext(0)
						.srcAccessMask(VK10.VK_ACCESS_MEMORY_WRITE_BIT)
						.dstAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT)
						.srcQueueFamilyIndex(0)
						.dstQueueFamilyIndex(0)
						.buffer(vertexBuffer.getVk())
						.offset(0)
						.size(vertexBuffer.getSize())
					).rewind(), null);
			}
			
			int renderersCount = shaders.size();
			if (defaultProgram != null) renderersCount++;
			
			int minUniformBufferOffsetAlignment = (int) device.getPhysicalDevice().getLimits().minUniformBufferOffsetAlignment();
			
			int vertexBindingSize = (int) vertexStageUniforms.sumOfInt(v -> v.getType().getLength());
			int fragmentBindingSize = (int) fragmentStageUniforms.sumOfInt(v -> v.getType().getLength());
			int spatialMoveComputeBindingSize = ShaderInputType.VECTOR_FLOAT_4.getLength() * 2;
			int initComputeBindingSize = ShaderInputType.VECTOR_FLOAT_2.getLength() * 2 + ShaderInputType.INT.getLength();
			int transformComputeBindingSize = ShaderInputType.VECTOR_FLOAT_2.getLength() * 3 + ShaderInputType.INT.getLength();
			
			int minVertexSize = Math.align(vertexBindingSize, minUniformBufferOffsetAlignment);
			int minFragmentSize = Math.align(fragmentBindingSize, minUniformBufferOffsetAlignment);
			int minSpatialMoveComputeSize = Math.align(spatialMoveComputeBindingSize, minUniformBufferOffsetAlignment);
			int minInitComputeSize = Math.align(initComputeBindingSize, minUniformBufferOffsetAlignment);
			int minTransformComputeSize = Math.align(transformComputeBindingSize, minUniformBufferOffsetAlignment);
			int minUvComputeSize = Math.max(minInitComputeSize, minTransformComputeSize);
			int minComputeSize = minSpatialMoveComputeSize + minUvComputeSize;
			
			int renderingUniformSize = minVertexSize + minFragmentSize;
			int renderersUniformSize = renderingUniformSize * buffersCount * renderersCount;
			int computeUniformSize = minComputeSize * entities.size();
			int computersUniformSize = computeUniformSize;
			
			int bufferSize = renderersUniformSize + computersUniformSize;
			VulkanBuffer uniformBuffer = VulkanBuffer.ofUniforms(device, bufferSize);
			
			int computerBuffersCount = entityMaterialCount;
			int computerUniformOffset0 = renderersUniformSize;
			
			computeShadersFuture.join();
			ComputePipeline spatialMoveComputePipeline = new ComputePipeline(device, computeLayout, spatialMoveComputeShader.getShader());
			ComputePipeline initComputePipeline = new ComputePipeline(device, computeLayout, initComputeShader.getShader());
			ComputePipeline transformComputePipeline = new ComputePipeline(device, computeLayout, transformComputeShader.getShader());
			
			Computer spatialMoveComputer = Computer.builder()
				.computePipeline(spatialMoveComputePipeline)
				.descriptorSets(spatialComputingDescriptorSets)
				.buffersCount(entities.size())
				.groupCount(Vector3ir.of(256, 1, 1))
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, computerUniformOffset0 + minComputeSize * i, spatialMoveComputeBindingSize), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						ShaderVariable.POSITION,
						ShaderVariable.POSITION_EXTRA_1
					)),
					BindingInfo.ofStorage(VulkanBuffer.view(vertexBuffer, vertexOffsets[i], vertexSizes[i]), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						ShaderVariable.VERTEX_POSITION,
						ShaderVariable.NORMAL_POSITION
					))
				})
				.build();
			int computerUniformOffset1 = computerUniformOffset0 + minSpatialMoveComputeSize;
			
			Computer initComputer = Computer.builder()
				.computePipeline(initComputePipeline)
				.descriptorSets(uvComputingDescriptorSets)
				.buffersCount(computerBuffersCount)
				.groupCount(Vector3ir.of(256, 1, 1))
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, computerUniformOffset1 + minComputeSize * i, initComputeBindingSize), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						ShaderVariable.UV,
						ShaderVariable.UV_EXTRA_1,
						ShaderVariable.OFFSET
					)),
					BindingInfo.ofStorage(VulkanBuffer.view(textureUvBuffer, textureUvOffsets[i], textureUvSizes[i]), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						ShaderVariable.UV
					))
				})
				.build();
			
			Computer transformComputer = Computer.builder()
				.computePipeline(transformComputePipeline)
				.descriptorSets(uvComputingDescriptorSets)
				.buffersCount(computerBuffersCount)
				.groupCount(Vector3ir.of(256, 1, 1))
				.bindings(i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, computerUniformOffset1 + minComputeSize * i, transformComputeBindingSize), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						ShaderVariable.UV,
						ShaderVariable.UV_EXTRA_1,
						ShaderVariable.UV_EXTRA_2,
						ShaderVariable.OFFSET
					)),
					BindingInfo.ofStorage(VulkanBuffer.view(textureUvBuffer, textureUvOffsets[i], textureUvSizes[i]), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
						ShaderVariable.UV
					))
				})
				.build();
			
			if (useSpatial) computeSpatialMoveData(spatialMoveComputer, entities, () -> new Iterator<>() {
				
				@Override
				public boolean hasNext()
				{
					return true;
				}
				
				@Override
				public Vector3fr next()
				{
					return Vector3fr.of();
				}
				
			}, () -> new Iterator<>() {
				
				int k = 0;
				
				@Override
				public boolean hasNext()
				{
					return true;
				}
				
				@Override
				public Integer next()
				{
					return k++;
				}
				
			}, commandBuffer);
			atlas.draw(commandBuffer);
			if (useTextureUv) computeInitData(entities, initComputer, commandBuffer);
			
			GraphicsPipelineTemplate graphicsPipelineTemplate = GraphicsPipelineTemplate.create3dRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 24, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(2, 0, ShaderInputType.VECTOR_FLOAT_3, 12)
					.addBinding(1, 8, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(1, 1, ShaderInputType.VECTOR_FLOAT_2, 0)
				)
				.build();
			
			MutableList<Data.ProgramRenderer> renderings = Lists.mutable.of();
			
			int uniformOffset[] = {0};
			int uniformStride[] = {minVertexSize};
			
			Data.ProgramRenderer defaultRendering = null;
			if (defaultProgram != null)
			{
				defaultRendering = createRenderer(stack, defaultProgram, vertexStageUniforms, fragmentStageUniforms, descriptorPool, pipelineLayoutInfo, renderPass, outputSize, graphicsPipelineTemplate, buffersCount, vertexBuffer, textureUvBuffer, atlas, i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, renderingUniformSize * i + uniformOffset[0], vertexBindingSize), VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, vertexStageUniforms),
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, renderingUniformSize * i + uniformStride[0], fragmentBindingSize), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, fragmentStageUniforms),
					BindingInfo.ofTexture(atlas.getTexture(), new TextureSampler(device), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderVariable.TEXTURE)
				});
				uniformOffset[0] += renderingUniformSize * buffersCount;
				uniformStride[0] += renderingUniformSize * buffersCount;
			}
			for (Entry<ShaderProgram, VkPipelineShaderStageCreateInfo.Buffer> e : shaders.entrySet())
			{
				renderings.add(createRenderer(stack, e.getKey(), vertexStageUniforms, fragmentStageUniforms, descriptorPool, pipelineLayoutInfo, renderPass, outputSize, graphicsPipelineTemplate, buffersCount, vertexBuffer, textureUvBuffer, atlas, i -> new BindingInfo[] {
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, renderingUniformSize * i + uniformOffset[0], vertexBindingSize), VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, vertexStageUniforms),
					BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, renderingUniformSize * i + uniformStride[0], fragmentBindingSize), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, fragmentStageUniforms),
					BindingInfo.ofTexture(atlas.getTexture(), new TextureSampler(device), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderVariable.TEXTURE)
				}));
				uniformOffset[0] += renderingUniformSize * buffersCount;
				uniformStride[0] += renderingUniformSize * buffersCount;
			}
			boolean[] isFrameUniformsDirty = new boolean[renderersCount * buffersCount];
			if (uniformUpdates != null) Arrays.fill(isFrameUniformsDirty, true);
			
			//@formatter:off
			data = new Data(
				memoryManager, vertexBuffer, textureUvBuffer, uniformBuffer, cache,
				target, renderPass, outputSize, buffersCount,
				new int[entities.size() * (Data.MAX_RELOAD_INDEX + 1)], groupedEntities,
				defaultRendering, renderings,
				descriptorPool,	graphicsPipelineTemplate,
				computeLayout, spatialMoveComputePipeline, spatialMoveComputer,
				computeLayout, initComputePipeline, initComputer,
				computeLayout, transformComputePipeline, transformComputer,
				atlas,
				isFrameUniformsDirty,
				vertexCount
			);
			//@formatter:on
			updateBufferBindings(data);
		}
	}
	
	private Data.ProgramRenderer createRenderer(MemoryStack stack, ShaderProgram program, MutableList<ShaderVariable> vertexStageUniforms, MutableList<ShaderVariable> fragmentStageUniforms, DescriptorPool descriptorPool, VkPipelineLayoutCreateInfo pipelineLayoutInfo, RenderPass renderPass, Vector2ir outputSize, GraphicsPipelineTemplate graphicsPipelineTemplate, int buffersCount, VulkanBuffer vertexBuffer, VulkanBuffer textureUvBuffer, Atlas atlas, VulkanFunction<Integer, BindingInfo[]> bindingFactory) throws VulkanException
	{
		Data.ProgramRenderer defaultRendering;
		DescriptorSetLayout graphicsDescriptorSetLayout = new DescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.malloc(stack)
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
		pipelineLayoutInfo.pSetLayouts(stack.longs(graphicsDescriptorSetLayout.getVk()));
		
		PipelineLayout pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
		GraphicsPipeline graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, program, outputSize, pipelineLayout, renderPass, 0);
		
		DescriptorSet[] renderingDescriptorSets = descriptorPool.allocates(graphicsDescriptorSetLayout, buffersCount);
		
		Renderer renderer = Renderer.builder()
			.graphicsPipeline(graphicsPipeline)
			.buffersCount(buffersCount)
			.descriptorSets(renderingDescriptorSets)
			.bindings(bindingFactory)
			.initDescriptorSets(false)
			.build();
		
		long atlasViewVk = atlas.getTexture().getView().getVk();
		
		for (int i = 0; i < buffersCount; i++) renderer.updateDescriptorSet(i, b -> {
			((VkDescriptorImageInfo.@NonNull Buffer) b.get(2).pImageInfo()).imageView(atlasViewVk);
		});
		defaultRendering = new Data.ProgramRenderer(program, pipelineLayout, graphicsPipeline, renderer);
		return defaultRendering;
	}
	
	private void updateBufferBindings(Data data) throws VulkanException
	{
		MutableListMultimap<ShaderProgram, Entity> groupedEntities = data.groupedEntities;
		int[] vertexOffset = {0};
		int[] textureUvOffset = {0};
		forEachRenderer(data, (p, _) -> {
			int vertexCount = 0;
			int vertexSize = 0;
			int textureUvSize = 0;
			for (Entity e : groupedEntities.get(p.program))
			{
				MeshComponent mesh = (@NonNull MeshComponent) e.getComponent(MeshComponent.TYPE);
				MeshComponent.Properties meshProperties = mesh.getProperties();
				vertexCount += meshProperties.vertexCount();
				vertexSize += meshProperties.vertexesSize();
				textureUvSize += meshProperties.textureUvSize();
				vertexSize += meshProperties.normalsSize();
			}
			p.renderer.setBuffer(0, data.vertexBuffer, vertexOffset[0]);
			p.renderer.setBuffer(1, data.textureUvBuffer, textureUvOffset[0]);
			p.vertexCount = vertexCount;
			
			vertexOffset[0] += vertexSize;
			textureUvOffset[0] += textureUvSize;
		});
	}
	
	public void reinit(CommandBuffer commandBuffer, @Nullable ShaderProgram defaultProgram, @Nullable ShaderResource initComputeShader, @Nullable ShaderResource transformComputeShader, @Nullable PipelineOutputTarget target, @Nullable Collection<Entity> addedEntities, @Nullable Collection<Entity> removedEntities) throws VulkanException
	{
		Data data = getData();
		RenderPass renderPass;
		Vector2ir outputSize;
		//TODO: handle buffersCount change
		//int buffersCount;
		switch (target)
		{
			case null -> {
				renderPass = data.renderPass;
				outputSize = data.outputSize;
				//buffersCount = data.buffersCount;
			}
			case SurfaceTarget s -> {
				VulkanWindow window = s.window;
				renderPass = window.getRenderPass();
				outputSize = window.getSurface().getData().size();
				//buffersCount = window.getFramesCount();
			}
			case TextureTarget t -> {
				Texture texture = t.texture;
				try (RenderPassTemplate template = RenderPassTemplate.transformColorDepthTextureTemplate(texture.getFormat(), texture.getSamples()))
				{
					renderPass = template.createRenderPass(device);
				}
				outputSize = Vector2ir.of(texture.getWidth(), texture.getHeight());
				//buffersCount = 1;
			}
		}
		CompletableFuture<?> initComputeShaderFuture = initComputeShader == null ? null : initComputeShader.load();
		CompletableFuture<?> transformComputeShaderFuture = transformComputeShader == null ? null : transformComputeShader.load();
		
		boolean hasNewDefaultProgram = defaultProgram != null && (data.defaultRendering == null || !((Data.@NonNull ProgramRenderer) data.defaultRendering).program.equals(defaultProgram));
		boolean hasNewOutputSize = !data.outputSize.equals(outputSize);
		boolean hasNewRenderPass = !data.renderPass.equals(renderPass);
		
		if (hasNewDefaultProgram || ((hasNewOutputSize || hasNewRenderPass) && data.defaultRendering != null))
		{
			if (data.defaultRendering == null) throw new UnsupportedOperationException("Cannot set a default program after init with a null value");
			Data.ProgramRenderer defaults = ((Data.@NonNull ProgramRenderer) data.defaultRendering);
			
			GraphicsPipeline graphicsPipeline = data.graphicsPipelineTemplate.createPipeline(device, ((@NonNull ShaderProgram) defaultProgram), outputSize, defaults.pipelineLayout, renderPass, 0);
			defaults.renderer.changePipeline(graphicsPipeline, null);
			defaults.graphicsPipeline.close();
			defaults.graphicsPipeline = graphicsPipeline;
			data.outputSize = outputSize;
			data.renderPass = renderPass;
		}
		if (hasNewOutputSize || hasNewRenderPass)
		{
			for (Data.ProgramRenderer p : data.renderings)
			{
				GraphicsPipeline graphicsPipeline = data.graphicsPipelineTemplate.createPipeline(device, p.program, outputSize, p.pipelineLayout, renderPass, 0);
				p.renderer.changePipeline(graphicsPipeline, null);
				p.graphicsPipeline.close();
				p.graphicsPipeline = graphicsPipeline;
			}
		}
		if (initComputeShader != null)
		{
			((@NonNull CompletableFuture<?>) initComputeShaderFuture).join();
			ComputePipeline computePipeline = new ComputePipeline(device, data.initComputePipelineLayout, initComputeShader.getShader());
			data.initComputer.changePipeline(computePipeline, null);
			data.initComputePipeline.close();
			data.initComputePipeline = computePipeline;
		}
		if (transformComputeShader != null)
		{
			((@NonNull CompletableFuture<?>) transformComputeShaderFuture).join();
			ComputePipeline computePipeline = new ComputePipeline(device, data.transformComputePipelineLayout, transformComputeShader.getShader());
			data.transformComputer.changePipeline(computePipeline, null);
			data.transformComputePipeline.close();
			data.transformComputePipeline = computePipeline;
		}
		
		if (removedEntities != null)
		{
			//TODO: support remove entities
		}
		if (addedEntities != null)
		{
			//TODO: support add entities
		}
	}
	
	private void forEachRenderer(Data data, VulkanBiConsumer<Data.ProgramRenderer, Integer> action) throws VulkanException
	{
		if (data.defaultRendering != null) action.accept((Data.@NonNull ProgramRenderer) data.defaultRendering, 0);
		int i = 0;
		for (Data.ProgramRenderer p : data.renderings) action.accept(p, i++);
	}
	
	public void update(CommandBuffer commandBuffer, VkRenderPassBeginInfo renderPassBeginInfo, int frameIndex, float enlapsed) throws VulkanException
	{
		Data data = getData();
		RichIterable<Entity> entities = data.groupedEntities.valuesView();
		
		int reloadsCount = 0;
		int[] reloads = new int[data.reloadCounters.length];
		int[] componentTypeReloads = new int[2];
		{
			int j = 0;
			for (Iterator<Entity> i = entities.iterator(); i.hasNext(); j++)
			{
				Entity e = i.next();
				MeshComponent mesh = e.getComponent(MeshComponent.TYPE);
				if (mesh == null || !mesh.isLoaded()) continue;
				
				if (mesh.validate(null))
				{
					reloads[j] |= 0x1;
					componentTypeReloads[0]++;
				}
				
				ShaderComponent shader = e.getComponent(ShaderComponent.TYPE);
				if (shader != null && shader.isLoaded())
				{
					if (shader.validate(new ShaderComponent.Params(device)))
					{
						reloads[j] |= 0x2;
						componentTypeReloads[1]++;
					}
				}
				if (reloads[j] != 0) reloadsCount++;
			}
		}
		
		if (reloadsCount > 0)
		{
			if (componentTypeReloads[0] > 0)
			{
				MutableList<Entity> reinitUvEntities = Lists.mutable.of();
				try (MemoryStack stack = MemoryStack.stackPush(); MemoryTransferState transfer = data.memoryManager.allocateForTransfer(commandBuffer, data.vertexBuffer, data.textureUvBuffer))
				{
					VulkanBuffer localVertexBuffer = transfer.getLocal(data.vertexBuffer);
					VulkanBuffer localTextureUvBuffer = transfer.getLocal(data.textureUvBuffer);
					int vertexBufferOffset = 0;
					int textureUvBufferOffset = 0;
					VkBufferCopy.Buffer copyVertexRegion = VkBufferCopy.calloc(componentTypeReloads[0], stack);
					VkBufferCopy.Buffer copyTextureUvRegion = VkBufferCopy.calloc(componentTypeReloads[0], stack);
					int j = 0;
					for (Iterator<Entity> i = entities.iterator(); i.hasNext(); j++)
					{
						Entity e = i.next();
						MeshComponent mesh = (@NonNull MeshComponent) e.getComponent(MeshComponent.TYPE);
						
						MeshComponent.Properties meshProperties = mesh.getProperties();
						
						int vertexSize = (meshProperties.vertexesSize() + meshProperties.normalsSize()) / meshProperties.indicesSize();
						int textureUvSize = 2;
						
						if ((reloads[j] & 0x1) != 0)
						{
							mesh.validate(new MeshComponent.Params(new MeshComponent.VertexParams(localVertexBuffer, vertexSize, vertexBufferOffset, localTextureUvBuffer, textureUvSize, textureUvBufferOffset, localVertexBuffer, vertexSize, vertexBufferOffset + meshProperties.vertexesSize() / meshProperties.indicesSize(), null), null));
							copyVertexRegion.get()
								.srcOffset(vertexBufferOffset * 4)
								.dstOffset(vertexBufferOffset * 4)
								.size(meshProperties.vertexCount() * vertexSize * 4);
							copyTextureUvRegion.get()
								.srcOffset(textureUvBufferOffset * 4)
								.dstOffset(textureUvBufferOffset * 4)
								.size(meshProperties.vertexCount() * textureUvSize * 4);
							if (meshProperties.useTextureUv()) reinitUvEntities.add(e);
						}
						
						vertexBufferOffset += meshProperties.vertexCount() * vertexSize;
						textureUvBufferOffset += meshProperties.vertexCount() * textureUvSize;
					}
					commandBuffer.copyBuffer(localVertexBuffer, data.vertexBuffer, copyVertexRegion.rewind());
					commandBuffer.copyBuffer(localTextureUvBuffer, data.textureUvBuffer, copyTextureUvRegion.rewind());
				}
				if (!reinitUvEntities.isEmpty()) computeInitData(reinitUvEntities, data.initComputer, commandBuffer);
			}
			if (componentTypeReloads[1] > 0)
			{
				MutableSet<ShaderProgram> programs = Sets.mutable.of();
				int j = 0;
				for (Iterator<Entity> i = entities.iterator(); i.hasNext(); j++)
				{
					Entity e = i.next();
					if ((reloads[j] & 0x2) == 0) continue;
					ShaderComponent shader = (@NonNull ShaderComponent) e.getComponent(ShaderComponent.TYPE);
					
					ShaderProgram program = shader.getProgram();
					if (programs.add(program)) continue;
					
					Data.ProgramRenderer p = data.renderings.detect(p0 -> p0.program.equals(program));
					GraphicsPipeline graphicsPipeline = data.graphicsPipelineTemplate.createPipeline(device, program, data.outputSize, p.pipelineLayout, data.renderPass, 0);
					p.renderer.changePipeline(graphicsPipeline, null);
					p.graphicsPipeline.close();
					p.graphicsPipeline = graphicsPipeline;
				}
			}
		}
		
		boolean reloadAtlas = false;
		{
			record ActiveEntity(Entity e, Vector3fr p, Integer k) {}
			MutableList<ActiveEntity> activeEntities = Lists.mutable.of();
			int k = 0;
			for (Iterator<Entity> i = entities.iterator(); i.hasNext(); k++)
			{
				Entity e = i.next();
				SpatialComponent spatial = e.getComponent(SpatialComponent.TYPE);
				if (spatial != null)
				{
					State state = spatial.getState();
					if ((reloads[k] & 0x1) != 0)
					{
						activeEntities.add(new ActiveEntity(e, Vector3fr.of(), k));
						if (!state.movement().equals(Vector3fr.of()))
						{
							state.position().add(Vector3f.of(state.movement()).multiplyAll(enlapsed));
						}
					}
					else
					{
						if (!state.movement().equals(Vector3fr.of()))
						{
							activeEntities.add(new ActiveEntity(e, state.position(), k));
							state.position().add(Vector3f.of(state.movement()).multiplyAll(enlapsed));
						}
						else if (state.isManuallyUpdated()[0])
						{
							Vector3fr oldPosition = spatial.getOldPosition();
							if (oldPosition != null)
							{
								activeEntities.add(new ActiveEntity(e, oldPosition, k));
								state.isManuallyUpdated()[0] = false;
							}
						}
					}
				}
			}
			if (!activeEntities.isEmpty())
			{
				computeSpatialMoveData(data.spatialMoveComputer, LazyIterate.collect(activeEntities, ActiveEntity::e), LazyIterate.collect(activeEntities, ActiveEntity::p), LazyIterate.collect(activeEntities, ActiveEntity::k), commandBuffer);
			}
		}
		
		if (reloadAtlas && data.atlas.draw(commandBuffer))
		{
			long atlasViewVk = data.atlas.getTexture().getView().getVk();
			
			forEachRenderer(data, (p, _) -> {
				//TODO: update descriptor sets after update
				//Arrays.fill(data.isFrameUniformsDirty, true);
				for (int i = 0; i < data.buffersCount; i++)
				{
					p.renderer.updateDescriptorSet(i, b -> {
						((VkDescriptorImageInfo.@NonNull Buffer) b.get(2).pImageInfo()).imageView(atlasViewVk);
					});
				}
			});
			int j = 0;
			for (Iterator<Entity> i = entities.iterator(); i.hasNext(); j++)
			{
				Entity e = i.next();
				if ((reloads[j] & 0x1) == 0) continue;
				MeshComponent mesh = (@NonNull MeshComponent) e.getComponent(MeshComponent.TYPE);
				MeshComponent.Properties meshProperties = mesh.getProperties();
				
				data.transformComputer.dispatch(commandBuffer, j, meshProperties.vertexCount(), 1, 1);
			}
		}
		
		commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
		forEachRenderer(data, (p, i) -> render(i, p.renderer, p.vertexCount, data, commandBuffer, frameIndex));
		commandBuffer.endRenderPass();
	}
	
	private void render(int index, Renderer renderer, int vertexCount, Data data, CommandBuffer commandBuffer, int frameIndex) throws VulkanException
	{
		if (data.isFrameUniformsDirty[index * data.buffersCount + frameIndex])
		{
			data.isFrameUniformsDirty[index * data.buffersCount + frameIndex] = false;
			VulkanBiConsumer<Integer, BufferLayoutWriter> action = uniformUpdates;
			if (action != null)
			{
				for (int i = 0; i < 2; i++)
				{
					int binding = i;
					renderer.useBufferLayoutWriter(frameIndex, binding, writer -> action.accept(binding, writer));
				}
			}
			if (onUniformUpdateFinish != null)
			{
				boolean r = false;
				for (boolean b : data.isFrameUniformsDirty) if (b)
				{
					r = true;
					break;
				}
				if (!r)
				{
					((@NonNull Runnable) onUniformUpdateFinish).run();
				}
			}
		}
		
		renderer.draw(commandBuffer, frameIndex, 1, vertexCount);
	}
	
	@Override
	public void close()
	{
		Data data = this.data;
		if (data == null) return;
		data.atlas.close();
	}
	
	private @Nullable Runnable onUniformUpdateFinish;
	
	/**
	 * Called during {@link #update}
	 * @param action
	 */
	public void setUniforms(VulkanBiConsumer<Integer, BufferLayoutWriter> action)
	{
		setUniforms(action, null);
	}
	
	/**
	 * Called during {@link #update}
	 * @param action
	 */
	public void setUniforms(VulkanBiConsumer<Integer, BufferLayoutWriter> action, @Nullable Runnable onUpdateFinish)
	{
		uniformUpdates = action;
		onUniformUpdateFinish = onUpdateFinish;
		Data data = this.data;
		if (data == null) return;
		Arrays.fill(data.isFrameUniformsDirty, true);
	}
	
	private Data getData()
	{
		return VulkanErrorUtils.nonNull(data, "Renderer was not initialized");
	}
	
	private void computeSpatialMoveData(Computer spatialMoveComputer, Iterable<Entity> entities, Iterable<Vector3fr> oldPositions, Iterable<Integer> indices, CommandBuffer commandBuffer) throws VulkanException
	{
		Iterator<Vector3fr> pi = oldPositions.iterator();
		Iterator<Integer> ki = indices.iterator();
		for (Iterator<Entity> i = entities.iterator(); i.hasNext() && pi.hasNext();)
		{
			Entity e = i.next();
			Vector3fr p = pi.next();
			int k = ki.next();
			MeshComponent mesh = e.getComponent(MeshComponent.TYPE);
			if (mesh == null) continue;
			SpatialComponent spatial = e.getComponent(SpatialComponent.TYPE);
			if (spatial == null) continue;
			
			int vertexCount = mesh.getProperties().vertexCount();
			SpatialComponent.State state = spatial.getState();
			
			spatialMoveComputer.useBufferLayoutWriter(k, 0, writer -> {
				writer.set(ShaderVariable.POSITION, p);
				writer.set(ShaderVariable.POSITION_EXTRA_1, state.position());
			});
			
			spatialMoveComputer.dispatch(commandBuffer, k, vertexCount, 1, 1);
		}
	}
	
	private void computeInitData(Collection<Entity> entities, Computer initComputer, CommandBuffer commandBuffer) throws VulkanException
	{
		int k = 0;
		for (Iterator<Entity> i = entities.iterator(); i.hasNext();)
		{
			Entity e = i.next();
			MeshComponent mesh = e.getComponent(MeshComponent.TYPE);
			if (mesh == null) continue;
			MeshComponent.Properties meshProperties = mesh.getProperties();
			if (!meshProperties.useTextureUv()) continue;
			
			for (Material material : mesh.getMaterials())
			{
				TextureData colorTexture = material.colorTexture();
				if (colorTexture == null) continue;
				
				initComputer.useBufferLayoutWriter(k, 0, writer -> {
					writer.set(ShaderVariable.UV, colorTexture.region().getUvStart());
					writer.set(ShaderVariable.UV_EXTRA_1, colorTexture.region().getUvEnd());
					writer.set(ShaderVariable.OFFSET, colorTexture.vertexOffset());
				});
				
				initComputer.dispatch(commandBuffer, k, colorTexture.vertexCount(), 1, 1);
				k++;
			}
		}
	}
}
