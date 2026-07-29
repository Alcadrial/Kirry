package com.alcadrial.kirry.render;

import java.util.Arrays;
import java.util.List;

import org.eclipse.collections.api.set.ImmutableSet;
import org.jspecify.annotations.Nullable;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import org.lwjgl.vulkan.VkWriteDescriptorSet.Buffer;

import com.alcadrial.kirry.render.buffer.BufferLayout;
import com.alcadrial.kirry.render.buffer.BufferLayoutWriter;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.pipeline.GraphicsPipeline;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.shader.binding.BindingInfo;
import com.alcadrial.kirry.render.shader.binding.BufferBindingInfo;
import com.alcadrial.kirry.render.shader.binding.ImageBindingInfo;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSet;
import com.alcadrial.kirry.render.shader.layout.ShaderLayout.Type;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.texture.TextureSampler;
import com.alcadrial.kirry.render.util.VulkanDebug;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.functional.VulkanConsumer;
import com.alcadrial.kirry.render.util.functional.VulkanFunction;

public class Renderer implements VulkanDebug {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public static final class Builder {
		
		private @Nullable GraphicsPipeline graphicsPipeline;
		private @Nullable VulkanFunction<Integer, BindingInfo[]> bindings;
		private DescriptorSet @Nullable [] descriptorSets;
		private int buffersCount;
		
		private Builder()
		{
			buffersCount = 1;
		}
		
		public Builder graphicsPipeline(GraphicsPipeline graphicsPipeline)
		{
			this.graphicsPipeline = graphicsPipeline;
			return this;
		}
		
		public Builder bindings(BindingInfo... bindings)
		{
			return bindings(_ -> bindings);
		}
		
		public Builder bindings(BindingInfo[][] bindings)
		{
			return bindings(i -> bindings[i]);
		}
		
		public Builder bindings(VulkanFunction<Integer, BindingInfo[]> bindings)
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
	
	private static class InfoHolder {
		
		@Nullable
		BufferBindingInfo bufferBindingInfo;
		@Nullable
		ImageBindingInfo imageBindingInfo;
		@Nullable
		BufferLayout layout;
		
		public InfoHolder(BufferBindingInfo bufferBindingInfo)
		{
			this.bufferBindingInfo = bufferBindingInfo;
		}
		
		public InfoHolder(ImageBindingInfo imageBindingInfo)
		{
			this.imageBindingInfo = imageBindingInfo;
		}
	}
	
	private class DescriptorsData {
		
		private InfoHolder[][] info;
		private VkWriteDescriptorSet.Buffer[] writeDescriptorSets;
		private @Nullable BufferLayoutWriter[][] writers;
		
		public DescriptorsData(InfoHolder[][] info, Buffer[] writeDescriptorSets)
		{
			this.info = info;
			this.writeDescriptorSets = writeDescriptorSets;
			writers = new BufferLayoutWriter[info.length][];
			for (int i = 0; i < bindings.length; i++)
			{
				writers[i] = new BufferLayoutWriter[bindings[i].length];
			}
		}
	}
	
	private final BindingInfo[][] bindings;
	
	private Device device;
	private GraphicsPipeline graphicsPipeline;
	private VulkanBuffer[] bindingBuffers;
	private long[] bindingBuffersOffsets;
	private @Nullable DescriptorsData descriptorsData;
	private @Nullable VulkanBuffer indexBuffer;
	private long indexOffset;
	private DescriptorSet[] descriptorSets;
	
	private Renderer(Builder builder) throws VulkanException
	{
		graphicsPipeline = VulkanErrorUtils.nonNull(builder.graphicsPipeline);
		device = graphicsPipeline.getDevice();
		descriptorSets = VulkanErrorUtils.nonNull(builder.descriptorSets);
		
		ImmutableSet<Integer> activeBindings = graphicsPipeline.getActiveBindings();
		bindingBuffers = new VulkanBuffer[activeBindings.isEmpty() ? 0 : activeBindings.max() + 1];
		bindingBuffersOffsets = new long[bindingBuffers.length];
		
		bindings = new BindingInfo[builder.buffersCount][];
		VulkanFunction<Integer, BindingInfo[]> bindingsFactory = builder.bindings;
		
		if (bindingsFactory != null)
		{
			for (int i = 0; i < bindings.length; i++) bindings[i] = bindingsFactory.apply(i);
			descriptorsData = initInfo();
		}
	}
	
