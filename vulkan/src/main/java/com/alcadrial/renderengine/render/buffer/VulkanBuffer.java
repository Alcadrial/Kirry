package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.DataUtils;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class VulkanBuffer extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static VulkanBuffer asLocal(VulkanBuffer vulkanBuffer) throws VulkanException
	{
		return new VulkanBuffer(vulkanBuffer.device, vulkanBuffer.size, vulkanBuffer.usage | VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT);
	}
	
	protected Memory memory;
	private VulkanBuffer parent;
	
	protected final long offset;
	protected final int size;
	private final int usage;
	
	/**
	 *
	 * Create a device local buffer.
	 *
	 * @param device the device holding the new buffer
	 * @param size in bytes
	 * @param usage a bitmask of {@link VK10#VK_BUFFER_USAGE_TRANSFER_SRC_BIT VkBufferUsageFlagBits} specifying allowed usages of the buffer
	 */
	public VulkanBuffer(Device device, int size, int usage) throws VulkanException
	{
		super(device);
		offset = 0;
		this.size = size;
		this.usage = usage;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = device.allocateMemory(stack, this, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
		}
		Debug.debugAllocation(() -> "Allocated " + this);
	}
	
	public VulkanBuffer(Device device, int size, int usage, int requiredProperties, int optimalProperties) throws VulkanException
	{
		super(device);
		offset = 0;
		this.size = size;
		this.usage = usage;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = device.allocateMemory(stack, this, requiredProperties, optimalProperties);
		}
		Debug.debugAllocation(() -> "Allocated " + this);
	}
	
	public VulkanBuffer(VulkanBuffer buffer, long offset, int size, int usage) throws VulkanException
	{
		super(buffer.device);
		this.offset = buffer.offset + offset;
		this.size = size;
		this.usage = usage;
		parent = buffer;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = buffer.memory;
			VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(device.getVk(), vk, memory.getVk(), buffer.offset + offset), "Failed to bind buffer memory");
		}
	}
	
	public VulkanBuffer(VulkanBuffer buffer, long offset, int size) throws VulkanException
	{
		super(buffer.device);
		this.offset = buffer.offset + offset;
		this.size = size;
		usage = buffer.usage;
		parent = buffer;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = buffer.memory;
			VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(device.getVk(), vk, memory.getVk(), buffer.offset + offset), "Failed to bind buffer memory");
		}
	}
	
	public Memory getMemory()
	{
		return memory;
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
	
	@Override
	public void close()
	{
		if (parent == null)
		{
			Debug.debugAllocation(() -> "Deallocated " + this);
			memory.close();
		}
		VK10.vkDestroyBuffer(device.getVk(), vk, null);
		super.close();
	}
	
	@Override
	public String toString()
	{
		return (name != null ? name + "{" : "VulkanBuffer{") + Debug.address(vk) + ", " + (offset != 0 ? "offset=" + offset + ", " : "") + size + " bytes, (" + DataUtils.getBufferUsage(usage) + "), mem=" + Debug.address(memory.getVk()) + "}";
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk", vk);
		Debug.print("Name", name);
		Debug.print("Offset: " + offset);
		Debug.print("Memory", memory);
		Debug.print("Size: " + size);
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
}
