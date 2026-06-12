package com.alcadrial.kirry.render.shader.descriptor;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.util.RawDeviceResource;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class DescriptorPool extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
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