	private DescriptorsData initInfo() throws VulkanException
	{
		InfoHolder[][] info = new InfoHolder[bindings.length][];
		VkWriteDescriptorSet.Buffer[] writeDescriptorSetsResult = new VkWriteDescriptorSet.Buffer[bindings.length];
		for (int i = 0; i < bindings.length; i++)
		{
			BindingInfo[] bindings = this.bindings[i];
			
			VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.malloc(bindings.length);
			
			info[i] = new InfoHolder[bindings.length];
			long setVk = descriptorSets[i].getVk();
			
			for (int binding = 0; binding < bindings.length; binding++)
			{
				BindingInfo description = bindings[binding];
				Type descriptorType = description.getDescriptorType();
				List<ShaderVariable> variables = description.getVariables();
				
				InfoHolder writer = null;
				if (descriptorType.useBuffer()) writer = info[i][binding] = new InfoHolder((BufferBindingInfo) description);
				else if (descriptorType.useImage()) writer = info[i][binding] = new InfoHolder((ImageBindingInfo) description);
				else
				{
					Debug.debug("WARNING: Unsupported descriptor type " + descriptorType);
					continue;
				}
				
				VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
				writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
				writeDescriptorSet.pNext(0);
				writeDescriptorSet.dstSet(setVk);
				writeDescriptorSet.dstBinding(binding);
				writeDescriptorSet.dstArrayElement(0);
				writeDescriptorSet.descriptorType(descriptorType.getType());
				
				BufferBindingInfo bufferBindingInfo = writer.bufferBindingInfo;
				if (bufferBindingInfo != null)
				{
					switch (descriptorType)
					{
						case UNIFORM_BUFFER -> {
							VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.malloc(1);
							if (variables.isEmpty())
							{
								VulkanBuffer buffer = bufferBindingInfo.getBuffer();
								descriptorBufferInfo.buffer(buffer.getVk());
								descriptorBufferInfo.offset(buffer.getOffset());
								descriptorBufferInfo.range(Math.max(device.getPhysicalDevice().getLimits().minUniformBufferOffsetAlignment(), buffer.getSize()));
							}
							else
							{
								VulkanBuffer buffer = bufferBindingInfo.getBuffer();
								BufferLayout layout = new BufferLayout();
								
								int range = 0;
								int offset = 0;
								for (ShaderVariable variable : variables)
								{
									int size = variable.getType().getLength();
									if (offset > 0 && offset + size > 16) range += 16 - offset;
									layout.mark(variable, range / 4);
									range += size;
									offset = range % 16;
								}
								writer.layout = layout;
								descriptorBufferInfo.buffer(buffer.getVk());
								descriptorBufferInfo.offset(buffer.getOffset());
								descriptorBufferInfo.range(Math.min(range, buffer.getSize()));
							}
							writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
							writeDescriptorSet.pImageInfo(null);
							writeDescriptorSet.pTexelBufferView(null);
							writeDescriptorSet.descriptorCount(1);
							Debug.debug("[Set " + i + ", Binding " + binding + ", Index 0] " + variables);
						}
						case STORAGE_BUFFER -> {
							VulkanBuffer buffer = bufferBindingInfo.getBuffer();
							
							VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.malloc(1);
							descriptorBufferInfo.buffer(buffer.getVk());
							descriptorBufferInfo.offset(buffer.getOffset());
							descriptorBufferInfo.range(buffer.getSize());
							
							writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
							writeDescriptorSet.pImageInfo(null);
							writeDescriptorSet.pTexelBufferView(null);
							writeDescriptorSet.descriptorCount(1);
							Debug.debug("[Set " + i + ", Binding " + binding + ", Index 0] " + variables);
						}
						default -> {
							Debug.debug("WARNING: Unsupported descriptor type " + descriptorType);
						}
					}
				}
				ImageBindingInfo imageBindingInfo = writer.imageBindingInfo;
				if (imageBindingInfo != null)
				{
					List<Texture> texture = imageBindingInfo.getTextures();
					List<TextureSampler> sampler = imageBindingInfo.getSamplers();
					VkDescriptorImageInfo.Buffer descriptorImageInfos = VkDescriptorImageInfo.calloc(variables.size());
					for (int j = 0; j < variables.size(); j++)
					{
						VkDescriptorImageInfo descriptorImageInfo = descriptorImageInfos.get();
						descriptorImageInfo.imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
						descriptorImageInfo.imageView(texture.get(j).getView().getVk());
						descriptorImageInfo.sampler(sampler.get(j).getVk());
						Debug.debug("[Set " + i + ", Binding " + binding + ", Index " + j + "] " + variables.get(j));
					}
					writeDescriptorSet.pBufferInfo(null);
					writeDescriptorSet.pImageInfo(descriptorImageInfos.rewind());
					writeDescriptorSet.pTexelBufferView(null);
					writeDescriptorSet.descriptorCount(descriptorImageInfos.limit());
				}
			}
			writeDescriptorSetsResult[i] = writeDescriptorSets.rewind();
		}
		return new DescriptorsData(info, writeDescriptorSetsResult);
	}
	
