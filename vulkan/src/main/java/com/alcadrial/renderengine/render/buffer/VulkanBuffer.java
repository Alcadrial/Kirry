package com.alcadrial.renderengine.render.buffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.LongDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class VulkanBuffer extends LongDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected long memory;
	protected long size;
	protected Object closingMonitor;
	private ByteBuffer map;
	
	public VulkanBuffer(Device device, long size, int usage)
	{
		super(device);
		this.size = size;
		closingMonitor = new Object();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			Pair<Long, Long> data = build(stack, size, usage, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
			vk = data.l();
			memory = data.r();
		}
	}
	
	protected VulkanBuffer(Device device, long size, int usage, int flags)
	{
		super(device);
		this.size = size;
		closingMonitor = new Object();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			Pair<Long, Long> data = build(stack, size, usage, flags);
			vk = data.l();
			memory = data.r();
		}
	}
	
	protected Pair<Long, Long> build(MemoryStack stack, long size, int usage, int properties)
	{
		Pair<Long, Long> data = new Pair<>(0L, 0L);
		
		VkBufferCreateInfo bufferInfo = VulkanUtils.bufferInfo(stack, size, usage);
		bufferInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateBuffer(device.getVk(), bufferInfo, null, p), "Failed to create buffer");
		data.setLeft(p[0]);
		
		VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
		VK10.vkGetBufferMemoryRequirements(device.getVk(), data.l(), memRequirements);
		
		int index;
		VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.malloc(stack);
		VK10.vkGetPhysicalDeviceMemoryProperties(device.getPhysicalDevice().getVk(), memProperties);
		
		for (index = 0; index < memProperties.memoryTypeCount(); index++)
		{
			if ((memRequirements.memoryTypeBits() & (1 << index)) != 0 && (memProperties.memoryTypes(index).propertyFlags() & properties) == properties)
			{
				break;
			}
		}
		
		VulkanErrorUtils.assertStatement(index < memProperties.memoryTypeCount(), "Failed to find suitable memory type");
		
		LongBuffer pVertexBufferMemory = stack.mallocLong(1);
		VulkanErrorUtils.assertNoError(VK10.vkAllocateMemory(device.getVk(), VulkanUtils.memoryAllocateInfo(stack, memRequirements.size(), index), null, pVertexBufferMemory), "Failed to allocate buffer memory");
		data.setRight(pVertexBufferMemory.get(0));
		
		VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(device.getVk(), data.l(), data.r(), 0), "Failed to bind buffer memory");
		return data;
	}
	
	private synchronized ByteBuffer getByteBuffer()
	{
		if (map == null)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				PointerBuffer gpuData = stack.mallocPointer(1);
				VulkanErrorUtils.assertNoError(VK10.vkMapMemory(device.getVk(), memory, 0, size, 0, gpuData), "Failed to map buffer memory");
				map = gpuData.getByteBuffer((int) size);
			}
		}
		return map;
	}
	
	public ByteBuffer asByteBuffer()
	{
		return getByteBuffer().duplicate();
	}
	
	public CharBuffer asCharBuffer()
	{
		return getByteBuffer().asCharBuffer();
	}
	
	public ShortBuffer asShortBuffer()
	{
		return getByteBuffer().asShortBuffer();
	}
	
	public IntBuffer asIntBuffer()
	{
		return getByteBuffer().asIntBuffer().duplicate();
	}
	
	public LongBuffer asLongBuffer()
	{
		return getByteBuffer().asLongBuffer();
	}
	
	public FloatBuffer asFloatBuffer()
	{
		return getByteBuffer().asFloatBuffer().duplicate();
	}
	
	public DoubleBuffer asDoubleBuffer()
	{
		return getByteBuffer().asDoubleBuffer();
	}
	
	public long getSize()
	{
		return size;
	}
	
	@Override
	public void close()
	{
		synchronized (closingMonitor)
		{
			VK10.vkDestroyBuffer(device.getVk(), vk, null);
			VK10.vkFreeMemory(device.getVk(), memory, null);
			memory = 0;
			super.close();
		}
	}
}
