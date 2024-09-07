package com.alcadrial.renderengine.render.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

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
	 * Create a device local buffer.
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
			memory = device.allocateMemory(stack, this, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
		}
	}
	
	public VulkanBuffer(Device device, int size, int usage, int requiredProperties, int optimalProperties)
	{
		super(device);
		offset = 0;
		this.size = size;
		this.usage = usage;
		parent = null;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = device.allocateMemory(stack, this, requiredProperties, optimalProperties);
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
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	public ByteBuffer getByteBuffer(int offset, int size)
	{
		if (map == null)
		{
			if (parent != null)
			{
				map = parent.getByteBuffer((int) (this.offset + offset), size);
			}
			else
			{
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					PointerBuffer gpuData = stack.mallocPointer(1);
					VulkanErrorUtils.assertNoError(VK10.vkMapMemory(device.getVk(), memory, this.offset, this.size, 0, gpuData), "Failed to map buffer memory");
					map = gpuData.getByteBuffer(this.size);
					mappings++;
				}
			}
		}
		return map.slice(offset, size).order(map.order());
	}
	
	public synchronized void unmapBuffer()
	{
		if (map != null)
		{
			if (parent != null)
			{
				parent.unmapBuffer();
			}
			else if (mappings <= 1)
			{
				mappings = 0;
				VK10.vkUnmapMemory(device.getVk(), memory);//keep mapped if possible
				map = null;
			}
			else
			{
				mappings--;
			}
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
	public int getSize()
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
