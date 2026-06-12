package com.alcadrial.kirry.render.buffer;

import java.nio.ByteBuffer;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.util.DataUtils;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class VulkanBuffer extends MemoryResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	/** Does not need to be closed */
	public static VulkanBuffer view(VulkanBuffer parent) throws VulkanException
	{
		return view(parent, parent.getSize());
	}
	
	/** Does not need to be closed */
	public static VulkanBuffer view(VulkanBuffer parent, int size) throws VulkanException
	{
		return view(parent, 0, size);
	}
	
	/** Does not need to be closed */
	public static VulkanBuffer view(VulkanBuffer parent, long offset, int size) throws VulkanException
	{
		return new VulkanBuffer(parent, parent.getVk(), offset, size, parent.usage, true);
	}
	
	public static VulkanBuffer asLocal(VulkanBuffer vulkanBuffer) throws VulkanException
	{
		return new VulkanBuffer(vulkanBuffer.device, vulkanBuffer.size, vulkanBuffer.usage | VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT);
	}
	
	public static VulkanBuffer ofUniformsBindings(Device device, int bindingsCount, int bindingSize) throws VulkanException
	{
		int minUniformBufferOffsetAlignment = (int) device.getPhysicalDevice().getLimits().minUniformBufferOffsetAlignment();
		int minSize = Math.ceilDiv(bindingSize, minUniformBufferOffsetAlignment) * minUniformBufferOffsetAlignment;
		return ofUniforms(device, minSize * bindingsCount, 0);
	}
	
	public static VulkanBuffer ofUniformsBindingsView(VulkanBuffer parent, int bindingsOffset, int bindingsCount, int bindingSize) throws VulkanException
	{
		int minUniformBufferOffsetAlignment = (int) parent.getDevice().getPhysicalDevice().getLimits().minUniformBufferOffsetAlignment();
		int minSize = Math.ceilDiv(bindingSize, minUniformBufferOffsetAlignment) * minUniformBufferOffsetAlignment;
		return view(parent, minSize * bindingsOffset, minSize * bindingsCount);
	}
	
	public static VulkanBuffer ofUniforms(Device device, int size) throws VulkanException
	{
		return ofUniforms(device, size, 0);
	}
	
	public static VulkanBuffer ofUniforms(Device device, int size, int extraUsage) throws VulkanException
	{
		return new VulkanBuffer(device, size, extraUsage | VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
	}
	
	public static VulkanBuffer forTransfer(Device device, int size) throws VulkanException
	{
		return new VulkanBuffer(device, size, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT);
	}
	
	private @Nullable VulkanBuffer parent;
	
	protected final long offset;
	protected final int size;
	protected final int usage;
	protected final boolean isView;
	
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
		this(device, size, usage, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
	}
	
	public VulkanBuffer(Device device, int size, int usage, int requiredProperties, int optimalProperties) throws VulkanException
	{
		long vk;
		Memory memory;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = device.createBuffer(stack, size, usage);
			memory = device.allocateBufferMemory(stack, vk, requiredProperties, optimalProperties);
			Debug.debugCreate(() -> "Created " + Debug.address(vk));
			Debug.debugAllocation(() -> "Allocated " + Debug.address(vk));
		}
		super(device, memory);
		this.vk = vk;
		offset = 0;
		this.size = size;
		this.usage = usage;
		isView = false;
		bindMemory();
		if ((requiredProperties & VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) == VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) memory.mapMemory();
	}
	
	/*
	public VulkanBuffer(MemoryResource buffer, long offset, int size, int usage) throws VulkanException
	{
		long vk;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = buffer.getDevice().createBuffer(stack, size, usage);
			Debug.debugCreate(() -> "Created " + Debug.address(vk));
		}
		super(buffer.getDevice(), buffer.memory);
		this.vk = vk;
		this.offset = offset;
		this.size = size;
		this.usage = usage;
		parent = buffer;
		isView = false;
		bindMemory();
	}*/
	
	public VulkanBuffer(Memory memory, long vk, long offset, int size, int usage) throws VulkanException
	{
		super(memory.getDevice(), memory);
		this.vk = vk;
		this.offset = offset;
		this.size = size;
		this.usage = usage;
		isView = false;
		bindMemory();
	}
	
	public VulkanBuffer(Memory memory, long offset, int size, int usage) throws VulkanException
	{
		long vk;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = memory.getDevice().createBuffer(stack, size, usage);
			Debug.debugCreate(() -> "Created " + Debug.address(vk));
		}
		super(memory.getDevice(), memory);
		this.vk = vk;
		this.offset = offset;
		this.size = size;
		this.usage = usage;
		isView = false;
		bindMemory();
	}
	
	public VulkanBuffer(VulkanBuffer buffer, long offset, int size, int usage) throws VulkanException
	{
		long vk;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vk = buffer.getDevice().createBuffer(stack, size, usage);
			Debug.debugCreate(() -> "Created " + Debug.address(vk));
		}
		super(buffer.getDevice(), buffer.memory);
		this.vk = vk;
		this.offset = buffer.offset + offset;
		this.size = size;
		this.usage = usage;
		parent = buffer;
		isView = false;
		bindMemory();
	}
	
	public VulkanBuffer(VulkanBuffer buffer, long offset, int size) throws VulkanException
	{
		this(buffer, offset, size, buffer.usage);
	}
	
	public VulkanBuffer(VulkanBuffer buffer) throws VulkanException
	{
		this(buffer, buffer.offset, buffer.size, buffer.usage);
	}
	
	protected VulkanBuffer(/*MemoryResource*/VulkanBuffer parent, long vk, long offset, int size, int usage, boolean isView) throws VulkanException
	{
		super(parent.getDevice(), parent.getMemory());
		this.offset = offset;
		this.size = size;
		this.usage = usage;
		this.parent = parent;
		if (isView) super.close();
		this.isView = isView;
		this.vk = vk;
	}
	
	protected void bindMemory() throws VulkanException
	{
		bindMemory(offset);
	}
	
	protected void bindMemory(long offset) throws VulkanException
	{
		bindMemory(memory, offset);
	}
	
	protected void bindMemory(Memory memory, long offset) throws VulkanException
	{
		VulkanErrorUtils.assertNoError(VK10.vkBindBufferMemory(device.getVk(), vk, memory.getVk(), offset), "Failed to bind buffer memory");
	}
	
	@Override
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
	
	public ByteBuffer asBuffer()
	{
		long offset = this.offset;
		for (VulkanBuffer buffer = parent; buffer != null; buffer = buffer.parent) offset += buffer.offset;
		ByteBuffer buffer = VulkanErrorUtils.nonNull(memory.getMapping(), "Memory is not mapped").getBuffer();
		return buffer.slice((int) offset, size).order(buffer.order());
	}
	
	@Override
	public void close()
	{
		if (!isView)
		{
			if (parent == null)
			{
				Debug.debugAllocation(() -> "Deallocated " + Debug.address(vk));
				if (memory.getVk() != 0) memory.close();
			}
			Debug.debugClose(() -> "Closing VulkanBuffer " + Debug.address(vk));
			VK10.vkDestroyBuffer(device.getVk(), vk, null);
			super.close();
		}
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
		if (isView) Debug.print("IsView: true");
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
}
