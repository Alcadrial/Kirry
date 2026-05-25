package com.alcadrial.renderengine.render.buffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkMappedMemoryRange;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.buffer.Memory.Mapping;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.future.VulkanFuture;

public class BufferWriter implements AutoCloseable {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	public static BufferWriter nullWriter()
	{
		return NullBufferWriter.INSTANCE;
	}
	
	public static VulkanFuture<?> writeData(ByteBuffer data, VulkanBuffer dst) throws VulkanException
	{
		VulkanBuffer src = new VulkanBuffer(dst.getDevice(), data.limit(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
		BufferWriter writer = new BufferWriter(src, dst);
		
		src.getMemory().mapMemory();
		writer.put(data);
		src.getMemory().unmapMemory();
		writer.transfer();
		return VulkanFuture.composeAfter(writer.getLoad(), () -> {
			src.close();
			writer.close();
		});
	}
	
	public static VulkanFuture<?> writeData(int[] data, VulkanBuffer dst) throws VulkanException
	{
		VulkanBuffer src = new VulkanBuffer(dst.getDevice(), data.length * 4, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
		BufferWriter writer = new BufferWriter(src, dst);
		
		src.getMemory().mapMemory();
		writer.put(data);
		src.getMemory().unmapMemory();
		writer.transfer();
		return VulkanFuture.composeAfter(writer.getLoad(), () -> {
			src.close();
			writer.close();
		});
	}
	
	public static VulkanFuture<?> writeData(byte[] data, VulkanBuffer dst) throws VulkanException
	{
		VulkanBuffer src = new VulkanBuffer(dst.getDevice(), data.length, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
		BufferWriter writer = new BufferWriter(src, dst);
		
		src.getMemory().mapMemory();
		writer.put(data);
		src.getMemory().unmapMemory();
		writer.transfer();
		return VulkanFuture.composeAfter(writer.getLoad(), () -> {
			src.close();
			writer.close();
		});
	}
	
	private class Work {
		
		private VulkanFuture<?> work;
		private @Nullable Work next;
		
		public Work(VulkanFuture<?> work)
		{
			this.work = work;
			next = null;
		}
	}
	
	private static final int MAX_COPY_COUNT = 16;
	
	private VulkanBuffer srcBuffer;
	private int srcOffset;
	private VulkanBuffer dstBuffer;
	private int dstOffset;
	
	private ByteBuffer buffer;
	private int lastSetCursor;
	private int cursor;
	private int size;
	private boolean isModified;
	
	private VkBufferCopy.Buffer vkBufferCopy;
	private int bufferCopyCount;
	private boolean preferFlush;
	
	private @Nullable CommandBuffer flushCommandBuffer;
	private @Nullable Work firstWork;
	private @Nullable Work lastWork;
	
	public BufferWriter(VulkanBuffer srcDstBuffer) throws VulkanException
	{
		this(srcDstBuffer, srcDstBuffer);
	}
	
	public BufferWriter(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer) throws VulkanException
	{
		this(srcBuffer, dstBuffer, 0, dstBuffer.getSize());
	}
	
	public BufferWriter(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int offset, int size) throws VulkanException
	{
		this(srcBuffer, offset, dstBuffer, offset, size, null);
	}
	
	public BufferWriter(VulkanBuffer srcBuffer, int srcOffset, VulkanBuffer dstBuffer, int dstOffset, int size) throws VulkanException
	{
		this(srcBuffer, srcOffset, dstBuffer, dstOffset, size, null);
	}
	
	public BufferWriter(VulkanBuffer srcBuffer, int srcOffset, VulkanBuffer dstBuffer, int dstOffset, int size, @Nullable CommandBuffer flushCommandBuffer) throws VulkanException
	{
		Mapping srcBufferMapping = srcBuffer.getMemory().getMapping();
		if (srcBufferMapping == null) throw new VulkanException("Memory of srcBuffer " + srcBuffer + " is not mapped");
		this.srcBuffer = srcBuffer;
		this.srcOffset = srcOffset;
		this.dstBuffer = dstBuffer;
		this.dstOffset = dstOffset;
		this.flushCommandBuffer = flushCommandBuffer;
		this.size = size;
		cursor = 0;
		buffer = srcBufferMapping.getBuffer();
		buffer = buffer.slice((int) srcBuffer.getOffset() + srcOffset, size).order(buffer.order());
		vkBufferCopy = VkBufferCopy.malloc(MAX_COPY_COUNT);
		firstWork = lastWork = null;
		lastSetCursor = 0;
		isModified = false;
		preferFlush = false;
	}
	
	public void setPreferFlush(boolean preferFlush)
	{
		this.preferFlush = preferFlush;
	}
	
	/**
	 * Null instance for {@link NullBufferWriter}
	 */
	BufferWriter()
	{
		this.srcBuffer = (@NonNull VulkanBuffer) null;
		this.srcOffset = 0;
		this.dstBuffer = (@NonNull VulkanBuffer) null;
		this.dstOffset = 0;
		this.flushCommandBuffer = null;
		this.size = 0;
		cursor = 0;
		buffer = (@NonNull ByteBuffer) null;
		vkBufferCopy = (VkBufferCopy.@NonNull Buffer) null;
		firstWork = lastWork = null;
		lastSetCursor = 0;
		isModified = false;
	}
	
	public BufferWriter resetCursor() throws VulkanException
	{
		registerCopy();
		cursor = lastSetCursor = 0;
		if (bufferCopyCount == MAX_COPY_COUNT)
		{
			if (preferFlush) flushAndInvalidate();
			else transfer();
		}
		return this;
	}
	
	public BufferWriter setCursor(int index) throws VulkanException
	{
		if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
		if (index != cursor)
		{
			registerCopy();
			cursor = lastSetCursor = index;
			if (bufferCopyCount == MAX_COPY_COUNT)
			{
				if (preferFlush) flushAndInvalidate();
				else transfer();
			}
		}
		return this;
	}
	
	private void registerCopy()
	{
		if (lastSetCursor != cursor)
		{
			VkBufferCopy copy = vkBufferCopy.get();
			copy.srcOffset(srcOffset + lastSetCursor * 4L);
			copy.dstOffset(dstOffset + lastSetCursor * 4L);
			copy.size((cursor - lastSetCursor) * 4L);
			Debug.debugCopy(() -> "Copy (" + bufferCopyCount + ") from " + srcBuffer + " to " + dstBuffer + " {" + lastSetCursor * 4L + ", " + lastSetCursor * 4 + ", " + (cursor - lastSetCursor) * 4L + "}");
			bufferCopyCount++;
			lastSetCursor = cursor;
		}
	}
	
	public BufferWriter put(int value)
	{
		isModified = true;
		buffer.asIntBuffer().put(cursor, value);
		cursor += 1;
		return this;
	}
	
	public BufferWriter put(int... values)
	{
		isModified = true;
		buffer.asIntBuffer().put(cursor, values);
		cursor += values.length;
		return this;
	}
	
	public BufferWriter put(byte... values)
	{
		isModified = true;
		buffer.put(cursor * 4, values);
		cursor += Math.ceilDiv(values.length, 4);
		return this;
	}
	
	public BufferWriter put(ByteBuffer value)
	{
		isModified = true;
		buffer.put(cursor * 4, value, 0, value.limit());
		cursor += Math.ceilDiv(value.limit(), 4);
		return this;
	}
	
	public BufferWriter put(ByteBuffer value, int offset, int size)
	{
		isModified = true;
		buffer.put(cursor * 4, value, offset, size);
		cursor += Math.ceilDiv(size, 4);
		return this;
	}
	
	public BufferWriter put(byte[] value, int offset, int size)
	{
		isModified = true;
		buffer.put(cursor * 4, value, offset, size);
		cursor += Math.ceilDiv(size, 4);
		return this;
	}
	
	public BufferWriter put(int[] value, int offset, int size)
	{
		isModified = true;
		buffer.asIntBuffer().put(cursor, value, offset, size);
		cursor += size;
		return this;
	}
	
	public BufferWriter put(float value)
	{
		isModified = true;
		buffer.asFloatBuffer().put(cursor, value);
		cursor += 1;
		return this;
	}
	
	public BufferWriter put(float... value)
	{
		isModified = true;
		buffer.asFloatBuffer().put(cursor, value);
		cursor += value.length;
		return this;
	}
	
	public BufferWriter put(Vector2f value)
	{
		return put(value.getValuesArray());
	}
	
	public BufferWriter put(Vector3f value)
	{
		return put(value.getValuesArray());
	}
	
	public BufferWriter put(Vector4f value)
	{
		return put(value.getValuesArray());
	}
	
	public BufferWriter put(Vector2i value)
	{
		return put(value.getValuesArray());
	}
	
	public BufferWriter put(Vector3i value)
	{
		return put(value.getValuesArray());
	}
	
	public BufferWriter put(Vector4i value)
	{
		return put(value.getValuesArray());
	}
	
	public BufferWriter put(Matrix4f value)
	{
		return put(value.toArray());
	}
	
	public BufferWriter fill(byte value)
	{
		isModified = true;
		for (int i = cursor * 4; i < buffer.limit(); i++) buffer.put(i, value);
		cursor = Math.ceilDiv(buffer.limit(), 4);
		return this;
	}
	
	public BufferWriter fill(int value)
	{
		isModified = true;
		IntBuffer b = buffer.asIntBuffer();
		while (cursor < b.limit()) b.put(cursor++, value);
		return this;
	}
	
	public BufferWriter fill(float value)
	{
		isModified = true;
		FloatBuffer b = buffer.asFloatBuffer();
		while (cursor < b.limit()) b.put(cursor++, value);
		return this;
	}
	
	public void flush() throws VulkanException
	{
		Mapping dstBufferMapping = dstBuffer.getMemory().getMapping();
		boolean isMapped = dstBufferMapping != null;
		
		if (isModified)
		{
			registerCopy();
			if (!dstBuffer.getMemory().isHostCoherent())
			{
				vkBufferCopy.rewind();
				if (!isMapped)
				{
					int startRegion = dstBuffer.getSize();
					int endRegion = 0;
					
					for (VkBufferCopy bufferCopy : vkBufferCopy)
					{
						int offset = (int) ((bufferCopy.dstOffset() - dstOffset) / 4);
						int end = (int) (offset + bufferCopy.size() / 4);
						if (startRegion > offset) startRegion = offset;
						if (endRegion < end) endRegion = end;
					}
					
					dstBufferMapping = dstBuffer.getMemory().mapMemory(startRegion, endRegion - startRegion);
				}
				
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					VkMappedMemoryRange.Buffer ranges = VkMappedMemoryRange.malloc(vkBufferCopy.remaining(), stack);
					for (VkBufferCopy bufferCopy : vkBufferCopy) ranges.get()
						.sType(VK10.VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
						.pNext(0)
						.memory(dstBuffer.getMemory().getVk())
						.offset(bufferCopy.dstOffset())
						.size(bufferCopy.size());
					VK10.vkFlushMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges.rewind());
				}
			}
			
			vkBufferCopy.clear();
			bufferCopyCount = 0;
			isModified = false;
			if (!isMapped && !dstBuffer.getMemory().isHostCoherent()) dstBuffer.getMemory().unmapMemory();
		}
	}
	
	public void flushAndInvalidate() throws VulkanException
	{
		Mapping dstBufferMapping = dstBuffer.getMemory().getMapping();
		boolean isMapped = dstBufferMapping != null;
		
		if (isModified)
		{
			registerCopy();
			if (!dstBuffer.getMemory().isHostCoherent())
			{
				vkBufferCopy.rewind();
				if (!isMapped)
				{
					int startRegion = dstBuffer.getSize();
					int endRegion = 0;
					
					for (VkBufferCopy bufferCopy : vkBufferCopy)
					{
						int offset = (int) ((bufferCopy.dstOffset() - dstOffset) / 4);
						int end = (int) (offset + bufferCopy.size() / 4);
						if (startRegion > offset) startRegion = offset;
						if (endRegion < end) endRegion = end;
					}
					
					dstBufferMapping = dstBuffer.getMemory().mapMemory(startRegion, endRegion - startRegion);
				}
				
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					VkMappedMemoryRange.Buffer ranges = VkMappedMemoryRange.malloc(vkBufferCopy.remaining(), stack);
					for (VkBufferCopy bufferCopy : vkBufferCopy) ranges.get()
						.sType(VK10.VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
						.pNext(0)
						.memory(dstBuffer.getMemory().getVk())
						.offset(bufferCopy.dstOffset())
						.size(bufferCopy.size());
					VK10.vkFlushMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges.rewind());
					VK10.vkInvalidateMappedMemoryRanges(dstBuffer.getDevice().getVk(), ranges);
				}
			}
			
			vkBufferCopy.clear();
			bufferCopyCount = 0;
			isModified = false;
			if (!isMapped && !dstBuffer.getMemory().isHostCoherent()) dstBuffer.getMemory().unmapMemory();
		}
	}
	
	public void transfer() throws VulkanException
	{
		if (flushCommandBuffer != null)
		{
			transfer(flushCommandBuffer);
			return;
		}
		if (isModified)
		{
			Device device = srcBuffer.getDevice();
			Work work = new Work(device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), this::transfer));
			
			Work w = lastWork;
			if (w == null) firstWork = lastWork = work;
			else
			{
				w.next = work;
				lastWork = work;
			}
		}
	}
	
