package com.alcadrial.renderengine.render;

import java.math.RoundingMode;
import java.nio.LongBuffer;
import java.util.HashMap;
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
import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
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
import com.alcadrial.renderengine.render.shader.layouts.ShaderUniformBufferLayout;
import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class Computer implements AutoCloseable, VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private Device device;
	private ComputePipeline computePipeline;
	private Vector3i groupCount;
	
	private VertexBuffer inputBuffer;
	private BufferLayout inputLayout;
	private VertexBuffer outputBuffer;
	private BufferLayout outputLayout;
	private VulkanBuffer uniformBuffer;
	
	private long computeDescriptorPool;
	private long computeDescriptorSetLayout;
	private long[] computeDescriptorSet;
	private BufferLayout computeUniformBufferLayout;
	private BufferLayout computeStorageBufferLayout;
	private ReentrantLock computeDescriptorSetLock;
	private PreparedShader computeShader;
	
	public Computer(Device device, PreparedShader computeShader)
	{
		VulkanErrorUtils.match(computeShader.info().getType(), ShaderType.COMPUTE, "Only compute shader can be used");
		this.device = device;
		this.computeShader = computeShader;
		for (ShaderLayout layout : computeShader.info().getLayouts()) if (layout.getType() == Type.LOCAL_SIZE) groupCount = ((ShaderLocalSizeLayout) layout).getLocalSize();
		VulkanErrorUtils.nonNull(groupCount, "Missing localsize in shader info");
	}
	
	public void setInputBuffer(VertexBuffer inputBuffer, BufferLayout layout)
	{
		assert (inputBuffer.getGpuUsage() & VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT) != 0 : "Only Storage Buffers can be used in compute";
		this.inputBuffer = inputBuffer;
		inputLayout = layout;
	}
	
	public void setOutputBuffer(VertexBuffer outputBuffer, BufferLayout layout)
	{
		assert (outputBuffer.getGpuUsage() & VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT) != 0 : "Only Storage Buffers can be used in compute";
		this.outputBuffer = outputBuffer;
		outputLayout = layout;
	}
	
	public void setUniformBuffer(VulkanBuffer uniformBuffer)
	{
		assert (uniformBuffer.getUsage() & VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT) != 0 : "Not a Uniform Buffer";
		this.uniformBuffer = uniformBuffer;
	}
	
	public VertexBuffer getOutputBuffer()
	{
		return outputBuffer;
	}
	
	public BufferLayout getComputeUniformBufferLayout()
	{
		return computeUniformBufferLayout;
	}
	
	public void init()
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
			
			computeDescriptorSetLock = new ReentrantLock();
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			pipelineLayoutInfo.pSetLayouts(p);
			computePipeline = new ComputePipeline(device, new PipelineLayout(device, pipelineLayoutInfo), true, computeShader);
			
			VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.malloc(3);
			
			computeUniformBufferLayout = new BufferLayout(device, writeDescriptorSets.slice(0, 1), 0, computeDescriptorSetLock);
			computeStorageBufferLayout = new BufferLayout(device, writeDescriptorSets.slice(1, 2), 0, computeDescriptorSetLock);
			
			VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
			writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
			writeDescriptorSet.pNext(0);
			writeDescriptorSet.dstSet(computeDescriptorSet[0]);
			writeDescriptorSet.dstBinding(0);
			writeDescriptorSet.dstArrayElement(0);
			writeDescriptorSet.descriptorCount(1);
			writeDescriptorSet.descriptorType(ShaderLayout.Type.UNIFORM_BUFFER.getType());
			
			int offset = 512;
			int partialOffset = 0;
			
			VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
			descriptorBufferInfo.buffer(uniformBuffer.getVk());
			descriptorBufferInfo.offset(offset);
			
			for (ShaderLayout shaderLayout : computeShader.info().getLayouts()) if (shaderLayout.getType() == Type.UNIFORM_BUFFER)
			{
				ShaderUniformBufferLayout layout = (ShaderUniformBufferLayout) shaderLayout;
				for (ShaderVariable variable : layout.getVariables())
				{
					int size = variable.getType().getLength();
					if (partialOffset > 0 && partialOffset + size > 16) offset += 16 - partialOffset;
					computeUniformBufferLayout.mark(variable, offset / 4);
					offset += size;
					partialOffset = offset % 16;
				}
			}
			
			descriptorBufferInfo.range(offset - descriptorBufferInfo.offset());
			writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
			writeDescriptorSet.pImageInfo(null);
			writeDescriptorSet.pTexelBufferView(null);
			
			writeDescriptorSet = writeDescriptorSets.get();
			writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
			writeDescriptorSet.pNext(0);
			writeDescriptorSet.dstSet(computeDescriptorSet[0]);
			writeDescriptorSet.dstBinding(1);
			writeDescriptorSet.dstArrayElement(0);
			writeDescriptorSet.descriptorCount(1);
			writeDescriptorSet.descriptorType(ShaderLayout.Type.STORAGE_BUFFER.getType());
			descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
			descriptorBufferInfo.buffer(inputBuffer.getGpuVk());
			descriptorBufferInfo.offset(0);
			descriptorBufferInfo.range(inputBuffer.getGpuSize());
			writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
			writeDescriptorSet.pImageInfo(null);
			writeDescriptorSet.pTexelBufferView(null);
			
			writeDescriptorSet = writeDescriptorSets.get();
			writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
			writeDescriptorSet.pNext(0);
			writeDescriptorSet.dstSet(computeDescriptorSet[0]);
			writeDescriptorSet.dstBinding(2);
			writeDescriptorSet.dstArrayElement(0);
			writeDescriptorSet.descriptorCount(1);
			writeDescriptorSet.descriptorType(ShaderLayout.Type.STORAGE_BUFFER.getType());
			descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
			descriptorBufferInfo.buffer(outputBuffer.getGpuVk());
			descriptorBufferInfo.offset(0);
			descriptorBufferInfo.range(outputBuffer.getGpuSize());
			writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
			writeDescriptorSet.pImageInfo(null);
			writeDescriptorSet.pTexelBufferView(null);
			
			writeDescriptorSets.rewind();
			
			computeUniformBufferLayout.updateDescriptorSets();
			computeStorageBufferLayout.updateDescriptorSets();
			
			System.out.println("SUCCESS");
		}
	}
	
	@SuppressWarnings("null")
	public void swapBuffers()
	{
		computeStorageBufferLayout.update(sets -> {
			VertexBuffer t = inputBuffer;
			inputBuffer = outputBuffer;
			outputBuffer = t;
			sets.get(0).pBufferInfo().buffer(inputBuffer.getGpuVk());
			sets.get(1).pBufferInfo().buffer(outputBuffer.getGpuVk());
		});
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
	
	public void dispatch(CommandBufferData commandBuffer, int countX, int countY, int countZ)
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
		computePipeline.close();
		VK10.vkDestroyDescriptorSetLayout(device.getVk(), computeDescriptorSetLayout, null);
		VK10.vkDestroyDescriptorPool(device.getVk(), computeDescriptorPool, null);
	}
	
	@Override
	public void debug()
	{
		Debug.print("Device", device);
		Debug.print("Pipeline ", computePipeline);
		Debug.print("GroupCount: " + groupCount.x() + "x" + groupCount.y() + "x" + groupCount.z());
		
		Debug.print("InputBuffer", inputBuffer);
		//Debug.printState("InputLayout", inputLayout);
		Debug.print("OutputBuffer", outputBuffer);
		//Debug.printState("OutputLayout", outputLayout);
		Debug.print("UniformBuffer", uniformBuffer);
		Debug.print("DescriptorPool: " + Debug.address(computeDescriptorPool));
		Debug.print("DescriptorSetLayout: " + Debug.address(computeDescriptorSetLayout));
		Debug.print("DescriptorSet: " + Debug.address(computeDescriptorSet[0]));
		Debug.printState("ComputeUniformLayout", computeUniformBufferLayout);
		Debug.printState("ComputeStorageLayout", computeStorageBufferLayout);
	}
}
