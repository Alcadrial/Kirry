package com.alcadrial.renderengine.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.collections.api.set.ImmutableSet;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceLimits;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.shader.ShaderLayoutBinding;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout.Type;
import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanFunction;

public class Renderer implements VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static final class Builder {
		
		private @Nullable GraphicsPipeline graphicsPipeline;
		private @Nullable VulkanFunction<Integer, BufferWriter[]> writers;
		private DescriptorSet @Nullable [] descriptorSets;
		private int buffersCount;
		private ShaderLayoutBinding @Nullable [] bindings;
		
		private Builder()
		{
			buffersCount = 1;
		}
		
		public Builder graphicsPipeline(GraphicsPipeline graphicsPipeline)
		{
			this.graphicsPipeline = graphicsPipeline;
			return this;
		}
		
		public Builder writers(VulkanFunction<Integer, BufferWriter[]> writers)
		{
			this.writers = writers;
			return this;
		}
		
		public Builder bindingsLayout(ShaderLayoutBinding... bindings)
		{
			this.bindings = bindings;
			return this;
		}
		
		public Builder buffersCount(int buffersCount)
		{
			if (buffersCount < 0) throw new IllegalArgumentException();
			this.buffersCount = buffersCount;
			return this;
		}
		
		public Builder descriptorSets(DescriptorSet... descriptorSets)
		{
			this.descriptorSets = descriptorSets;
			return this;
		}
		
