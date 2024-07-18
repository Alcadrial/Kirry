package com.alcadrial.renderengine.render;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline.GraphicsPipelineBuildFunction;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderLayoutBinding;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderUtils;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
import com.alcadrial.renderengine.render.util.DataAccumulator;
import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class Renderer implements AutoCloseable, VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static Builder create()
	{
		return new Builder();
	}
	
	public enum DrawResult {
		SUCCESS,
		FAILED,
		NO_DRAWING_AREA;
	}
	
	private ModificationPlan modificationPlan;
	private Device device;
	private GraphicsPipeline graphicsPipeline;
	private Map<Integer, List<VertexBuffer>> vertexBuffers;
	private IndexBuffer indexBuffer;
	private long[] descriptorSetLayouts;
	private long[][] descriptorSets;
	private long descriptorPool;
	private List<Pair<VulkanBuffer, BufferLayout>> uniformBuffers;
	private List<Deque<BiConsumer<VulkanBuffer, BufferLayout>>> uniformBuffersUpdates;
	private List<Consumer<CommandBuffer>> extraCommands;
	private int uniformBufferIndex;
	
	private Renderer(Builder builder, Device device, GraphicsPipeline graphicsPipeline, long[] descriptorSetLayouts, long[][] descriptorSets, long descriptorPool, List<Pair<VulkanBuffer, BufferLayout>> uniformBuffers)
	{
		modificationPlan = new ModificationPlan(builder);
		this.device = device;
		this.graphicsPipeline = graphicsPipeline;
		this.descriptorSetLayouts = descriptorSetLayouts;
		this.descriptorSets = descriptorSets;
		this.descriptorPool = descriptorPool;
		this.uniformBuffers = uniformBuffers;
		uniformBuffersUpdates = new LinkedList<>();
		vertexBuffers = new HashMap<>();
		extraCommands = new LinkedList<>();
		uniformBufferIndex = -1;
		
		for (int i = 0; i < uniformBuffers.size(); i++)
		{
			LinkedList<BiConsumer<VulkanBuffer, BufferLayout>> list = new LinkedList<>();
			list.add((b, l) -> {});
			uniformBuffersUpdates.add(list);
		}
	}
	
	public VertexAccumulator accumulate(VertexBuffer vertexBuffer)
	{
		return new VertexAccumulator(graphicsPipeline, vertexBuffer);
	}
	
	public void updateUniformBuffers(BiConsumer<VulkanBuffer, BufferLayout> action)
	{
		for (Deque<BiConsumer<VulkanBuffer, BufferLayout>> queue : uniformBuffersUpdates) queue.add(action);
	}
	
	public void addExtraCommand(Consumer<CommandBuffer> command)
	{
		extraCommands.add(command);
	}
	
	public void setUniformBufferIndex(int uniformBufferIndex)
	{
		this.uniformBufferIndex = uniformBufferIndex;
	}
	
	public void draw(CommandBuffer commandBuffer)
	{
		draw(commandBuffer, 1);
	}
	
	public void draw(CommandBuffer commandBuffer, int instanceCount)
	{
		if (uniformBufferIndex >= 0)
		{
			Deque<BiConsumer<VulkanBuffer, BufferLayout>> queue = uniformBuffersUpdates.get(uniformBufferIndex);
			boolean update = queue.size() > 0;
			
			BiConsumer<VulkanBuffer, BufferLayout> action;
			while ((action = queue.poll()) != null) for (Pair<VulkanBuffer, BufferLayout> p : uniformBuffers) action.accept(p.left, p.right);
			
			BufferLayout layout = uniformBuffers.get(uniformBufferIndex).right;
			if (update) layout.updateDescriptorSets();
		}
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			commandBuffer.bindPipeline(graphicsPipeline);
			for (Entry<Integer, List<VertexBuffer>> p : vertexBuffers.entrySet()) commandBuffer.bindVertexBuffers(p.getKey(), p.getValue());
			if (uniformBufferIndex >= 0) commandBuffer.bindDescriptorSets(graphicsPipeline, descriptorSets[uniformBufferIndex], new int[0]);
			if (indexBuffer != null)
			{
				commandBuffer.bindIndexBuffer(indexBuffer, 0);
				commandBuffer.drawIndexed((int) (indexBuffer.getSize() / 4), instanceCount, 0, 0, 0);
			}
			else
			{
				commandBuffer.draw(0, instanceCount, 0, 0);
			}
			
			for (Consumer<CommandBuffer> command : extraCommands) command.accept(commandBuffer);
		}
	}
	
	public ModificationPlan startModificationPlan()
	{
		modificationPlan.pushStack();
		return modificationPlan;
	}
	
	public ModificationPlan getCurrentModificationPlan()
	{
		return modificationPlan;
	}
	
	public void addVertexBuffer(int binding, VertexBuffer vertexBuffer)
	{
		vertexBuffers.computeIfAbsent(binding, k -> new LinkedList<>()).add(vertexBuffer);
	}
	
	public void resetVertexBuffer(int binding)
	{
		vertexBuffers.put(binding, new LinkedList<>());
	}
	
	public void setIndexBuffer(IndexBuffer indexBuffer)
	{
		this.indexBuffer = indexBuffer;
	}
	
	@Override
	public void close()
	{
		device.waitIdle();
		graphicsPipeline.close();
		for (long l : descriptorSetLayouts) VK10.vkDestroyDescriptorSetLayout(device.getVk(), l, null);
		VK10.vkDestroyDescriptorPool(device.getVk(), descriptorPool, null);
	}
	
	@Override
	public void debug()
	{
		Debug.print("Device", device);
		Debug.print("Pipeline ", graphicsPipeline);
		Debug.print("DescriptorPool: " + Debug.address(descriptorPool));
		Debug.print("VertexBuffers [");
		Debug.indent();
		for (Entry<Integer, List<VertexBuffer>> e : vertexBuffers.entrySet())
		{
			Debug.print("Binding " + e.getKey() + " [");
			Debug.indent();
			for (VertexBuffer vertexBuffer : e.getValue()) Debug.print("Buffer", vertexBuffer);
			Debug.deindent();
			Debug.print("]");
		}
		Debug.deindent();
		Debug.print("]");
		Debug.print("UniformBuffers [");
		Debug.indent();
		for (int i = 0; i < uniformBuffers.size(); i++)
		{
			Debug.print((i == uniformBufferIndex ? "Current " : "Set ") + i + " {");
			Debug.indent();
			Debug.print("DescriptorSetLayout: " + Debug.address(descriptorSetLayouts[i]));
			Debug.print("DescriptorSet: " + Debug.address(descriptorSets[i][0]));
			Debug.print("Buffer", uniformBuffers.get(i).l());
			Debug.print("Layout", uniformBuffers.get(i).r());
			Debug.deindent();
			Debug.print("}");
		}
		Debug.deindent();
		Debug.print("]");
	}
	
	public class ModificationPlan extends RendererDataAccumulator<ModificationPlan> {
		
		private boolean isActive;
		
		private ModificationPlan(RendererDataAccumulator<?> defaultSource)
		{
			super(defaultSource);
			isActive = false;
		}
		
		@Override
		public ModificationPlan device(@Nullable Device device)
		{
			return super.device(device);
		}
		
		public void submit()
		{
			submit(GraphicsPipeline::new);
		}
		
		public void submit(GraphicsPipelineBuildFunction<?> builderFunction)
		{
			if (isActive)
			{
				synchronized (Renderer.this)
				{
					if (isActive) submit0(builderFunction);
				}
			}
		}
		
		private void pushStack()
		{
			stack.push();
			isActive = true;
		}
		
		private void submit0(GraphicsPipelineBuildFunction<?> builderFunction)
		{
			boolean deviceModified = device.isModified();
			
			physicalDevice.set(device.require().getPhysicalDevice());
			
			if (deviceModified || shaderProgram.isModified())
			{
				createUniformBuffers(Renderer.this.descriptorSetLayouts.length);
				
				VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
				pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayouts));
				
				pipelineLayout.set(new PipelineLayout(device.get(), pipelineLayoutInfo));
				
				long[] descriptorSetLayouts0 = Renderer.this.descriptorSetLayouts;
				long descriptorPool0 = Renderer.this.descriptorPool;
				
				device.get().waitAsyncIdle(device0 -> {
					for (long l : descriptorSetLayouts0) VK10.vkDestroyDescriptorSetLayout(device0.getVk(), l, null);
					VK10.vkDestroyDescriptorPool(device0.getVk(), descriptorPool0, null);
				});
				
				Renderer.this.descriptorSetLayouts = descriptorSetLayouts;
				Renderer.this.descriptorSets = descriptorSets;
				Renderer.this.descriptorPool = descriptorPool;
				
				Iterator<Pair<VulkanBuffer, BufferLayout>> i = uniformBuffers.iterator();
				Iterator<Pair<VulkanBuffer, BufferLayout>> j = Renderer.this.uniformBuffers.iterator();
				
				for (Pair<VulkanBuffer, BufferLayout> u0, u1; i.hasNext();)
				{
					u0 = i.next();
					u1 = j.next();
					u0.left.asByteBuffer().put(u1.left.asByteBuffer());
					u0.right.updateDescriptorSets();
				}
				Renderer.this.uniformBuffers = uniformBuffers;
			}
			
			Renderer.this.graphicsPipeline.getDevice().waitAsyncIdle(Renderer.this.graphicsPipeline::close);
			graphicsPipeline.set(createGraphicsPipeline(size.require(), builderFunction));
			
			Renderer.this.device = device.get();
			Renderer.this.graphicsPipeline = graphicsPipeline.get();
			
			isActive = false;
			stack.pop();
		}
	}
	
	public static class Builder extends RendererDataAccumulator<Builder> {
		
		private Builder()
		{}
		
		public Renderer build(Device device, Vector2i outputSize, RenderPass renderPass, int uniformBufferCount)
		{
			return build(device, outputSize, renderPass, uniformBufferCount, GraphicsPipeline::new);
		}
		
		public Renderer build(Device device, Vector2i outputSize, RenderPass renderPass, int uniformBufferCount, GraphicsPipelineBuildFunction<?> builderFunction)
		{
			try
			{
				this.device.setNonNull(device);
				physicalDevice.set(device.getPhysicalDevice());
				size.setNonNull(outputSize);
				this.renderPass.setNonNull(renderPass);
				
				createUniformBuffers(uniformBufferCount);
				
				VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
				pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayouts));
				
				pipelineLayout.set(new PipelineLayout(device, pipelineLayoutInfo));
				
				graphicsPipeline.set(createGraphicsPipeline(outputSize, builderFunction));
				
				return new Renderer(this, device, graphicsPipeline.get(), descriptorSetLayouts, descriptorSets, descriptorPool, uniformBuffers);
			}
			finally
			{
				close();
			}
		}
	}
	
	private static class RendererDataAccumulator<RDA extends RendererDataAccumulator<RDA>> extends DataAccumulator<RDA> {
		
		protected long[] descriptorSetLayouts;
		protected long[][] descriptorSets;
		protected long descriptorPool;
		protected List<Pair<VulkanBuffer, BufferLayout>> uniformBuffers;
		
		protected RendererDataAccumulator()
		{}
		
		protected RendererDataAccumulator(DataAccumulator<?> defaultSource)
		{
			super(defaultSource);
		}
		
		@SuppressWarnings("null")
		protected void createUniformBuffers(int count)
		{
			Debug.debug("Creating uniform buffers");
			device.require();
			
			VkDescriptorPoolCreateInfo poolInfo = VulkanUtils.descriptorPoolCreateInfo(stack, VulkanUtils.descriptorPoolSize(stack, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, count));// static
			poolInfo.maxSets(count);
			
			long[] p0 = new long[1];
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorPool(device.get().getVk(), poolInfo, null, p0), "Failed to create descriptor pool!");
			
			descriptorPool = p0[0];
			
			Map<Integer, ShaderLayoutBinding> bindingFlags = new HashMap<>();
			
			for (PreparedShader prepared : shaderProgram.require().getshaders()) ShaderUtils.extractData(prepared, bindingFlags);
			
			boolean useUniform = false;
			boolean useStorage = false;
			
			VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = VkDescriptorSetLayoutBinding.malloc(bindingFlags.size(), stack);// static
			for (Entry<Integer, ShaderLayoutBinding> e : bindingFlags.entrySet())
			{
				ShaderLayout.Type type = e.getValue().getDescriptorType();
				VkDescriptorSetLayoutBinding descriptorSetLayoutBinding = descriptorSetLayoutBindings.get();
				descriptorSetLayoutBinding.binding(e.getKey());
				descriptorSetLayoutBinding.descriptorCount(1);
				descriptorSetLayoutBinding.descriptorType(type.getType());
				descriptorSetLayoutBinding.pImmutableSamplers(null);
				descriptorSetLayoutBinding.stageFlags(e.getValue().getStages());
				
				switch (type)
				{
					case UNIFORM_BUFFER, UNIFORM_BUFFER_DYNAMIC, INLINE_UNIFORM_BLOCK -> useUniform = true;
					case STORAGE_BUFFER, STORAGE_BUFFER_DYNAMIC -> useStorage = true;
				}
			}
			descriptorSetLayoutBindings.rewind();
			
			LongBuffer p = stack.callocLong(count);
			
			descriptorSetLayouts = new long[count];
			for (int i = 0; i < count; i++)
			{
				VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorSetLayout(device.get().getVk(), VulkanUtils.descriptorSetLayoutInfo(stack, 0, descriptorSetLayoutBindings), null, p), "Failed to create descriptor set layout!");
				descriptorSetLayouts[i] = p.get();
			}
			p.rewind();
			
			long[] p1 = new long[count];
			
			descriptorSets = new long[count][1];
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.get().getVk(), VulkanUtils.descriptorSetAllocateInfo(stack, descriptorPool, p), p1), "Failed to allocate descriptor sets!");
			
			VulkanBuffer uniformBuffer = null;
			VulkanBuffer storageBuffer = null;
			if (useUniform)
			{
				uniformBuffers = new ArrayList<>(count);
				uniformBuffer = this.uniformBuffer.require("Shaders require uniforms so a uniform buffer is required");
			}
			else uniformBuffers = List.of();
			if (useStorage)
			{
				storageBuffer = this.storageBuffer.require("Shaders require storages so a storage buffer is required");
			}
			
			int minOffsetAlignment = (int) physicalDevice.get().getLimits().minUniformBufferOffsetAlignment();
			
			for (int i = 0; i < count; i++)
			{
				descriptorSets[i] = new long[] {p1[i]};
				Lock lock = new ReentrantLock();
				VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.malloc(bindingFlags.size());
				BufferLayout uniformBufferLayout = new BufferLayout(device.get(), writeDescriptorSets, 0, lock);
				BufferLayout storageBufferLayout = new BufferLayout(device.get(), writeDescriptorSets, 0, lock);
				if (useUniform) uniformBuffers.add(new Pair<>(uniformBuffer, uniformBufferLayout));
				
				int offset = 0;
				int partialOffset = 0;
				for (Entry<Integer, ShaderLayoutBinding> e : bindingFlags.entrySet())
				{
					if (offset % minOffsetAlignment != 0)
					{
						offset += minOffsetAlignment - offset % minOffsetAlignment;
					}
					VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
					writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
					writeDescriptorSet.pNext(0);
					writeDescriptorSet.dstSet(p1[i]);
					writeDescriptorSet.dstBinding(e.getKey());
					writeDescriptorSet.dstArrayElement(0);
					writeDescriptorSet.descriptorCount(1);
					writeDescriptorSet.descriptorType(e.getValue().getDescriptorType().getType());
					
					Set<ShaderVariable> variables = e.getValue().getLayoutsVariables();
					switch (e.getValue().getDescriptorType())
					{
						case UNIFORM_BUFFER -> {
							VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
							descriptorBufferInfo.buffer(uniformBuffer.getVk());
							descriptorBufferInfo.offset(offset);
							for (ShaderVariable variable : variables)
							{
								int size = variable.getType().getLength();
								if (partialOffset > 0 && partialOffset + size > 16) offset += 16 - partialOffset;
								uniformBufferLayout.mark(variable, offset / 4);
								offset += size;
								partialOffset = offset % 16;
							}
							descriptorBufferInfo.range(offset - descriptorBufferInfo.offset());
							writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
							writeDescriptorSet.pImageInfo(null);
							writeDescriptorSet.pTexelBufferView(null);
						}
						case STORAGE_BUFFER -> {
							VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
							descriptorBufferInfo.buffer(storageBuffer.getVk());
							descriptorBufferInfo.offset(offset);
							for (ShaderVariable variable : variables)
							{
								storageBufferLayout.mark(variable, offset / 4);
								offset += variable.getType().getLength();
							}
							descriptorBufferInfo.range(offset - descriptorBufferInfo.offset());
							writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
							writeDescriptorSet.pImageInfo(null);
							writeDescriptorSet.pTexelBufferView(null);
						}
						case COMBINED_IMAGE_SAMPLER -> {
							VkDescriptorImageInfo.Buffer descriptorImageInfos = VkDescriptorImageInfo.calloc(variables.size());
							for (ShaderVariable variable : variables)
							{
								uniformBufferLayout.mark(variable, writeDescriptorSets.position() - 1);
								VkDescriptorImageInfo descriptorImageInfo = descriptorImageInfos.get();
								descriptorImageInfo.imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
								descriptorImageInfo.imageView(0);
								descriptorImageInfo.sampler(0);
							}
							descriptorImageInfos.rewind();
							writeDescriptorSet.pBufferInfo(null);
							writeDescriptorSet.pImageInfo(descriptorImageInfos);
							writeDescriptorSet.pTexelBufferView(null);
						}
					}
				}
				writeDescriptorSets.rewind();
			}
		}
		
		@Override
		public RDA uniformBuffer(@Nullable VulkanBuffer value)
		{
			return super.uniformBuffer(value);
		}
		
		@Override
		public RDA storageBuffer(VulkanBuffer value)
		{
			return super.storageBuffer(value);
		}
		
		@Override
		public RDA shaderProgram(ShaderProgram value)
		{
			return super.shaderProgram(value);
		}
		
		@Override
		public RDA alphaToCoverageEnable(@Nullable Boolean value)
		{
			return super.alphaToCoverageEnable(value);
		}
		
		@Override
		public RDA alphaToOneEnable(@Nullable Boolean value)
		{
			return super.alphaToOneEnable(value);
		}
		
		@Override
		public RDA cullMode(@Nullable Integer value)
		{
			return super.cullMode(value);
		}
		
		@Override
		public RDA depthBiasClamp(@Nullable Float value)
		{
			return super.depthBiasClamp(value);
		}
		
		@Override
		public RDA depthBiasConstantFactor(@Nullable Float value)
		{
			return super.depthBiasConstantFactor(value);
		}
		
		@Override
		public RDA depthBiasEnable(@Nullable Boolean value)
		{
			return super.depthBiasEnable(value);
		}
		
		@Override
		public RDA depthBiasSlopeFactor(@Nullable Float value)
		{
			return super.depthBiasSlopeFactor(value);
		}
		
		@Override
		public RDA depthClampEnable(@Nullable Boolean value)
		{
			return super.depthClampEnable(value);
		}
		
		@Override
		public RDA frontFace(@Nullable Integer value)
		{
			return super.frontFace(value);
		}
		
		@Override
		public RDA lineWidth(@Nullable Float value)
		{
			return super.lineWidth(value);
		}
		
		@Override
		public RDA logicOp(@Nullable Integer value)
		{
			return super.logicOp(value);
		}
		
		@Override
		public RDA logicOpEnable(@Nullable Boolean value)
		{
			return super.logicOpEnable(value);
		}
		
		@Override
		public RDA minSampleShading(@Nullable Float value)
		{
			return super.minSampleShading(value);
		}
		
		@Override
		public RDA polygonMode(@Nullable Integer value)
		{
			return super.polygonMode(value);
		}
		
		@Override
		public RDA primitiveRestartEnable(@Nullable Boolean value)
		{
			return super.primitiveRestartEnable(value);
		}
		
		@Override
		public RDA rasterizationSamples(@Nullable Integer value)
		{
			return super.rasterizationSamples(value);
		}
		
		@Override
		public RDA rasterizerDiscardEnable(@Nullable Boolean value)
		{
			return super.rasterizerDiscardEnable(value);
		}
		
		@Override
		public RDA sampleShadingEnable(@Nullable Boolean value)
		{
			return super.sampleShadingEnable(value);
		}
		
		@Override
		public RDA topology(@Nullable Integer value)
		{
			return super.topology(value);
		}
		
		@Override
		public <SSDA extends DataAccumulator<RDA>.PipelineColorBlendAttachmentStateAccumulator<SSDA>> SSDA startPipelineColorBlendAttachmentState()
		{
			return super.startPipelineColorBlendAttachmentState();
		}
		
		@Override
		public RDA depthTestEnable(@Nullable Boolean value)
		{
			return super.depthTestEnable(value);
		}
		
		@Override
		public RDA depthWriteEnable(@Nullable Boolean value)
		{
			return super.depthWriteEnable(value);
		}
		
		@Override
		public RDA depthCompareOp(@Nullable Integer value)
		{
			return super.depthCompareOp(value);
		}
		
		@Override
		public RDA minDepth(@Nullable Float value)
		{
			return super.minDepth(value);
		}
		
		@Override
		public RDA maxDepth(@Nullable Float value)
		{
			return super.maxDepth(value);
		}
		
		@Override
		public RDA dynamicViewport(Boolean value)
		{
			return super.dynamicViewport(value);
		}
		
		@Override
		public RDA dynamicScissor(Boolean value)
		{
			return super.dynamicScissor(value);
		}
		
		@Override
		public RDA size(Vector2i value)
		{
			return super.size(value);
		}
	}
}
