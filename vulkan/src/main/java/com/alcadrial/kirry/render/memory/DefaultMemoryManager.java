package com.alcadrial.kirry.render.memory;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.utility.LazyIterate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkMemoryRequirements;

import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.buffer.Memory;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.exception.VulkanRuntimeException;

public class DefaultMemoryManager implements MemoryManager {
	
	private final MutableMemoryType<StackSegment> stackMemory;
	private final MutableMemoryType<DynamicSegment> dynamicMemory;
	private final MutableMemoryType<CacheSegment> cacheMemory;
	private final ImmutableMemoryType<HostSegment> hostMemory;
	
	private final Device device;
	
	public DefaultMemoryManager(Device device, int minStackSize, int minDynamicSize, int minCacheSize, int minHostSize)
	{
		this.device = device;
		stackMemory = new MutableMemoryType<>(device, minStackSize, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, StackSegment::new);
		dynamicMemory = new MutableMemoryType<>(device, minDynamicSize, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, DynamicSegment::new);
		cacheMemory = new MutableMemoryType<>(device, minCacheSize, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, CacheSegment::new);
		hostMemory = new ImmutableMemoryType<>(device, minHostSize, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT, HostSegment::new);
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
			if (stackMemory.name != null)
			{
				buffer.setName(stackMemory.name + " Buffer " + segment.index + " " + segment.nextId());
			}
			return buffer;
		}
	}
	
	@Override
	public VulkanBuffer allocateHost(int size, int usage) throws VulkanException
	{
		HostSegment segment = hostMemory.makeSegment(size, usage);
		if (hostMemory.name != null)
		{
			segment.buffer.setName(hostMemory.name + " Buffer " + segment.index);
		}
		return segment.buffer;
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
		
		if (hostMemory.name != null)
		{
			segment.buffer.setName(hostMemory.name + " Buffer " + segment.index);
		}
		return new StackMemoryTransferState(segment.buffer, commandBuffer) {
			
			@Override
			protected void free() throws VulkanException
			{
				hostMemory.release(segment);
			}
		};
	}
	
	@Override
	public MemoryCacheState allocateCache(int size, int usage) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long bufferVk = device.createBuffer(stack, size, usage);
			CacheSegment segment = cacheMemory.makeSegment(bufferVk);
			MemoryCacheState cache = segment.bindCache(bufferVk, size, usage);
			if (cache == null) throw new ConcurrentModificationException();
			if (cacheMemory.name != null)
			{
				cache.getBuffer().setName(cacheMemory.name + " Buffer " + segment.index + " " + segment.size());
			}
			return cache;
		}
	}
	
	@Override
	public MemoryCacheBulkState allocateCacheAll(int[] sizes, int usage, int bulkUsage) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] vks = new long[sizes.length];
			int[] newSizes = new int[sizes.length];
			for (int i = 0; i < sizes.length; i++)
			{
				vks[i] = device.createBuffer(stack, sizes[i], usage);
				newSizes[i] = (int) device.getBufferRequirements(stack, vks[i]).size();
			}
			int size = 0;
			for (int i = 0; i < newSizes.length; i++) size += newSizes[i];
			long bulkVk = device.createBuffer(stack, size, bulkUsage);
			size = (int) device.getBufferRequirements(stack, bulkVk).size();
			
			CacheSegment segment = cacheMemory.makeSegment(bulkVk);
			MemoryCacheBulkState cache = segment.bindBulkCache(bulkVk, size, bulkUsage, vks, newSizes, usage);
			if (cache == null) throw new ConcurrentModificationException();
			
			if (cacheMemory.name != null)
			{
				int k = -sizes.length;
				for (MemoryCacheState memoryCacheState : cache)
				{
					memoryCacheState.getBuffer().setName(cacheMemory.name + " Buffer " + segment.index + " " + (k++ + segment.size()));
				}
			}
			return cache;
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
			if (dynamicMemory.name != null) buffer.setName(dynamicMemory.name + " Buffer " + segment.index + " " + segment.size());
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
		protected final MemoryType<?> type;
		protected final int index;
		
		protected AbstractSegment(MemoryType<?> type, Memory memory, int index)
		{
			this.type = type;
			this.memory = memory;
			this.index = index;
		}
		
		@Override
		public Memory getMemory()
		{
			return memory;
		}
		
		@SuppressWarnings("unchecked")
		protected void release()
		{
			release0((MemoryType<AbstractSegment>) type, this);
		}
		
		private static <S extends Segment> void release0(MemoryType<S> type, S s)
		{
			type.release(s);
		}
		
		@Override
		public void close()
		{
			memory.close();
		}
	}
	
	protected static class StackSegment extends AbstractSegment {
		
		private class VB extends VulkanBuffer {
			
			public VB(Memory memory, long vk, long offset, int size, int usage) throws VulkanException
			{
				super(memory, vk, offset, size, usage);
			}
			
			@Override
			public void close()
			{
				deallocate(size);
				deallocateVk();
			}
		}
		
		private int offset;
		private int count;
		
		public StackSegment(MemoryType<?> type, Memory memory, long bufferVk, long size, int index)
		{
			super(type, memory, index);
			offset = 0;
			count = -1;
		}
		
		public int nextId()
		{
			return count;
		}

		public @Nullable VulkanBuffer bind(long vk, int size, int usage) throws VulkanException
		{
			int offset = this.offset;
			if (getFreeSize() < size) return null;
			try
			{
				return new VB(memory, vk, offset, size, usage);
			}
			finally
			{
				this.offset += size;
				count++;
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
		
		protected static class Slot {
			
			int x;
			int w;
			
			public Slot(int x, int w)
			{
				this.x = x;
				this.w = w;
			}
		}
		
		private class VB extends VulkanBuffer {
			
			public VB(Memory memory, long vk, long offset, int size, int usage) throws VulkanException
			{
				super(memory, vk, offset, size, usage);
			}
			
			@Override
			public void close()
			{
				deallocate(this);
				deallocateVk();
			}
		}
		
		protected NavigableSet<Slot> slotsW;
		protected NavigableSet<Slot> slotsX;
		protected int freeArea;
		protected int count;
		
		public DynamicSegment(MemoryType<?> type, Memory memory, long bufferVk, long size, int index)
		{
			super(type, memory, index);
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
			Slot newSlot = new Slot(0, freeArea);
			slotsX.add(newSlot);
			slotsW.add(newSlot);
			
			count = 0;
		}
		
		public int size()
		{
			return count;
		}
		
		public @Nullable VulkanBuffer bind(long vk, int size, int usage) throws VulkanException
		{
			if (size > freeArea) return null;
			SortedSet<Slot> tail = slotsW.tailSet(new Slot(0, size));
			if (tail.isEmpty()) return null;
			Slot candidate = tail.first();
			
			VulkanBuffer vulkanBuffer = new VB(memory, vk, candidate.x, size, usage);
			tail.removeFirst();
			int remaining = candidate.w - size;
			if (remaining > 0)
			{
				Slot newSlot = new Slot(candidate.x + size, remaining);
				slotsW.add(newSlot);
				slotsX.add(newSlot);
			}
			count++;
			
			return vulkanBuffer;
		}
		
		/**
		 * 
		 * @param bulkVk
		 * @param bulkUsage
		 * @param size -1 means unknown
		 * @param vks
		 * @param sizes
		 * @param usage
		 * @throws VulkanException
		 */
		public VulkanBuffer @Nullable [] bulkBind(long bulkVk, int size, int bulkUsage, long[] vks, int[] sizes, int usage) throws VulkanException
		{
			if (size == -1)
			{
				size = 0;
				for (int i = 0; i < sizes.length; i++) size += sizes[i];
			}
			if (size > freeArea) return null;
			
			SortedSet<Slot> tail = slotsW.tailSet(new Slot(0, size));
			if (tail.isEmpty()) return null;
			Slot candidate = tail.first();
			
			VulkanBuffer[] buffers = new VulkanBuffer[sizes.length + 1];
			for (int i = 0, x = candidate.x; i < sizes.length; x += sizes[i], i++) buffers[i] = new VB(memory, vks[i], x, sizes[i], usage);
			buffers[sizes.length] = new VB(memory, bulkVk, candidate.x, size, bulkUsage);
			tail.removeFirst();
			int remaining = candidate.w - size;
			if (remaining > 0)
			{
				Slot newSlot = new Slot(candidate.x + size, remaining);
				slotsW.add(newSlot);
				slotsX.add(newSlot);
			}
			count += sizes.length;
			
			return buffers;
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
				default -> {}
			}
			count--;
		}
		
		@Override
		public int getFreeSize()
		{
			return freeArea;
		}
	}
	
	protected static class CacheSegment extends DynamicSegment {
		
		private static class CacheState implements MemoryCacheState {
			
			private VulkanBuffer buffer;
			private boolean isValid;
			private AtomicInteger usageCount;
			
			public CacheState(VulkanBuffer buffer)
			{
				this.buffer = buffer;
				isValid = true;
				usageCount = new AtomicInteger(1);
			}
			
			@Override
			public VulkanBuffer getBuffer()
			{
				return buffer;
			}
			
			@Override
			public boolean isValid()
			{
				return isValid;
			}
			
			@Override
			public void acquire()
			{
				usageCount.incrementAndGet();
			}
			
			@Override
			public void release()
			{
				usageCount.decrementAndGet();
			}
		}
		
		static class CacheBulkState implements MemoryCacheBulkState {
			
			private @Nullable VulkanBuffer buffer;
			private MemoryCacheState[] states;
			
			public CacheBulkState(@Nullable VulkanBuffer buffer, MemoryCacheState[] states)
			{
				this.buffer = buffer;
				this.states = states;
			}
			
			@Override
			public @Nullable VulkanBuffer getBuffer()
			{
				return buffer;
			}
			
			@Override
			public MemoryCacheState getState(int index)
			{
				return states[index];
			}
			
			@Override
			public boolean isContinuous()
			{
				return buffer != null;
			}
			
			@Override
			public Iterator<MemoryCacheState> iterator()
			{
				return Arrays.asList(states).iterator();
			}
		}
		
		private TreeMap<Long, CacheState> allocations;
		
		public CacheSegment(MemoryType<?> type, Memory memory, long bufferVk, long size, int index)
		{
			super(type, memory, bufferVk, size, index);
			allocations = new TreeMap<>();
		}
		
		public @Nullable CacheState bindCache(long vk, int size, int usage) throws VulkanException
		{
			VulkanBuffer result = bind(vk, size, usage);
			if (result != null)
			{
				CacheState cache = new CacheState(result);
				allocations.put(result.getOffset(), cache);
				return cache;
			}
			if (!makeContinuousSpace(size)) return null;
			result = bind(vk, size, usage);
			if (result == null) return null;
			
			CacheState cache = new CacheState(result);
			allocations.put(result.getOffset(), cache);
			return cache;
		}
		
		//TODO: allocate buffers in blocks and return then ordered by offset (partial continuous memory)
		public @Nullable MemoryCacheBulkState bindBulkCache(long bulkVk, int size, int bulkUsage, long[] vks, int[] sizes, int usage) throws VulkanException
		{
			CacheState[] caches = new CacheState[sizes.length];
			VulkanBuffer[] result = bulkBind(bulkVk, size, bulkUsage, vks, sizes, usage);
			if (result != null)
			{
				for (int i = 0; i < sizes.length; i++)
				{
					CacheState cache = new CacheState(result[i]);
					allocations.put(result[i].getOffset(), cache);
					caches[i] = cache;
				}
				return new CacheBulkState(result[sizes.length], caches);
			}
			for (int i = 0; i < sizes.length; i++)
			{
				CacheState cache = bindCache(vks[i], sizes[i], usage);
				if (cache == null)
				{
					for (int j = i - 1; j >= 0; j--) caches[j].release();
					return null;
				}
				allocations.put(cache.getBuffer().getOffset(), cache);
				caches[i] = cache;
			}
			return new CacheBulkState(null, caches);
		}
		
		protected boolean makeContinuousSpace(int size)
		{
			//TODO CacheSegment#makeContinuousSpace
			return false;
		}
	}
	
	protected static class HostSegment extends StackSegment {
		
		private class VB extends VulkanBuffer {
			
			public VB(Memory memory, long vk, long offset, int size, int usage) throws VulkanException
			{
				super(memory, vk, offset, size, usage);
			}
			
			@Override
			public void close()
			{
				release();
			}
			
			public void closeSegment()
			{
				deallocateVk();
			}
		}
		
		protected final VB buffer;
		
		public HostSegment(MemoryType<HostSegment> type, Memory memory, long bufferVk, long size, int index) throws VulkanException
		{
			super(type, memory, bufferVk, size, index);
			buffer = new VB(memory, bufferVk, 0, (int) size, 0);
			memory.mapMemory();
		}
		
		@Override
		public void close()
		{
			buffer.closeSegment();
			super.close();
		}
	}
	
	protected static interface MemoryType<S extends Segment> extends AutoCloseable {
		
		@FunctionalInterface
		public interface Allocation<S extends Segment> {
			
			S allocate(MemoryType<S> type, Memory memory, long bufferVk, long size, int index) throws VulkanException;
		}
		
		void release(S segment);
	}
	
	protected static class MutableMemoryType<S extends Segment> implements MemoryType<S> {
		
		private final Allocation<S> allocationFactory;
		
		protected final Device device;
		protected final MutableList<S> segments;
		protected final int properties;
		protected int freeSegments;
		protected int minSegmentSize;
		protected long maxCurrentSegmentSize;
		private @Nullable String name;
		
		protected MutableMemoryType(Device device, int minSegmentSize, int properties, Allocation<S> allocationFactory)
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
					if (name != null) memory.setName(name + " " + segments.size());
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
								if (name != null) memory.setName(name + " " + segments.size());
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
			S segment = allocationFactory.allocate(this, memory, bufferVk, size, segments.size());
			
			if (!autoConsume)
			{
				segments.addFirst(segment);
				freeSegments++;
			}
			else segments.addLast(segment);
			
			if (maxCurrentSegmentSize < size) maxCurrentSegmentSize = size;
			return segment;
		}
		
		@Override
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
	
	protected static class ImmutableMemoryType<S extends Segment> implements MemoryType<S> {
		
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
					if (name != null) memory.setName(name + " " + segments.size());
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
								if (name != null) memory.setName(name + " " + segments.size());
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
			S segment = allocationFactory.allocate(this, memory, bufferVk, size, segments.size());
			
			if (!autoConsume)
			{
				segments.addFirst(segment);
				freeSegments++;
			}
			else segments.addLast(segment);
			
			if (maxCurrentSegmentSize < size) maxCurrentSegmentSize = size;
			return segment;
		}
		
		@Override
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