		public Renderer build() throws VulkanException
		{
			return new Renderer(this);
		}
	}
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	private Device device;
	private GraphicsPipeline graphicsPipeline;
	private VulkanBuffer[] buffers;
	private BufferLayout[][] layouts;
	private @Nullable VulkanBuffer indexBuffer;
	private DescriptorSet[] descriptorSets;
	
	private Renderer(Builder builder) throws VulkanException
	{
		this.graphicsPipeline = VulkanErrorUtils.nonNull(builder.graphicsPipeline);
		this.device = graphicsPipeline.getDevice();
		this.descriptorSets = VulkanErrorUtils.nonNull(builder.descriptorSets);
		
		ImmutableSet<Integer> activeBindings = graphicsPipeline.getActiveBindings();
		buffers = new VulkanBuffer[activeBindings.isEmpty() ? 0 : activeBindings.max() + 1];
		
		if (builder.buffersCount < 0) throw new IllegalStateException();
		BufferWriter[][] writers = new BufferWriter[builder.buffersCount][];
		VulkanFunction<Integer, BufferWriter[]> writersFactory = VulkanErrorUtils.nonNull(builder.writers);
		for (int i = 0; i < writers.length; i++) writers[i] = writersFactory.apply(i);
		
		layouts = initLayouts(writers, VulkanErrorUtils.nonNull(builder.bindings));
	}
	
	private BufferLayout[][] initLayouts(BufferWriter[][] writers, ShaderLayoutBinding[] bindingLayouts) throws VulkanException
	{
		VkPhysicalDeviceLimits limits = device.getPhysicalDevice().getLimits();
		int minStorageOffsetAlignment = (int) limits.minStorageBufferOffsetAlignment();
		int minTexelBufferOffsetAlignment = (int) limits.minTexelBufferOffsetAlignment();
		int minUniformOffsetAlignment = (int) limits.minUniformBufferOffsetAlignment();
		
		BufferLayout[][] layouts = new BufferLayout[writers.length][];
		long[] set = new long[1];
		for (int i = 0; i < writers.length; i++)
		{
			Map<BufferWriter, int[]> perBufferOffset = new HashMap<>();
			VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.malloc(bindingLayouts.length);
			
			BufferLayout[] layouts0 = layouts[i] = new BufferLayout[writers[i].length];
			for (int j = 0; j < writers[i].length; j++)
			{
				perBufferOffset.put(writers[i][j], new int[] {0, 0});
				layouts0[j] = BufferLayout
					.of(device)
					.writeDescriptorSets(writeDescriptorSets)
					.writer(writers[i][j])
					.build(new ReentrantLock());
			}
			set[0] = descriptorSets[i].getVk();
			
			for (int binding = 0; binding < bindingLayouts.length; binding++)
			{
				ShaderLayoutBinding layoutBinding = bindingLayouts[binding];
				Type descriptorType = layoutBinding.getDescriptorType();
				List<ShaderVariable> variables = layoutBinding.getLayoutsVariables();
				
				switch (descriptorType)
				{
					case UNIFORM_BUFFER, STORAGE_BUFFER -> {
						BufferLayout layout = layouts0[binding];
						if (layout == null) throw new VulkanException("Missing BufferWriter for binding " + binding);
						BufferWriter writer = layout.getWriter();
						int[] offsets = perBufferOffset.get(writer);
						Debug.debug("[Set 0, Binding " + binding + ", Index 0] " + variables);
						
						VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
						writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
						writeDescriptorSet.pNext(0);
						writeDescriptorSet.dstSet(set[0]);
						writeDescriptorSet.dstBinding(binding);
						writeDescriptorSet.dstArrayElement(0);
						writeDescriptorSet.descriptorCount(1);
						writeDescriptorSet.descriptorType(descriptorType.getType());
						
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
						BufferLayout layout = layouts0[binding];
						if (layout == null) throw new VulkanException("Missing BufferWriter for binding " + binding);
						Debug.debug("[Set 0, Binding " + binding + ", Index 0] " + variables);
						
						VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
						writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
						writeDescriptorSet.pNext(0);
						writeDescriptorSet.dstSet(set[0]);
						writeDescriptorSet.dstBinding(binding);
						writeDescriptorSet.dstArrayElement(0);
						writeDescriptorSet.descriptorCount(1);
						writeDescriptorSet.descriptorType(descriptorType.getType());
						
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
		return layouts;
	}
	
	public BufferLayout[] getBufferLayout(int index)
	{
		return layouts[index];
	}
	
	public void draw(CommandBuffer commandBuffer, int frameIndex) throws VulkanException
	{
		draw(commandBuffer, frameIndex, 1);
	}
	
	public void draw(CommandBuffer commandBuffer, int frameIndex, int instanceCount) throws VulkanException
	{
		draw(commandBuffer, frameIndex, instanceCount, indexBuffer != null ? indexBuffer.getSize() / 4 : 0);
	}
	
	public void draw(CommandBuffer commandBuffer, int frameIndex, int instanceCount, int count) throws VulkanException
	{
		if (count == 0) return;
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] set = new long[] {descriptorSets[frameIndex].getVk()};
			commandBuffer.bindPipeline(graphicsPipeline);
			commandBuffer.bindVertexBuffers(0, buffers);
			commandBuffer.bindDescriptorSets(graphicsPipeline, set, null);
			if (indexBuffer != null)
			{
				commandBuffer.bindIndexBuffer(indexBuffer, 0, VK10.VK_INDEX_TYPE_UINT32);
				commandBuffer.drawIndexed(count, instanceCount, 0, 0, 0);
			}
			else
			{
				commandBuffer.draw(count, instanceCount, 0, 0);
			}
		}
	}
	
	public void setBuffer(int binding, VulkanBuffer buffer)
	{
		if (graphicsPipeline.getActiveBindings().contains(binding)) buffers[binding] = buffer;
	}
	
	public void setIndexBuffer(VulkanBuffer indexBuffer)
	{
		this.indexBuffer = indexBuffer;
	}
	
	@Override
	public void debug()
	{
		Debug.print("Device", device);
		Debug.print("Pipeline ", graphicsPipeline);
		Debug.print("VertexBuffers [");
		Debug.indent();
		for (Integer binding : graphicsPipeline.getActiveBindings())
		{
			Debug.print("Binding " + binding, buffers[binding]);
		}
		Debug.deindent();
		Debug.print("]");
		Debug.print("DescriptorSets: ");
		Debug.indent();
		for (DescriptorSet descriptorSet : descriptorSets)
		{
			Debug.print("Set ", descriptorSet);
		}
		Debug.deindent();
	}
}