	public void transfer(CommandBuffer commandBuffer)
	{
		if (isModified)
		{
			registerCopy();
			commandBuffer.copyBuffer(srcBuffer, dstBuffer, vkBufferCopy.flip());
			vkBufferCopy.clear();
			bufferCopyCount = 0;
			isModified = false;
		}
	}
	
	public VulkanFuture<?> getLoad()
	{
		return VulkanFuture.submitAsync(() -> {
			for (Work work = firstWork; work != null; work = work.next)
			{
				work.work.join();
				firstWork = work;
			}
			return null;
		});
	}
	
	public int getSrcOffset()
	{
		return srcOffset;
	}
	
	public int getDstOffset()
	{
		return dstOffset;
	}
	
	public VulkanBuffer getSrcBuffer()
	{
		return srcBuffer;
	}
	
	public VulkanBuffer getDstBuffer()
	{
		return dstBuffer;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public void setCommandBuffer(CommandBuffer commandBuffer)
	{
		flushCommandBuffer = commandBuffer;
	}
	
	@Override
	public void close() throws VulkanException
	{
		if (preferFlush) flushAndInvalidate();
		else transfer();
		if (firstWork != null) for (Work work = firstWork, work0 = work.next; work0 != null; work = work0, work0 = work0.next) work.next = null;
		vkBufferCopy.free();
	}
}
