package com.alcadrial.renderengine.render.buffer;

import java.util.LinkedList;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkMemoryRequirements;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.OutOfSpaceException;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class StaticBigBuffer extends BigVulkanBuffer {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private int remoteAllocationOffset;
	private int localAllocationOffset;
	protected LinkedList<Slot<? extends VulkanBuffer>> localSlots;
	protected LinkedList<Slot<? extends VulkanBuffer>> remoteSlots;
	
	public StaticBigBuffer(Device device, int size) throws VulkanException
	{
		super(device, size, 0);
		remoteAllocationOffset = 0;
		localSlots = new LinkedList<>();
		remoteSlots = new LinkedList<>();
	}
	
	@Override
	public VulkanBuffer allocate(int size, int usage) throws OutOfSpaceException, VulkanException
	{
		if (size == 0) throw new IllegalStateException("Size must be positive");
		
		VulkanBuffer bigBuffer;
		int allocationOffset;
		LinkedList<Slot<?>> slots;
		
		if (isLocalBufferingActive())
		{
			bigBuffer = localBigBuffer;
			allocationOffset = localAllocationOffset;
			slots = localSlots;
		}
		else
		{
			bigBuffer = remoteBigBuffer;
			allocationOffset = remoteAllocationOffset;
			slots = remoteSlots;
		}
		
		if (bigBuffer.size >= allocationOffset + size)
		{
			int offset = allocationOffset;
			allocationOffset += size;
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkMemoryRequirements requirements = VkMemoryRequirements.malloc(stack);
				VK10.vkGetBufferMemoryRequirements(bigBuffer.getDevice().getVk(), bigBuffer.getVk(), requirements);
				
				allocationOffset = (int) (Math.ceilDiv(allocationOffset, requirements.alignment()) * requirements.alignment());
			}
			usage |= isLocalBufferingActive() ? VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT : VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
			
			VulkanBuffer buffer = new VulkanBuffer(bigBuffer, offset, size, usage);
			Debug.debugAllocation(() -> "Allocate " + (isLocalBufferingActive() ? "Local" : "Remote") + " " + buffer);
			slots.addLast(new Slot<>(buffer));
			
			if (isLocalBufferingActive()) localAllocationOffset = allocationOffset;
			else remoteAllocationOffset = allocationOffset;
			return buffer;
		}
		throw new OutOfSpaceException("Missing " + (allocationOffset + size - bigBuffer.size) + " bytes of " + size + " requested (" + allocationOffset + "/" + bigBuffer.size + ")");
	}
	
	public VulkanBuffer getRemoteBuffer()
	{
		return remoteBigBuffer;
	}
	
	@Override
	public synchronized PhysicalDeviceTranferState startLocalBuffering() throws VulkanException
	{
		PhysicalDeviceTranferState t = super.startLocalBuffering();
		localAllocationOffset = 0;
		return t;
	}
	
	@Override
	public int getMaxAllocableSize()
	{
		if (isLocalBufferingActive()) return localBigBuffer.size - localAllocationOffset;
		return remoteBigBuffer.size - remoteAllocationOffset;
	}
	
	@Override
	public void sync()
	{
		
	}
	
	@Override
	public void clear()
	{
		if (isLocalBufferingActive())
		{
			localAllocationOffset = 0;
			for (Slot<? extends VulkanBuffer> slot : localSlots)
			{
				VulkanBuffer buffer = slot.getBuffer();
				Debug.debugAllocation(() -> "Deallocated Local " + buffer);
				buffer.close();
			}
			localSlots.clear();
		}
		else
		{
			remoteAllocationOffset = 0;
			for (Slot<? extends VulkanBuffer> slot : remoteSlots)
			{
				VulkanBuffer buffer = slot.getBuffer();
				Debug.debugAllocation(() -> "Deallocated Remote " + buffer);
				buffer.close();
			}
			remoteSlots.clear();
		}
	}
}