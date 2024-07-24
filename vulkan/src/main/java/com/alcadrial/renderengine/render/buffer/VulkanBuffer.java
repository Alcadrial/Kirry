package com.alcadrial.renderengine.render.buffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.Future;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class VulkanBuffer extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected long memory;
	private ByteBuffer map;
	private int mappings;
	
	protected long offset;
	protected int size;
	private VulkanBuffer parent;
	private int usage;
	
	/**
	 * 
	 * @param device the device holding the new buffer
	 * @param size in bytes
	 * @param usage a bitmask of {@link VK10#VK_BUFFER_USAGE_TRANSFER_SRC_BIT VkBufferUsageFlagBits} specifying allowed usages of the buffer
	 */
	public VulkanBuffer(Device device, int size, int usage)
	{
		super(device);
		offset = 0;
		this.size = size;
		this.usage = usage;
		parent = null;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = device.allocateBufferMemory(stack, vk, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		}
	}
	
	protected VulkanBuffer(Device device, int size, int usage, int flags)
	{
		super(device);
		offset = 0;
		this.size = size;
		this.usage = usage;
		parent = null;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = device.allocateBufferMemory(stack, vk, flags);
		}
	}
	
	public VulkanBuffer(VulkanBuffer buffer, long offset, int usage, int size)
	{
		super(buffer.device);
		this.offset = offset;
		this.size = size;
		this.usage = usage;
		parent = buffer;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = buffer.memory;
			VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(device.getVk(), vk, memory, offset), "Failed to bind buffer memory");
		}
	}
	
	public VulkanBuffer(VulkanBuffer buffer, long offset, int size)
	{
		super(buffer.device);
		this.offset = offset;
		this.size = size;
		usage = buffer.usage;
		parent = buffer;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = buffer.memory;
			VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(device.getVk(), vk, memory, offset), "Failed to bind buffer memory");
		}
	}
	
	public byte[] getAllBytes()
	{
		byte[] array = new byte[size];
		getByteBuffer().get(array).reset();
		return array;
	}
	
	public int[] getAllInts()
	{
		IntBuffer buffer = asIntBuffer();
		int[] array = new int[buffer.capacity()];
		buffer.get(array);
		return array;
	}
	
	public float[] getAllFloats()
	{
		FloatBuffer buffer = asFloatBuffer();
		float[] array = new float[buffer.capacity()];
		buffer.get(array);
		return array;
	}
	
	public float[] getAllFloats(int offset, int size)
	{
		float[] array = new float[size];
		asFloatBuffer().position(offset).get(array);
		return array;
	}
	
	public synchronized Future<?> copyToBuffer(VulkanBuffer dstBuffer)
	{
		return copyToBuffer(0, 0, size, dstBuffer.vk);
	}
	
	public synchronized Future<?> copyToBuffer(long offset, int size, VulkanBuffer dstBuffer)
	{
		return copyToBuffer(offset, offset, size, dstBuffer.vk);
	}
	
	public synchronized Future<?> copyToBuffer(long srcOffset, long dstOffset, int size, VulkanBuffer dstBuffer)
	{
		return copyToBuffer(srcOffset, dstOffset, size, dstBuffer.vk);
	}
	
	protected synchronized Future<?> copyToBuffer(long srcOffset, long dstOffset, int size, long dstBuffer)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			CommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			VK10.vkCmdCopyBuffer(commandBuffer.getVk(), vk, dstBuffer, VulkanUtils.bufferCopy(stack, srcOffset, dstOffset, size));
			return device.submitSingleUseCommandBuffer(stack, commandBuffer);
		}
	}
	
	private synchronized ByteBuffer getByteBuffer()
	{
		if (map == null)
		{
			if (parent != null) map = parent.getByteBuffer().position((int) offset);
			else
			{
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					PointerBuffer gpuData = stack.mallocPointer(1);
					VulkanErrorUtils.assertNoError(VK10.vkMapMemory(device.getVk(), memory, offset, size, 0, gpuData), "Failed to map buffer memory");
					map = gpuData.getByteBuffer((int) size);
					mappings++;
				}
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
	
	public synchronized void unmapBuffer()
	{
		if (map != null)
		{
			if (parent != null) parent.unmapBuffer();
			else if (mappings <= 1)
			{
				mappings = 0;
				VK10.vkUnmapMemory(device.getVk(), memory);
				map = null;
			}
			else mappings--;
		}
	}
	
	/**
	 * 
	 * @return the offset from memory in bytes
	 */
	public long getOffset()
	{
		return offset;
	}
	
	/**
	 * 
	 * @return the size of this buffer in bytes
	 */
	public long getSize()
	{
		return size;
	}
	
	public int getUsage()
	{
		return usage;
	}
	
	protected void closeCopy()
	{
		VK10.vkDestroyBuffer(device.getVk(), vk, null);
		memory = 0;
		super.close();
	}
	
	protected void closeOriginal()
	{
		VK10.vkDestroyBuffer(device.getVk(), vk, null);
		VK10.vkFreeMemory(device.getVk(), memory, null);
		memory = 0;
		super.close();
	}
	
	@Override
	public void close()
	{
		if (map != null) unmapBuffer();
		if (parent != null) closeCopy();
		else closeOriginal();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("Offset: " + offset);
		Debug.print("Memory: " + Debug.address(memory));
		Debug.print("Size: " + size);
	}
}
