package com.alcadrial.renderengine.render.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

import org.lwjgl.vulkan.VkBufferCopy;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;

public class BufferWriter implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private class Work {
		
		private Future<?> work;
		private Work next;
		
		public Work(Future<?> work)
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
	
	private @Nullable CommandBuffer flushCommandBuffer;
	private Work firstWork;
	private Work lastWork;
	
	public BufferWriter(VulkanBuffer srcBuffer, int srcOffset, VulkanBuffer dstBuffer, int dstOffset, int size)
	{
		this(srcBuffer, srcOffset, dstBuffer, dstOffset, size, null);
	}
	
	public BufferWriter(VulkanBuffer srcBuffer, int srcOffset, VulkanBuffer dstBuffer, int dstOffset, int size, @Nullable CommandBuffer flushCommandBuffer)
	{
		this.srcBuffer = srcBuffer;
		this.srcOffset = srcOffset;
		this.dstBuffer = dstBuffer;
		this.dstOffset = dstOffset;
		this.flushCommandBuffer = flushCommandBuffer;
		this.size = size;
		cursor = 0;
		buffer = srcBuffer.getByteBuffer(srcOffset, size);
		vkBufferCopy = VkBufferCopy.malloc(MAX_COPY_COUNT);
		firstWork = lastWork = null;
		lastSetCursor = 0;
		isModified = false;
	}
	
	public BufferWriter resetCursor()
	{
		registerCopy();
		cursor = lastSetCursor = 0;
		if (bufferCopyCount == MAX_COPY_COUNT) flush();
		return this;
	}
	
	public BufferWriter setCursor(int index)
	{
		if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
		registerCopy();
		cursor = lastSetCursor = index;
		if (bufferCopyCount == MAX_COPY_COUNT) flush();
		return this;
	}
	
	private void registerCopy()
	{
		if (lastSetCursor != cursor)
		{
			VkBufferCopy copy = vkBufferCopy.get();
			copy.srcOffset(lastSetCursor * 4L);
			copy.dstOffset(dstOffset + lastSetCursor * 4L);
			copy.size((cursor - lastSetCursor) * 4L);
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
		isModified = true;
		buffer.asFloatBuffer().put(cursor, value.getValuesArray());
		cursor += 2;
		return this;
	}
	
	public BufferWriter put(Vector3f value)
	{
		isModified = true;
		buffer.asFloatBuffer().put(cursor, value.getValuesArray());
		cursor += 3;
		return this;
	}
	
	public BufferWriter put(Vector4f value)
	{
		isModified = true;
		buffer.asFloatBuffer().put(cursor, value.getValuesArray());
		cursor += 4;
		return this;
	}
	
	public BufferWriter put(Vector2i value)
	{
		isModified = true;
		buffer.asIntBuffer().put(cursor, value.getValuesArray());
		cursor += 2;
		return this;
	}
	
	public BufferWriter put(Vector3i value)
	{
		isModified = true;
		buffer.asIntBuffer().put(cursor, value.getValuesArray());
		cursor += 3;
		return this;
	}
	
	public BufferWriter put(Vector4i value)
	{
		isModified = true;
		buffer.asIntBuffer().put(cursor, value.getValuesArray());
		cursor += 4;
		return this;
	}
	
	public BufferWriter put(Matrix4f value)
	{
		isModified = true;
		buffer.asFloatBuffer().put(cursor, value.toArray());
		cursor += 16;
		return this;
	}
	
	public void flush()
	{
		if (isModified)
		{
			if (flushCommandBuffer != null)
			{
				flush(flushCommandBuffer);
				return;
			}
			Device device = srcBuffer.getDevice();
			Work work = new Work(device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), this::flush));
			
			if (firstWork == null) firstWork = lastWork = work;
			else
			{
				lastWork.next = work;
				lastWork = work;
			}
		}
	}
	
	public void flush(CommandBuffer commandBuffer)
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
	
	public Future<?> getLoad()
	{
		return RenderEngine.getExecutor().submit(() -> {
			
			for (Work work = firstWork; work != null; work = work.next)
			{
				if (Thread.interrupted()) throw new CancellationException();
				work.work.get();
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
	public void close()
	{
		flush();
		if (firstWork != null) for (Work work = firstWork, work0 = work.next; work0 != null; work = work0, work0 = work0.next) work.next = null;
		vkBufferCopy.free();
		srcBuffer.unmapBuffer();
	}
}
