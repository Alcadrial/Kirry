package com.alcadrial.renderengine.render.buffer;

import java.util.LinkedList;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkMemoryRequirements;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.OverflowException;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class StaticBigBuffer extends AbstractBigVulkanBuffer<StaticBigBuffer.Data> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static class Data extends AbstractBigVulkanBuffer.Data {
		
		protected int allocationOffset;
		protected LinkedList<Slot<? extends VulkanBuffer>> slots;
		
		public Data(VulkanBuffer buffer, int allocationOffset, LinkedList<AbstractBigVulkanBuffer<Data>.Slot<? extends VulkanBuffer>> slots)
		{
			super(buffer);
			this.allocationOffset = allocationOffset;
			this.slots = slots;
		}
	}
	
	public StaticBigBuffer(Device device, int size) throws VulkanException
	{
		super(device, size, 0);
	}
	
	@Override
	protected Data allocateData(VulkanBuffer buffer)
	{
		return new Data(buffer, 0, new LinkedList<>());
	}
	
	@Override
	public VulkanBuffer allocate(int size, int usage) throws OverflowException, VulkanException
	{
		if (size == 0) throw new IllegalStateException("Size must be positive");
		
		Data data = getActiveData();
		int allocationOffset = data.allocationOffset;
		VulkanBuffer bigBuffer = data.buffer;
		
		if (bigBuffer.size >= allocationOffset + size)
		{
			usage |= isLocalBufferingActive() ? VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT : VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
			long bufferVk;
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				bufferVk = device.createBuffer(stack, size, usage);
				VkMemoryRequirements requirements = VkMemoryRequirements.malloc(stack);
				VK10.vkGetBufferMemoryRequirements(bigBuffer.getDevice().getVk(), bufferVk, requirements);
				
				allocationOffset = (int) (Math.ceilDiv(allocationOffset, requirements.alignment()) * requirements.alignment());
			}
			
			VulkanBuffer buffer = new VulkanBuffer(bigBuffer, bufferVk, allocationOffset, size, usage, false);
			Debug.debugAllocation(() -> "Allocate " + (isLocalBufferingActive() ? "Local" : "Remote") + " " + buffer);
			buffer.bindMemory();
			data.slots.addLast(new Slot<>(buffer));
			
			allocationOffset += size;
			data.allocationOffset = allocationOffset;
			return buffer;
		}
		throw new OverflowException("Missing " + (allocationOffset + size - bigBuffer.size) + " bytes of " + size + " requested (" + allocationOffset + "/" + bigBuffer.size + ")");
	}
	
	@Override
	public int getMaxAllocableSize()
	{
		Data data = getActiveData();
		return data.buffer.size - data.allocationOffset;
	}
	
	@Override
	public void sync()
	{}
	
	@Override
	public void clear()
	{
		Data data = getActiveData();
		if (data == null) return;
		data.allocationOffset = 0;
		for (Slot<? extends VulkanBuffer> slot : data.slots)
		{
			VulkanBuffer buffer = slot.getBuffer();
			Debug.debugAllocation(() -> "Deallocated " + (isLocalBufferingActive() ? "Local " : "Remote ") + buffer + " of " + this);
			buffer.close();
		}
		data.slots.clear();
	}
}