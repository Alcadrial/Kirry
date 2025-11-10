package com.alcadrial.renderengine.render;

import java.math.RoundingMode;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.pipeline.ComputePipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderLayoutBinding;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.shader.ShaderUtils;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout.Type;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLocalSizeLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderStorageBufferLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderUniformBufferLayout;
import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class Computer implements AutoCloseable, VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private class Binding {
		
		private @Nullable BufferWriter writer;
		private VulkanBuffer buffer;
		private int offset;
		private int size;
		private BufferLayout layout;
		
		private Binding(@Nullable BufferWriter writer, VulkanBuffer buffer, int offset, int size)
		{
			this.writer = writer;
			this.buffer = buffer;
			this.offset = offset;
			this.size = size;
			layout = new BufferLayout(device, new ReentrantLock());
			layout.setWriter(writer);
		}
		
		public @Nullable BufferWriter getWriter()
		{
			return writer;
		}
		
		public VulkanBuffer getBuffer()
		{
			return buffer;
		}
		
		public int getOffset()
		{
			return offset;
		}
		
		public int getSize()
		{
			return size;
		}
		
		public BufferLayout getLayout()
		{
			return layout;
		}
	}
	
	private Device device;
	private ComputePipeline computePipeline;
	private Vector3i groupCount;
	
	private long computeDescriptorPool;
	private long computeDescriptorSetLayout;
	private long[] computeDescriptorSet;
	private PreparedShader computeShader;
	private Map<Integer, Binding> bindings;
	
	public Computer(Device device, PreparedShader computeShader)
	{
		VulkanErrorUtils.match(computeShader.info().getType(), ShaderType.COMPUTE, "Only compute shader can be used");
		this.device = device;
		this.computeShader = computeShader;
		for (ShaderLayout layout : computeShader.info().getLayouts()) if (layout.getType() == Type.LOCAL_SIZE)
		{
			groupCount = ((ShaderLocalSizeLayout) layout).getLocalSize();
		}
		VulkanErrorUtils.nonNull(groupCount, "Missing localsize in shader info");
		bindings = new HashMap<>();
	}
	
	public BufferLayout setBinding(int binding, BufferWriter writer)
	{
		return setBinding(binding, writer, writer.getDstBuffer(), writer.getDstOffset(), writer.getSize());
	}
	
	public BufferLayout setBinding(int binding, VulkanBuffer buffer)
	{
		return setBinding(binding, buffer, 0, buffer.getSize());
	}
	
	public BufferLayout setBinding(int binding, VulkanBuffer buffer, int offset, int size)
	{
		return setBinding(binding, null, buffer, offset, size);
	}
	
	private BufferLayout setBinding(int binding, @Nullable BufferWriter writer, VulkanBuffer buffer, int offset, int size)
	{
		Binding bindingData = new Binding(writer, buffer, offset, size);
		bindings.put(binding, bindingData);
		return bindingData.getLayout();
	}
	
	public void init() throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkDescriptorPoolCreateInfo poolInfo = VulkanUtils.descriptorPoolCreateInfo(stack, VulkanUtils.descriptorPoolSize(stack, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 4));// static
			poolInfo.maxSets(3);
			
			long[] p0 = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorPool(device.getVk(), poolInfo, null, p0), "Failed to create descriptor pool!");
			computeDescriptorPool = p0[0];
			
			Map<Integer, ShaderLayoutBinding> bindingFlags = new HashMap<>();
			ShaderUtils.extractData(computeShader, bindingFlags);
			
			VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = ShaderUtils.createDescriptorSetLayoutBindingBuffer(stack, bindingFlags);
			
			LongBuffer p = stack.callocLong(1);
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorSetLayout(device.getVk(), VulkanUtils.descriptorSetLayoutInfo(stack, 0, descriptorSetLayoutBindings), null, p), "Failed to create descriptor set layout!");
			computeDescriptorSetLayout = p.rewind().get(0);
			
			computeDescriptorSet = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.getVk(), VulkanUtils.descriptorSetAllocateInfo(stack, computeDescriptorPool, p), computeDescriptorSet), "Failed to allocate descriptor sets!");
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			pipelineLayoutInfo.pSetLayouts(p);
			computePipeline = new ComputePipeline(device, new PipelineLayout(device, pipelineLayoutInfo), true, computeShader);
			
			List<ShaderLayout> layouts = computeShader.info().getLayouts();
			
			int uniformBufferCount = 0;
			int storageBufferCount = 0;
			
			for (ShaderLayout layout : layouts)
			{
				switch (layout.getType())
				{
					case UNIFORM_BUFFER -> uniformBufferCount++;
					case STORAGE_BUFFER -> storageBufferCount++;
				}
			}
			
			VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.malloc(uniformBufferCount + storageBufferCount);
			
			for (ShaderLayout shaderLayout : computeShader.info().getLayouts())
			{
				switch (shaderLayout.getType())
				{
					case UNIFORM_BUFFER -> {
						ShaderUniformBufferLayout shaderUniformBufferLayout = (ShaderUniformBufferLayout) shaderLayout;
						
						Binding binding = bindings.get(shaderUniformBufferLayout.getBinding());
						VulkanBuffer buffer = binding.getBuffer();
						BufferLayout layout = binding.getLayout();
						
						layout.setWriteDescriptorSets(writeDescriptorSets.slice(0, 1));
						VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
						writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
						writeDescriptorSet.pNext(0);
						writeDescriptorSet.dstBinding(shaderUniformBufferLayout.getBinding());
						writeDescriptorSet.dstSet(computeDescriptorSet[0]);
						writeDescriptorSet.dstArrayElement(0);
						writeDescriptorSet.descriptorCount(1);
						writeDescriptorSet.descriptorType(Type.UNIFORM_BUFFER.getType());
						
						VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.malloc(1);
						descriptorBufferInfo.buffer(buffer.getVk());
						descriptorBufferInfo.offset(binding.getOffset());
						descriptorBufferInfo.range(binding.getSize());
						
						writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
						writeDescriptorSet.pImageInfo(null);
						writeDescriptorSet.pTexelBufferView(null);
						
						int offset = 0;
						int partialOffset = 0;
						for (ShaderVariable variable : shaderUniformBufferLayout.getVariables())
						{
							int size = variable.getType().getLength();
							if (partialOffset > 0 && partialOffset + size > 16)
							{
								offset += 16 - partialOffset;
							}
							layout.mark(variable, offset / 4);
							offset += size;
							partialOffset = offset % 16;
						}
						layout.updateDescriptorSets();
					}
					case STORAGE_BUFFER -> {
						ShaderStorageBufferLayout shaderStorageBufferLayout = (ShaderStorageBufferLayout) shaderLayout;
						
						Binding binding = bindings.get(shaderStorageBufferLayout.getBinding());
						VulkanBuffer buffer = binding.getBuffer();
						BufferLayout layout = binding.getLayout();
						
						layout.setWriteDescriptorSets(writeDescriptorSets.slice(0, 1));
						VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
						writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
						writeDescriptorSet.pNext(0);
						writeDescriptorSet.dstSet(computeDescriptorSet[0]);
						writeDescriptorSet.dstBinding(shaderStorageBufferLayout.getBinding());
						writeDescriptorSet.dstArrayElement(0);
						writeDescriptorSet.descriptorCount(1);
						writeDescriptorSet.descriptorType(ShaderLayout.Type.STORAGE_BUFFER.getType());
						
						VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.malloc(1);
						descriptorBufferInfo.buffer(buffer.getVk());
						descriptorBufferInfo.offset(binding.getOffset());
						descriptorBufferInfo.range(binding.getSize());
						
						writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
						writeDescriptorSet.pImageInfo(null);
						writeDescriptorSet.pTexelBufferView(null);
						
						layout.updateDescriptorSets();
					}
				}
			}
		}
	}
	
	public ComputePipeline getComputePipeline()
	{
		return computePipeline;
	}
	
	public void setComputePipeline(ComputePipeline computePipeline)
	{
		this.computePipeline = computePipeline;
	}
	
	public void setComputeDescriptorSet(long[] computeDescriptorSet)
	{
		this.computeDescriptorSet = computeDescriptorSet;
	}
	
	public void setGroupCount(Vector3i groupCount)
	{
		this.groupCount = groupCount;
	}
	
	public void dispatch(CommandBuffer commandBuffer, int countX, int countY, int countZ)
	{
		commandBuffer.bindPipeline(computePipeline);
		commandBuffer.bindDescriptorSets(computePipeline, computeDescriptorSet, new int[0]);
		commandBuffer.dispatch(ceil(countX, groupCount.x()), ceil(countY, groupCount.y()), ceil(countZ, groupCount.z()));
	}
	
	private int ceil(int a, int b)
	{
		return Math.toInt((float) a / b, RoundingMode.CEILING);
	}
	
	@Override
	public void close()
	{
		device.waitIdle();
		if (computePipeline != null) computePipeline.close();
		VK10.vkDestroyDescriptorSetLayout(device.getVk(), computeDescriptorSetLayout, null);
		VK10.vkDestroyDescriptorPool(device.getVk(), computeDescriptorPool, null);
	}
	
	@Override
	public void debug()
	{
		Debug.print("Device", device);
		Debug.print("Pipeline ", computePipeline);
		Debug.print("GroupCount: " + groupCount.x() + "x" + groupCount.y() + "x" + groupCount.z());
		
		Debug.print("DescriptorPool: " + Debug.address(computeDescriptorPool));
		Debug.print("DescriptorSetLayout: " + Debug.address(computeDescriptorSetLayout));
		Debug.print("DescriptorSet: " + Debug.address(computeDescriptorSet[0]));
	}
}