	public void changePipeline(@Nullable GraphicsPipeline newPipeline, DescriptorSet @Nullable [] newSets) throws VulkanException
	{
		if (newPipeline == null)
		{
			if (newSets == null) return;
			if (Arrays.equals(descriptorSets, newSets)) return;
			updateInfo();
			return;
		}
		graphicsPipeline = newPipeline;
		
		ImmutableSet<Integer> activeBindings = graphicsPipeline.getActiveBindings();
		if (newPipeline.getDevice() != device || !newPipeline.getLayout().equals(graphicsPipeline.getLayout())) updateInfo();
		int newBindingsCount = activeBindings.isEmpty() ? 0 : activeBindings.max() + 1;
		if (bindingBuffers.length != newBindingsCount)
		{
			bindingBuffers = Arrays.copyOf(bindingBuffers, newBindingsCount);
			bindingBuffersOffsets = Arrays.copyOf(bindingBuffersOffsets, newBindingsCount);
		}
	}
	
	private void updateInfo() throws VulkanException
	{
		descriptorsData = initInfo();
	}
	
	public void loadDescriptors(BindingInfo... bindings) throws VulkanException
	{
		loadDescriptors(_ -> bindings);
	}
	
	public void loadDescriptors(BindingInfo[][] bindings) throws VulkanException
	{
		loadDescriptors(i -> bindings[i]);
	}
	
	public void loadDescriptors(VulkanFunction<Integer, BindingInfo[]> bindingsFactory) throws VulkanException
	{
		for (int i = 0; i < bindings.length; i++) bindings[i] = bindingsFactory.apply(i);
		
		descriptorsData = initInfo();
	}
	
	private DescriptorsData getDescriptorsData()
	{
		return VulkanErrorUtils.nonNull(descriptorsData, "Descriptors has not been loaded");
	}
	
	public int getBindingIndex(int binding)
	{
		return binding;
	}
	
	public BufferLayout getBufferLayout(int frameIndex, int binding)
	{
		return VulkanErrorUtils.nonNull(getDescriptorsData().info[frameIndex][binding].layout);
	}
	
	public void useBufferLayoutWriter(int frameIndex, int binding, VulkanConsumer<BufferLayoutWriter> action) throws VulkanException
	{
		if (getDescriptorsData().info[frameIndex][binding].layout != null)
		{
			action.accept(getBufferLayoutWriter(frameIndex, binding));
			freeBufferLayoutWriter(frameIndex, binding);
		}
	}
	
	public BufferLayoutWriter getBufferLayoutWriter(int frameIndex, int binding) throws VulkanException
	{
		DescriptorsData descriptorsData = getDescriptorsData();
		BufferLayoutWriter writer = descriptorsData.writers[frameIndex][binding];
		if (writer == null)
		{
			BufferBindingInfo bufferBindingInfo = VulkanErrorUtils.nonNull(descriptorsData.info[frameIndex][binding].bufferBindingInfo, "Cannot write to a non buffer object");
			BufferLayout layout = VulkanErrorUtils.nonNull(descriptorsData.info[frameIndex][binding].layout);
			writer = new BufferLayoutWriter(bufferBindingInfo.getBuffer(), layout);
			
			descriptorsData.writers[frameIndex][binding] = writer;
		}
		return writer;
	}
	
