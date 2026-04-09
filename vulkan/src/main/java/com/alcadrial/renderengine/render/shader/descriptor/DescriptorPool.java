package com.alcadrial.renderengine.render.shader.descriptor;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.layout.ShaderLayout;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class DescriptorPool extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static DescriptorPool ofShaderProgram(Device device, ShaderProgram shaderProgram, int maxSets) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			//may be more than required
			int size = (int) shaderProgram.getShaders().sumOfInt(ps -> ps.info().getLayouts().count(l -> l.getDescriptorCount() > 0));
			VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.calloc(size, stack);
			for (PreparedShader preparedShader : shaderProgram.getShaders()) for (ShaderLayout layout : preparedShader.info().getLayouts()) if (layout.getDescriptorCount() > 0)
			{
				poolSizes.get()
					.type(layout.getType().getType())
					.descriptorCount(layout.getDescriptorCount());
			}
			VkDescriptorPoolCreateInfo createInfo = VkDescriptorPoolCreateInfo.calloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.maxSets(maxSets)
				.pPoolSizes(poolSizes.rewind());
			
			return new DescriptorPool(device, createInfo);
		}
	}
	
	public static DescriptorPool ofShader(Device device, PreparedShader preparedShader, int maxSets) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			ShaderInfo shaderInfo = preparedShader.info();
			VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.calloc(shaderInfo.getLayouts().count(l -> l.getDescriptorCount() > 0), stack);
			for (ShaderLayout layout : shaderInfo.getLayouts()) if (layout.getDescriptorCount() > 0)
			{
				poolSizes.get()
					.type(layout.getType().getType())
					.descriptorCount(layout.getDescriptorCount());
			}
			VkDescriptorPoolCreateInfo createInfo = VkDescriptorPoolCreateInfo.calloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.maxSets(maxSets)
				.pPoolSizes(poolSizes.rewind());
			
			return new DescriptorPool(device, createInfo);
		}
	}
	
	private final int flags;
	
	public DescriptorPool(Device device, VkDescriptorPoolCreateInfo createInfo) throws VulkanException
	{
		super(device);
		
		long[] p0 = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorPool(device.getVk(), createInfo, null, p0), "Failed to create descriptor pool");
		vk = p0[0];
		flags = createInfo.flags();
	}

	public int getFlags()
	{
		return flags;
	}
	
	public DescriptorSet allocate(DescriptorSetLayout layout) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] p = new long[1];
			LongBuffer pp = stack.longs(layout.getVk());
			VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.calloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
				.descriptorPool(vk)
				.pSetLayouts(pp);
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.getVk(), allocateInfo, p), "Failed to allocate descriptor sets");
			return new DescriptorSet(device, p[0], this);
		}
	}
	
	public DescriptorSet[] allocates(DescriptorSetLayout layout, int count) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] p = new long[count];
			LongBuffer pp = stack.callocLong(count);
			for (int i = 0; i < count; i++) pp.put(layout.getVk());
			VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.calloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
				.descriptorPool(vk)
				.pSetLayouts(pp.rewind());
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.getVk(), allocateInfo, p), "Failed to allocate descriptor sets");
			DescriptorSet[] sets = new DescriptorSet[count];
			for (int i = 0; i < count; i++) sets[i] = new DescriptorSet(device, p[i], this);
			return sets;
		}
	}
	
	public DescriptorSet[] allocateBatch(DescriptorSetLayout[] layouts) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			DescriptorSet[] sets = new DescriptorSet[layouts.length];
			LongBuffer pp = stack.mallocLong(layouts.length);
			for (DescriptorSetLayout layout : layouts) pp.put(layout.getVk());
			long[] p = new long[1];
			VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.calloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
				.descriptorPool(vk)
				.pSetLayouts(pp.rewind());
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.getVk(), allocateInfo, p), "Failed to allocate descriptor sets");
			for (int i = 0; i < layouts.length; i++) sets[i] = new DescriptorSet(device, p[i], this);
			return sets;
		}
	}
	
	public long allocateRaw(DescriptorSetLayout layout) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] p = new long[1];
			LongBuffer pp = stack.longs(layout.getVk());
			VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.calloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
				.descriptorPool(vk)
				.pSetLayouts(pp);
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.getVk(), allocateInfo, p), "Failed to allocate descriptor sets");
			return p[0];
		}
	}
	
	public long[] allocateRawBatch(DescriptorSetLayout[] layouts) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			LongBuffer pp = stack.mallocLong(layouts.length);
			for (DescriptorSetLayout layout : layouts) pp.put(layout.getVk());
			long[] p = new long[layouts.length];
			VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.calloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
				.descriptorPool(vk)
				.pSetLayouts(pp.rewind());
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.getVk(), allocateInfo, p), "Failed to allocate descriptor sets");
			return p;
		}
	}
	
	public void freeRaw(long... descriptorSets) throws VulkanException
	{
		VulkanErrorUtils.assertNoError(VK10.vkFreeDescriptorSets(device.getVk(), vk, descriptorSets));
	}
	
	public void reset() throws VulkanException
	{
		VulkanErrorUtils.assertNoError(VK10.vkResetDescriptorPool(device.getVk(), vk, 0));
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroyDescriptorPool(device.getVk(), vk, null);
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.debug("DescriptorPool: " + name);
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
}
