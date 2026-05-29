package com.alcadrial.renderengine.render.memory;

import java.util.ConcurrentModificationException;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.utility.LazyIterate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkMemoryRequirements;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.buffer.Memory;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.exception.VulkanRuntimeException;

public class DefaultMemoryManager implements MemoryManager {
	
	private final MemoryType<StackSegment> stackMemory;
	private final MemoryType<DynamicSegment> dynamicMemory;
	private final MemoryType<CacheSegment> cacheMemory;
	private final ImmutableMemoryType<HostSegment> hostMemory;
	
	private final Device device;
	private @Nullable String name;
	
	public DefaultMemoryManager(Device device, int minStackSize, int minDynamicSize, int minCacheSize, int minHostSize)
	{
		this.device = device;
		stackMemory = new MemoryType<>(device, minStackSize, VK14.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, StackSegment::new);
		dynamicMemory = new MemoryType<>(device, minDynamicSize, VK14.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, DynamicSegment::new);
		cacheMemory = new MemoryType<>(device, minCacheSize, VK14.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, CacheSegment::new);
		hostMemory = new ImmutableMemoryType<>(device, minHostSize, VK14.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK14.VK_MEMORY_PROPERTY_HOST_CACHED_BIT, HostSegment::new);
	}
	
	@Override
	public VulkanBuffer allocate(int size, int usage) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long bufferVk = device.createBuffer(stack, size, usage);
			StackSegment segment = stackMemory.makeSegment(bufferVk);
			VulkanBuffer buffer = segment.bind(bufferVk, size, usage);
			if (buffer == null) throw new ConcurrentModificationException();
			return buffer;
		}
	}
	
	@Override
	public VulkanBuffer subAllocate(VulkanBuffer parent, int offset, int size, int usage) throws VulkanException
	{
		return new VulkanBuffer(parent, offset, size, usage);
	}
	
	@Override
	public MemoryTransferState allocateForTransfer(CommandBuffer commandBuffer, int size) throws VulkanException
	{
		HostSegment segment = hostMemory.makeSegment(size, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		
		return new StackMemoryTransferState(segment.buffer, commandBuffer) {
			
			@Override
			protected void free() throws VulkanException
			{
				hostMemory.release(segment);
			}
		};
	}
	
	@Override
	public VulkanBuffer allocateCache(int size, int usage) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long bufferVk = device.createBuffer(stack, size, usage);
			CacheSegment segment = cacheMemory.makeSegment(bufferVk);
			VulkanBuffer buffer = segment.bind(bufferVk, size, usage);
			if (buffer == null) throw new ConcurrentModificationException();
			return buffer;
		}
	}
	
	@Override
	public VulkanBuffer allocateSingleUse(int size, int usage) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long bufferVk = device.createBuffer(stack, size, usage);
			DynamicSegment segment = dynamicMemory.makeSegment(bufferVk);
			VulkanBuffer buffer = segment.bind(bufferVk, size, usage);
			if (buffer == null) throw new ConcurrentModificationException();
			return buffer;
		}
	}
	
	@Override
	public void close()
	{
		stackMemory.close();
		dynamicMemory.close();
		cacheMemory.close();
		hostMemory.close();
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
		stackMemory.setName(name + " Stack");
		dynamicMemory.setName(name + "Dynamic");
		cacheMemory.setName(name + " Cache");
		hostMemory.setName(name + " Host");
	}
	
	protected interface Segment extends AutoCloseable {
		
		Memory getMemory();
		int getFreeSize();
		@Override
		void close();
	}
	
	protected static abstract class AbstractSegment implements Segment {
		
		protected final Memory memory;
		
		protected AbstractSegment(Memory memory)
		{
			this.memory = memory;
		}
		
		@Override
		public Memory getMemory()
		{
			return memory;
		}
		
		@Override
		public void close()
		{
			memory.close();
		}
	}
	
	protected static class StackSegment extends AbstractSegment {
		
		private int offset;
		
		public StackSegment(Memory memory, long bufferVk, long size)
		{
			super(memory);
			offset = 0;
		}
		
		public @Nullable VulkanBuffer bind(long vk, int size, int usage) throws VulkanException
		{
			int offset = this.offset;
			if (getFreeSize() < size) return null;
			try
			{
				return new VulkanBuffer(memory, vk, offset, size, usage);
			}
			finally
			{
				this.offset += size;
			}
		}
		
		public void deallocate(int size)
		{
			offset -= size;
		}
		
		@Override
		public int getFreeSize()
		{
			return memory.getSize() - offset;
		}
	}
	
	protected static class DynamicSegment extends AbstractSegment {
		
		protected class Slot {
			
			int x;
			int w;
			
			public Slot(int x, int w)
			{
				this.x = x;
				this.w = w;
			}
		}
		
		protected NavigableSet<Slot> slotsW;
		protected NavigableSet<Slot> slotsX;
		protected int freeArea;
		
		public DynamicSegment(Memory memory, long bufferVk, long size)
		{
			super(memory);
			slotsX = new TreeSet<>((s0, s1) -> {
				int c = Integer.compare(s0.x, s1.x);
				if (c != 0) return c;
				return Integer.compare(s0.w, s1.w);
			});
			slotsW = new TreeSet<>((s0, s1) -> {
				int c = Integer.compare(s0.w, s1.w);
				if (c != 0) return c;
				return Integer.compare(s0.x, s1.x);
			});
			freeArea = memory.getSize();
		}
		
		public @Nullable VulkanBuffer bind(long vk, int size, int usage) throws VulkanException
		{
			if (size > freeArea) return null;
			SortedSet<Slot> tail = slotsW.tailSet(new Slot(0, size));
			Slot candidate = tail.first();
			if (candidate == null) return null;
			
			VulkanBuffer vulkanBuffer = new VulkanBuffer(memory, vk, candidate.x, size, usage);
			tail.removeFirst();
			int remaining = candidate.w - size;
			if (remaining > 0)
			{
				Slot newSlot = new Slot(candidate.x + size, remaining);
				slotsW.add(newSlot);
				slotsX.add(newSlot);
			}
			
			return vulkanBuffer;
		}
		
		public void deallocate(VulkanBuffer buffer)
		{
			Slot newSlot = new Slot((int) buffer.getOffset(), buffer.getSize());
			Slot higher = slotsX.higher(newSlot);
			Slot lower = slotsX.lower(newSlot);
			
			int mergeCount = 0;
			
			if (higher != null && newSlot.x + newSlot.w == higher.x)
			{
				slotsW.remove(higher);
				newSlot.w += higher.w;
				
				mergeCount += 1;
			}
			
			if (lower != null && lower.x + lower.w == newSlot.x)
			{
				slotsW.remove(lower);
				lower.w += newSlot.w;
				
				mergeCount += 2;
			}
			
			switch (mergeCount)
			{
				case 1 -> slotsW.add(higher);
				case 2 -> slotsW.add(lower);
				case 3 -> {
					((@NonNull Slot) lower).w += ((@NonNull Slot) higher).w;
					slotsW.add(lower);
				}
			}
		}
		
		@Override
		public int getFreeSize()
		{
			return freeArea;
		}
	}
	
	protected static class CacheSegment extends DynamicSegment {
		
		public CacheSegment(Memory memory, long bufferVk, long size)
		{
			super(memory, bufferVk, size);
		}
		
		@Override
		public @Nullable VulkanBuffer bind(long vk, int size, int usage) throws VulkanException
		{
			VulkanBuffer result = super.bind(vk, size, usage);
			if (result != null) return result;
			if (!makeContinuousSpace(size)) return null;
			return bind(vk, size, usage);
		}
		
		protected boolean makeContinuousSpace(int size)
		{
			//TODO CacheSegment#makeContinuousSpace
			return false;
		}
	}
	
	protected static class HostSegment extends StackSegment {
		
		protected final VulkanBuffer buffer;
		
		public HostSegment(Memory memory, long bufferVk, long size) throws VulkanException
		{
			super(memory, bufferVk, size);
			buffer = new VulkanBuffer(memory, bufferVk, 0, (int) size, 0);
			memory.mapMemory();
		}
	}
	
	protected static class MemoryType<S extends Segment> implements AutoCloseable {
		
		@FunctionalInterface
		public interface Allocation<S extends Segment> {
			
			S allocate(Memory memory, long bufferVk, long size) throws VulkanException;
		}
		
		private final Allocation<S> allocationFactory;
		
		protected final Device device;
		protected final MutableList<S> segments;
		protected final int properties;
		protected int freeSegments;
		protected int minSegmentSize;
		protected long maxCurrentSegmentSize;
		private @Nullable String name;
		
		protected MemoryType(Device device, int minSegmentSize, int properties, Allocation<S> allocationFactory)
		{
			this.allocationFactory = allocationFactory;
			this.device = device;
			segments = Lists.mutable.of();
			freeSegments = 0;
			this.minSegmentSize = minSegmentSize;
			maxCurrentSegmentSize = 0;
			this.properties = properties;
		}
		
		public S makeSegment(long bufferVk) throws VulkanException
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkMemoryRequirements requirements = device.getBufferRequirements(stack, bufferVk);
				long allocationSize = requirements.size();
				
				if (allocationSize > maxCurrentSegmentSize)
				{
					Memory memory = device.allocateMemory(stack, requirements, properties, 0);
					memory.setName(name + " " + segments.size());
					return allocateSegment(memory, bufferVk, allocationSize, true);
				}
				
				try
				{
					return LazyIterate.take(segments, freeSegments)
						.detectOptional(s -> s.getFreeSize() >= allocationSize && (s.getMemory().getMemoryTypeBits() & requirements.memoryTypeBits()) == requirements.memoryTypeBits())
						.orElseGet((() -> {
							try
							{
								Memory memory = device.allocateMemory(stack, requirements, properties, 0);
								memory.setName(name + " " + segments.size());
								return allocateSegment(memory, bufferVk, allocationSize, true);
							}
							catch (VulkanException e)
							{
								throw new VulkanRuntimeException(e);
							}
						}));
				}
				catch (VulkanRuntimeException e)
				{
					throw e.getCause();
				}
			}
		}
		
		public S allocateSegment(Memory memory, long bufferVk, long size, boolean autoConsume) throws VulkanException
		{
			size = Math.max(size, minSegmentSize);
			S segment = allocationFactory.allocate(memory, bufferVk, size);
			
			if (!autoConsume)
			{
				segments.addFirst(segment);
				freeSegments++;
			}
			else segments.addLast(segment);
			
			if (maxCurrentSegmentSize < size) maxCurrentSegmentSize = size;
			return segment;
		}
		
		public void release(S segment)
		{
			int i = segments.detectIndex(s -> s == segment);
			if (i < 0) return;
			segments.add(segments.remove(i));
		}
		
		@Override
		public void close()
		{
			for (S s : segments) s.close();
		}
		
		public void setName(String name)
		{
			this.name = name;
			int i = 0;
			for (S s : segments) s.getMemory().setName(name + " " + i++);
		}
	}
	
	protected static class ImmutableMemoryType<S extends Segment> implements AutoCloseable {
		
		@FunctionalInterface
		public interface Allocation<S extends Segment> {
			
			S allocate(Memory memory, long bufferVk, long size) throws VulkanException;
		}
		
		private final Allocation<S> allocationFactory;
		
		protected final Device device;
		protected final MutableList<S> segments;
		protected final int properties;
		protected int freeSegments;
		protected int minSegmentSize;
		protected long maxCurrentSegmentSize;
		private @Nullable String name;
		
		protected ImmutableMemoryType(Device device, int minSegmentSize, int properties, Allocation<S> allocationFactory)
		{
			this.allocationFactory = allocationFactory;
			this.device = device;
			segments = Lists.mutable.of();
			freeSegments = 0;
			this.minSegmentSize = minSegmentSize;
			maxCurrentSegmentSize = 0;
			this.properties = properties;
		}
		
		public S makeSegment(int size, int usage) throws VulkanException
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				int effectiveSize = Math.max(size, minSegmentSize);
				
				if (effectiveSize > maxCurrentSegmentSize)
				{
					long bufferVk = device.createBuffer(stack, effectiveSize, usage);
					Memory memory = device.allocateBufferMemory(stack, bufferVk, properties, 0);
					memory.setName(name + " " + segments.size());
					return allocateSegment(memory, bufferVk, effectiveSize, true);
				}
				
				try
				{
					return LazyIterate.take(segments, freeSegments)
						.detectOptional(s -> s.getFreeSize() >= effectiveSize)
						.orElseGet((() -> {
							try
							{
								long bufferVk = device.createBuffer(stack, effectiveSize, usage);
								Memory memory = device.allocateBufferMemory(stack, bufferVk, properties, 0);
								memory.setName(name + " " + segments.size());
								return allocateSegment(memory, bufferVk, effectiveSize, true);
							}
							catch (VulkanException e)
							{
								throw new VulkanRuntimeException(e);
							}
						}));
				}
				catch (VulkanRuntimeException e)
				{
					throw e.getCause();
				}
			}
		}
		
		public S allocateSegment(Memory memory, long bufferVk, long size, boolean autoConsume) throws VulkanException
		{
			S segment = allocationFactory.allocate(memory, bufferVk, size);
			
			if (!autoConsume)
			{
				segments.addFirst(segment);
				freeSegments++;
			}
			else segments.addLast(segment);
			
			if (maxCurrentSegmentSize < size) maxCurrentSegmentSize = size;
			return segment;
		}
		
		public void release(S segment)
		{
			int i = segments.detectIndex(s -> s == segment);
			if (i < 0) return;
			segments.add(segments.remove(i));
		}
		
		@Override
		public void close()
		{
			for (S s : segments) s.close();
		}
		
		public void setName(String name)
		{
			this.name = name;
			int i = 0;
			for (S s : segments) s.getMemory().setName(name + " " + i++);
		}
	}
}
