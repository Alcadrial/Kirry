package com.alcadrial.renderengine.render.buffer;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.Pair;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class VulkanBuffer implements AutoCloseable {
	
	protected long id;
	protected long memory;
	protected long size;
	
	public VulkanBuffer(long size, int usage)
	{
		this.size = size;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			Pair<Long, Long> data = build(stack, size, usage, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
			id = data.getLeft();
			memory = data.getRigth();
		}
	}
	
	protected Pair<Long, Long> build(MemoryStack stack, long size, int usage, int properties)
	{
		Pair<Long, Long> data = new Pair<>(0L, 0L);
		
		Device device = RenderEngine.getVulkan().getDevice();
		VkBufferCreateInfo bufferInfo = VulkanUtils.bufferCreateInfo(stack, 0, size, usage);
		bufferInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateBuffer(device.getDevice(), bufferInfo, null, p), "failed to create buffer");
		data.setLeft(p[0]);
		
		VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
		VK10.vkGetBufferMemoryRequirements(device.getDevice(), data.getLeft(), memRequirements);
		
		int index;
		VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.malloc(stack);
		VK10.vkGetPhysicalDeviceMemoryProperties(RenderEngine.getVulkan().getPhysicalDevice().getDevice(), memProperties);
		
		for (index = 0; index < memProperties.memoryTypeCount(); index++)
		{
			if ((memRequirements.memoryTypeBits() & (1 << index)) != 0 && (memProperties.memoryTypes(index).propertyFlags() & properties) == properties)
			{
				break;
			}
		}
		
		VulkanErrorUtils.assertStatement(index < memProperties.memoryTypeCount(), "Failed to find suitable memory type");
		
		LongBuffer pVertexBufferMemory = stack.mallocLong(1);
		VulkanErrorUtils.assertNoError(VK10.vkAllocateMemory(device.getDevice(), VulkanUtils.memoryAllocateInfo(stack, memRequirements.size(), index), null, pVertexBufferMemory), "Failed to allocate buffer memory");
		data.setRigth(pVertexBufferMemory.get(0));
		
		VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(device.getDevice(), data.getLeft(), data.getRigth(), 0), "Failed to bind buffer memory");
		return data;
	}
	
	public long getId()
	{
		return id;
	}
	
	public long getSize()
	{
		return size;
	}
	
	@Override
	public void close()
	{
		VkDevice device = RenderEngine.getVulkan().getDevice().getDevice();
		VK10.vkDestroyBuffer(device, id, null);
		VK10.vkFreeMemory(device, memory, null);
	}
}
