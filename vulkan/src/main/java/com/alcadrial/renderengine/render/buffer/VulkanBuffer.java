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

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.LongDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class VulkanBuffer extends LongDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected long memory;
	protected long size;
	protected Object closingMonitor;
	private ByteBuffer map;
	
	/**
	 * 
	 * @param device the device holding the new buffer
	 * @param size in bytes
	 * @param usage a bitmask of {@link VK10#VK_BUFFER_USAGE_TRANSFER_SRC_BIT VkBufferUsageFlagBits} specifying allowed usages of the buffer
	 */
	public VulkanBuffer(Device device, long size, int usage)
	{
		super(device);
		this.size = size;
		closingMonitor = new Object();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = device.allocateBufferMemory(stack, vk, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		}
	}
	
	protected VulkanBuffer(Device device, long size, int usage, int flags)
	{
		super(device);
		this.size = size;
		closingMonitor = new Object();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = device.allocateBufferMemory(stack, vk, flags);
		}
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