	public void freeBufferLayoutWriter(int frameIndex, int binding) throws VulkanException
	{
		DescriptorsData descriptorsData = getDescriptorsData();
		BufferLayoutWriter writer = descriptorsData.writers[frameIndex][binding];
		if (writer != null) descriptorsData.writers[frameIndex][binding] = null;
	}
	
	public void updateDescriptor(int frameIndex, int binding, VulkanBuffer buffer) throws VulkanException
	{
		VkDescriptorBufferInfo.Buffer p = getDescriptorsData().writeDescriptorSets[frameIndex].get(binding).pBufferInfo();
		if (p == null) throw new IllegalStateException("Cannot change descriptor type");
		p.get(0).set(buffer.getVk(), buffer.getOffset(), buffer.getSize());
	}
	
	public void updateDescriptor(int frameIndex, int binding, Texture texture) throws VulkanException
	{
		VkDescriptorImageInfo.Buffer p = getDescriptorsData().writeDescriptorSets[frameIndex].get(binding).pImageInfo();
		if (p == null) throw new IllegalStateException("Cannot change descriptor type");
		p.get(0).imageView(texture.getView().getVk()).imageLayout(texture.getLayout());
	}
	
	public void updateDescriptor(int frameIndex, int binding, TextureSampler sampler) throws VulkanException
	{
		VkDescriptorImageInfo.Buffer p = getDescriptorsData().writeDescriptorSets[frameIndex].get(binding).pImageInfo();
		if (p == null) throw new IllegalStateException("Cannot change descriptor type");
		p.get(0).sampler(sampler.getVk());
	}
	
	public void updateDescriptorSet(int frameIndex, VulkanConsumer<VkWriteDescriptorSet.Buffer> action) throws VulkanException
	{
		DescriptorsData descriptorsData = getDescriptorsData();
		action.accept(descriptorsData.writeDescriptorSets[frameIndex]);
		VK10.vkUpdateDescriptorSets(device.getVk(), descriptorsData.writeDescriptorSets[frameIndex], null);
	}
	
	public void updateDescriptorSet(int frameIndex)
	{
		VK10.vkUpdateDescriptorSets(device.getVk(), getDescriptorsData().writeDescriptorSets[frameIndex], null);
	}
	
	public void updateAllDescriptorSets()
	{
		for (int i = 0; i < bindings.length; i++) updateDescriptorSet(i);
	}
	
	public void draw(CommandBuffer commandBuffer, int frameIndex)
	{
		draw(commandBuffer, frameIndex, 1);
	}
	
	public void draw(CommandBuffer commandBuffer, int frameIndex, int instanceCount)
	{
		draw(commandBuffer, frameIndex, instanceCount, indexBuffer != null ? indexBuffer.getSize() / 4 : 0);
	}
	
	public void draw(CommandBuffer commandBuffer, int frameIndex, int instanceCount, int count)
	{
		if (count == 0) return;
		
		long[] set = new long[] {descriptorSets[frameIndex].getVk()};
		commandBuffer.bindPipeline(graphicsPipeline);
		commandBuffer.bindVertexBuffers(0, bindingBuffers, bindingBuffersOffsets);
		commandBuffer.bindDescriptorSets(graphicsPipeline, set, null);
		if (indexBuffer != null)
		{
			commandBuffer.bindIndexBuffer(indexBuffer, indexOffset, VK10.VK_INDEX_TYPE_UINT32);
			commandBuffer.drawIndexed(count, instanceCount, 0, 0, 0);
		}
		else
		{
			commandBuffer.draw(count, instanceCount, 0, 0);
		}
	}
	
	public void setBuffer(int binding, VulkanBuffer buffer)
	{
		setBuffer(binding, buffer, 0);
	}
	
	public void setBuffer(int binding, VulkanBuffer buffer, long offset)
	{
		if (graphicsPipeline.getActiveBindings().contains(binding))
		{
			bindingBuffers[binding] = buffer;
			bindingBuffersOffsets[binding] = offset;
		}
	}
	
	public void setIndexBuffer(VulkanBuffer indexBuffer)
	{
		setIndexBuffer(indexBuffer, 0);
	}
	
	public void setIndexBuffer(VulkanBuffer indexBuffer, long offset)
	{
		this.indexBuffer = indexBuffer;
		indexOffset = offset;
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
			Debug.print("Binding " + binding, bindingBuffers[binding]);
			Debug.print("Offset " + binding, bindingBuffersOffsets[binding]);
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
