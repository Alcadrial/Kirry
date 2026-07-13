package com.alcadrial.kirry.render.buffer;

import java.nio.ByteBuffer;

import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.util.RawDeviceResource;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class Memory extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public class Mapping implements AutoCloseable {
		
		private long offset;
		private int size;
		private ByteBuffer buffer;
		private boolean valid;
		
		public Mapping(long offset, int size, ByteBuffer buffer)
		{
			this.offset = offset;
			this.size = size;
			this.buffer = buffer;
			valid = true;
		}
		
		public ByteBuffer getBuffer()
		{
			if (!valid) return buffer.asReadOnlyBuffer().order(buffer.order());
			return buffer;
		}
		
		@Override
		public void close()
		{
			unmapMemory();
		}
	}
	
	private final int size;
	private final int memoryTypeBits;
	private final int propertyFlags;
	private final int index;
	private @Nullable Mapping mapping;
	
	public Memory(Device device, long vk, int size, int memoryTypeBits, int propertyFlags, int index)
	{
		super(device);
		this.vk = vk;
		this.size = size;
		this.propertyFlags = propertyFlags;
		this.memoryTypeBits = memoryTypeBits;
		this.index = index;
	}
	
	public @Nullable Mapping getMapping()
	{
		return mapping;
	}
	
	public Mapping mapMemory() throws VulkanException
	{
		return mapMemory(0, size);
	}
	
	public Mapping mapMemory(long offset, int size) throws VulkanException
	{
		if (mapping != null) throw new IllegalStateException("Already mapped memory " + Debug.address(vk));
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer gpuData = stack.mallocPointer(1);
			VulkanErrorUtils.assertNoError(VK10.vkMapMemory(device.getVk(), vk, offset, size, 0, gpuData), "Failed to map buffer memory");
			ByteBuffer buffer = gpuData.getByteBuffer(size);
			return mapping = new Mapping(offset, size, buffer);
		}
	}
	
	public Mapping remapMemory(long offset, int size) throws VulkanException
	{
		Mapping mapping = this.mapping;
		if (mapping == null) throw new IllegalStateException("Missing mapped memory " + Debug.address(vk));
		if (mapping.offset == offset && mapping.size == size) return mapping;
		mapping.valid = false;
		VK10.vkUnmapMemory(device.getVk(), vk);
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer gpuData = stack.mallocPointer(1);
			VulkanErrorUtils.assertNoError(VK10.vkMapMemory(device.getVk(), vk, offset, size, 0, gpuData), "Failed to map buffer memory");
			ByteBuffer buffer = gpuData.getByteBuffer(size);
			return mapping = new Mapping(offset, size, buffer);
		}
	}
	
	public void unmapMemory()
	{
		Mapping mapping = this.mapping;
		if (mapping != null)
		{
			mapping.valid = false;
			VK10.vkUnmapMemory(device.getVk(), vk);
			this.mapping = null;
		}
	}
	
	public int getSize()
	{
		return size;
	}
	
	public int getMemoryTypeBits()
	{
		return memoryTypeBits;
	}
	
	public int getPropertyFlags()
	{
		return propertyFlags;
	}
	
	public int getMemoryIndex()
	{
		return index;
	}
	
	public boolean isHostCoherent()
	{
		return (propertyFlags & VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT) != 0;
	}
	
	@Override
	public void close()
	{
		unmapMemory();
		VK10.vkFreeMemory(device.getVk(), vk, null);
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk", vk);
		Debug.print("Name", name);
		Debug.print("Mapped: " + (mapping != null));
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
		device.setObjectDebugName(this, name);
	}
}
