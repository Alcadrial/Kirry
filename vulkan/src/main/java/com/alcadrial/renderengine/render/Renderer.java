package com.alcadrial.renderengine.render;

import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkPhysicalDeviceLimits;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
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
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout.Type;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.texture.TextureView;
import com.alcadrial.renderengine.render.util.DataAccumulator;
import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanConsumer;

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
	private Map<Integer, List<VulkanBuffer>> vertexBuffers;
	private VulkanBuffer indexBuffer;
	private long descriptorSetLayout;
	private long[] descriptorSet;
	private long descriptorPool;
	private Map<String, Entry<BufferLayout, UniformLayout>> uniforms;
	private List<VulkanConsumer<CommandBuffer>> extraCommands;
	
	private Renderer(Builder builder, Device device, GraphicsPipeline graphicsPipeline, long descriptorSetLayout, long[] descriptorSet, long descriptorPool, Map<String, BufferLayout> uniforms)
	{
		modificationPlan = new ModificationPlan(builder);
		this.device = device;
		this.graphicsPipeline = graphicsPipeline;
		this.descriptorSetLayout = descriptorSetLayout;
		this.descriptorSet = descriptorSet;
		this.descriptorPool = descriptorPool;
		this.uniforms = new HashMap<>(uniforms.size());
		for (Entry<String, BufferLayout> e : uniforms.entrySet())
		{
			this.uniforms.put(e.getKey(), Map.entry(e.getValue(), new UniformLayout(e.getKey(), e.getValue())));
		}
		
		vertexBuffers = new HashMap<>();
		extraCommands = new LinkedList<>();
	}
	
	public void setName(String name)
	{
		graphicsPipeline.setName("Graphics Pipeline (" + name + ")");
		for (int i = 0; i < descriptorSet.length; i++) device.setObjectDebugName(VK10.VK_OBJECT_TYPE_DESCRIPTOR_SET, descriptorSet[i], "Descriptor Set " + i + " (" + name + ")");
		device.setObjectDebugName(VK10.VK_OBJECT_TYPE_DESCRIPTOR_SET_LAYOUT, descriptorSetLayout, "Descriptor Set Layout (" + name + ")");
		device.setObjectDebugName(VK10.VK_OBJECT_TYPE_DESCRIPTOR_POOL, descriptorPool, "Descriptor Pool (" + name + ")");
	}
	
	public void updateUniform(String uniform, VulkanConsumer<UniformLayout> action) throws VulkanException
	{
		Entry<BufferLayout, UniformLayout> e = uniforms.get(uniform);
		if (e != null) action.accept(e.getValue());
	}
	
	public void updateUniform(String uniform, VulkanConsumer<UniformLayout> action, CommandBuffer target) throws VulkanException
	{
		Entry<BufferLayout, UniformLayout> e = uniforms.get(uniform);
		if (e != null)
		{
			action.accept(e.getValue());
			e.getKey().flush(target);
		}
	}
	
	public void addExtraCommand(VulkanConsumer<CommandBuffer> command)
	{
		extraCommands.add(command);
	}
	
	public void draw(CommandBuffer commandBuffer) throws VulkanException
	{
		draw(commandBuffer, 1);
	}
	
	public void draw(CommandBuffer commandBuffer, int instanceCount) throws VulkanException
	{
		draw(commandBuffer, instanceCount, indexBuffer != null ? (int) (indexBuffer.getSize() / 4) : 0);
	}
	
	public void draw(CommandBuffer commandBuffer, int instanceCount, int count) throws VulkanException
	{
		if (count == 0) throw new IllegalStateException("Cannot draw 0 vertices");
		
		for (Entry<BufferLayout, UniformLayout> e : uniforms.values()) e.getKey().updateDescriptorSets();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			commandBuffer.bindPipeline(graphicsPipeline);
			for (Entry<Integer, List<VulkanBuffer>> p : vertexBuffers.entrySet()) commandBuffer.bindVertexBuffers(p.getKey(), p.getValue());
			commandBuffer.bindDescriptorSets(graphicsPipeline, descriptorSet, new int[0]);
			if (indexBuffer != null)
			{
				commandBuffer.bindIndexBuffer(indexBuffer, 0, VK10.VK_INDEX_TYPE_UINT32);
				commandBuffer.drawIndexed(count, instanceCount, 0, 0, 0);
			}
			else
			{
				commandBuffer.draw(count, instanceCount, 0, 0);
			}
			
			for (VulkanConsumer<CommandBuffer> command : extraCommands) command.accept(commandBuffer);
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
	
	public void setVertexBuffers(int binding, VulkanBuffer... vertexBuffer)
	{
		vertexBuffers.put(binding, Arrays.asList(vertexBuffer));
	}
	
	public void setIndexBuffer(VulkanBuffer indexBuffer)
	{
		this.indexBuffer = indexBuffer;
	}
	
	@Override
	public void close()
	{
		device.waitIdle().join();
		graphicsPipeline.close();
		VK10.vkDestroyDescriptorSetLayout(device.getVk(), descriptorSetLayout, null);
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
		for (Entry<Integer, List<VulkanBuffer>> e : vertexBuffers.entrySet())
		{
			Debug.print("Binding " + e.getKey() + " [");
			Debug.indent();
			for (VulkanBuffer vertexBuffer : e.getValue())
			{
				Debug.print("Buffer", vertexBuffer);
			}
			Debug.deindent();
			Debug.print("]");
		}
		Debug.deindent();
		Debug.print("]");
		Debug.print("DescriptorSetLayout: " + Debug.address(descriptorSetLayout));
		Debug.print("DescriptorSet: " + Debug.address(descriptorSet[0]));
		Debug.print("Other Buffers [");
		Debug.indent();
		for (Entry<BufferLayout, UniformLayout> e : uniforms.values())
		{
			Debug.print("Buffer ", e.getKey().getWriter().getDstBuffer());
		}
		Debug.deindent();
		Debug.print("]");
	}
	
	public class UniformLayout {
		
		private String uniform;
		private BufferLayout layout;
		
		private UniformLayout(String uniform, BufferLayout layout)
		{
			this.uniform = uniform;
			this.layout = layout;
		}
		
		public void set(Matrix4f value) throws VulkanException
		{
			set(0, value);
		}
		
		public void set(int index, Matrix4f value) throws VulkanException
		{
			layout.set(uniform, index, value);
		}
		
		public void set(Vector2f value) throws VulkanException
		{
			set(0, value);
		}
		
		public void set(int index, Vector2f value) throws VulkanException
		{
			layout.set(uniform, index, value);
		}
		
		public void set(Vector3f value) throws VulkanException
		{
			set(0, value);
		}
		
		public void set(int index, Vector3f value) throws VulkanException
		{
			layout.set(uniform, index, value);
		}
		
		public void set(Vector4f value) throws VulkanException
		{
			set(0, value);
		}
		
		public void set(int index, Vector4f value) throws VulkanException
		{
			layout.set(uniform, index, value);
		}
		
		public void set(Vector2i value) throws VulkanException
		{
			set(0, value);
		}
		
		public void set(int index, Vector2i value) throws VulkanException
		{
			layout.set(uniform, index, value);
		}
		
		public void set(Vector3i value) throws VulkanException
		{
			set(0, value);
		}
		
		public void set(int index, Vector3i value) throws VulkanException
		{
			layout.set(uniform, index, value);
		}
		
		public void set(Vector4i value) throws VulkanException
		{
			set(0, value);
		}
		
		public void set(int index, Vector4i value) throws VulkanException
		{
			layout.set(uniform, index, value);
		}
		
		public void set(TextureView value, TextureSampler sampler)
		{
			layout.set(uniform, value, sampler);
		}
		
		public void set(int value) throws VulkanException
		{
			set(0, value);
		}
		
		public void set(int index, int value) throws VulkanException
		{
			layout.set(uniform, index, value);
		}
		
		public void set(float value) throws VulkanException
		{
			set(0, value);
		}
		
		public void set(int index, float value) throws VulkanException
		{
			layout.set(uniform, index, value);
		}
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
		
		public void submit() throws VulkanException
		{
			submit(GraphicsPipeline::new);
		}
		
		public void submit(GraphicsPipelineBuildFunction<?> builderFunction) throws VulkanException
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
		
		private void submit0(GraphicsPipelineBuildFunction<?> builderFunction) throws VulkanException
		{
			boolean deviceModified = device.isModified();
			
			Device device0 = device.require();
			physicalDevice.set(device0.getPhysicalDevice());
			
			if (deviceModified || shaderProgram.isModified())
			{
				createDescriptorSet();
				
				VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
				pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayout));
				
				pipelineLayout.set(new PipelineLayout(device0, pipelineLayoutInfo));
				
				long descriptorSetLayouts0 = Renderer.this.descriptorSetLayout;
				long descriptorPool0 = Renderer.this.descriptorPool;
				
				device0.waitIdle().thenRun(() -> {
					VK10.vkDestroyDescriptorSetLayout(device0.getVk(), descriptorSetLayouts0, null);
					VK10.vkDestroyDescriptorPool(device0.getVk(), descriptorPool0, null);
				});
				
				Renderer.this.descriptorSetLayout = descriptorSetLayout;
				Renderer.this.descriptorSet = descriptorSet;
				Renderer.this.descriptorPool = descriptorPool;
				Map<String, Entry<BufferLayout, UniformLayout>> newUniforms = new HashMap<>(uniforms.size());
				for (Entry<String, BufferLayout> e : uniforms.entrySet())
				{
					newUniforms.put(e.getKey(), Map.entry(e.getValue(), new UniformLayout(e.getKey(), e.getValue())));
				}
				Renderer.this.uniforms = newUniforms;
			}
			
			Renderer.this.graphicsPipeline.getDevice().waitIdle().thenRun(Renderer.this.graphicsPipeline::close);
			graphicsPipeline.set(createGraphicsPipeline(size.require(), builderFunction));
			
			Renderer.this.device = device0;
			Renderer.this.graphicsPipeline = graphicsPipeline.get();
			
			isActive = false;
			stack.pop();
		}
	}
	
	public static class Builder extends RendererDataAccumulator<Builder> {
		
		private Builder()
		{}
		
		public Renderer build(Device device, Vector2i outputSize, RenderPass renderPass) throws VulkanException
		{
			return build(device, outputSize, renderPass, GraphicsPipeline::new);
		}
		
		public Renderer build(Device device, Vector2i outputSize, RenderPass renderPass, GraphicsPipelineBuildFunction<?> builderFunction) throws VulkanException
		{
			this.device.setNonNull(device);
			physicalDevice.set(device.getPhysicalDevice());
			size.setNonNull(outputSize);
			this.renderPass.setNonNull(renderPass);
			
			createDescriptorSet();
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayout));
			
			pipelineLayout.set(new PipelineLayout(device, pipelineLayoutInfo));
			
			graphicsPipeline.set(createGraphicsPipeline(outputSize, builderFunction));
			
			return new Renderer(this, device, graphicsPipeline.get(), descriptorSetLayout, descriptorSet, descriptorPool, uniforms);
		}
	}
	
	private static class RendererDataAccumulator<RDA extends RendererDataAccumulator<RDA>> extends DataAccumulator<RDA> {
		
		protected long descriptorSetLayout;
		protected long[] descriptorSet;
		protected long descriptorPool;
		protected Map<String, BufferLayout> uniforms;
		
		protected RendererDataAccumulator()
		{}
		
		protected RendererDataAccumulator(DataAccumulator<?> defaultSource)
		{
			super(defaultSource);
		}
		
		protected void createDescriptorSet() throws VulkanException
		{
			device.require();
			
			Map<Integer, ShaderLayoutBinding> bindingFlags = new HashMap<>();
			
			for (PreparedShader prepared : shaderProgram.require().getshaders()) ShaderUtils.extractData(prepared, bindingFlags);
			
			List<BufferWriter> bufferBindings = this.bufferBindings.orElse(List.of());
			
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
				
				if (bufferBindings.size() <= e.getKey() || bufferBindings.get(e.getKey()) == null) addError("Missing buffer for binding " + e.getKey());
			}
			descriptorSetLayoutBindings.rewind();
			
			checkErrors();
			
			VkDescriptorPoolCreateInfo poolInfo = VulkanUtils.descriptorPoolCreateInfo(stack, VulkanUtils.descriptorPoolSize(stack, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1));
			poolInfo.maxSets(1);
			
			long[] p0 = new long[1];
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorPool(device.get().getVk(), poolInfo, null, p0), "Failed to create descriptor pool!");
			
			descriptorPool = p0[0];//add to DataAccumulator
			
			LongBuffer p = stack.callocLong(1);
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorSetLayout(device.get().getVk(), VulkanUtils.descriptorSetLayoutInfo(stack, 0, descriptorSetLayoutBindings), null, p), "Failed to create descriptor set layout!");
			descriptorSetLayout = p.get();
			
			p.rewind();
			
			descriptorSet = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.get().getVk(), VulkanUtils.descriptorSetAllocateInfo(stack, descriptorPool, p), descriptorSet), "Failed to allocate descriptor sets!");
			
			uniforms = new HashMap<>();
			
			VkPhysicalDeviceLimits limits = physicalDevice.get().getLimits();
			int minStorageOffsetAlignment = (int) limits.minStorageBufferOffsetAlignment();
			int minTexelBufferOffsetAlignment = (int) limits.minTexelBufferOffsetAlignment();
			int minUniformOffsetAlignment = (int) limits.minUniformBufferOffsetAlignment();
			
			Lock lock = new ReentrantLock();
			VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.malloc(bindingFlags.size());
			
			Map<BufferWriter, int[]> perBufferOffset = new HashMap<>();
			for (BufferWriter writer : bufferBindings) if (writer != null) perBufferOffset.put(writer, new int[] {0, 0});
			
			for (Entry<Integer, ShaderLayoutBinding> e : bindingFlags.entrySet())
			{
				ShaderLayoutBinding binding = e.getValue();
				Type descriptorType = binding.getDescriptorType();
				
				VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
				writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
				writeDescriptorSet.pNext(0);
				writeDescriptorSet.dstSet(descriptorSet[0]);
				writeDescriptorSet.dstBinding(e.getKey());
				writeDescriptorSet.dstArrayElement(0);
				writeDescriptorSet.descriptorCount(1);
				writeDescriptorSet.descriptorType(descriptorType.getType());
				
				BufferWriter writer = bufferBindings.get(e.getKey());
				int[] offsets = perBufferOffset.get(writer);
				
				BufferLayout layout = new BufferLayout(device.get(), lock);
				layout.setWriteDescriptorSets(writeDescriptorSets);
				layout.setWriter(writer);
				
				Set<ShaderVariable> variables = binding.getLayoutsVariables();
				for (ShaderVariable variable : variables) uniforms.putIfAbsent(variable.getName(), layout);
				
				switch (descriptorType)
				{
					case UNIFORM_BUFFER, STORAGE_BUFFER -> {
						
						switch (descriptorType)
						{
							case UNIFORM_BUFFER -> {
								if (offsets[0] % minUniformOffsetAlignment != 0) offsets[0] += minUniformOffsetAlignment - offsets[0] % minUniformOffsetAlignment;
							}
							case STORAGE_BUFFER -> {
								if (offsets[0] % minStorageOffsetAlignment != 0) offsets[0] += minStorageOffsetAlignment - offsets[0] % minStorageOffsetAlignment;
							}
							case UNIFORM_TEXEL_BUFFER, STORAGE_TEXEL_BUFFER -> {
								if (offsets[0] % minTexelBufferOffsetAlignment != 0) offsets[0] += minTexelBufferOffsetAlignment - offsets[0] % minTexelBufferOffsetAlignment;
							}
						}
						
						VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
						descriptorBufferInfo.buffer(writer.getDstBuffer().getVk());
						descriptorBufferInfo.offset(offsets[0] + writer.getDstOffset());
						for (ShaderVariable variable : variables)
						{
							int size = variable.getType().getLength();
							if (offsets[1] > 0 && offsets[1] + size > 16) offsets[0] += 16 - offsets[1];
							layout.mark(variable, offsets[0] / 4);
							offsets[0] += size;
							offsets[1] = offsets[0] % 16;
						}
						descriptorBufferInfo.range(writer.getSize());
						writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
						writeDescriptorSet.pImageInfo(null);
						writeDescriptorSet.pTexelBufferView(null);
					}
					case COMBINED_IMAGE_SAMPLER -> {
						VkDescriptorImageInfo.Buffer descriptorImageInfos = VkDescriptorImageInfo.calloc(variables.size());
						for (ShaderVariable variable : variables)
						{
							layout.mark(variable, writeDescriptorSets.position() - 1);
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
					default -> {
						Debug.debug("WARNING: Unsupported descriptor type " + descriptorType);
					}
				}
			}
			writeDescriptorSets.rewind();
		}
		
		@Override
		public RDA bufferBindings(@Nullable BufferWriter... value)
		{
			return super.bufferBindings(value);
		}
		
		@Override
		public RDA bufferBindings(@Nullable List<BufferWriter> value)
		{
			return super.bufferBindings(value);
		}
		
		@Override
		public RDA shaderProgram(@Nullable ShaderProgram value)
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
		public RDA dynamicViewport(@Nullable Boolean value)
		{
			return super.dynamicViewport(value);
		}
		
		@Override
		public RDA dynamicScissor(@Nullable Boolean value)
		{
			return super.dynamicScissor(value);
		}
		
		@Override
		public RDA size(@Nullable Vector2i value)
		{
			return super.size(value);
		}
	}
}
