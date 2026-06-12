package com.alcadrial.kirry.render.buffer.writer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.buffer.Memory.Mapping;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public abstract class AbstractBufferWriter<W extends AbstractBufferWriter<W>> implements BufferWriter {
	
	protected final VulkanBuffer srcBuffer;
	protected final int srcOffset;
	protected final VulkanBuffer dstBuffer;
	protected final int dstOffset;
	protected final int size;
	
	protected final ByteBuffer buffer;
	private @Nullable IntBuffer intBuffer;
	private @Nullable FloatBuffer floatBuffer;
	protected int cursor;
	
	public AbstractBufferWriter(VulkanBuffer srcDstBuffer) throws VulkanException
	{
		this(srcDstBuffer, srcDstBuffer);
	}
	
	public AbstractBufferWriter(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer) throws VulkanException
	{
		this(srcBuffer, dstBuffer, 0, dstBuffer.getSize());
	}
	
	public AbstractBufferWriter(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int offset, int size) throws VulkanException
	{
		this(srcBuffer, offset, dstBuffer, offset, size);
	}
	
	public AbstractBufferWriter(VulkanBuffer srcBuffer, int srcOffset, VulkanBuffer dstBuffer, int dstOffset, int size) throws VulkanException
	{
		Mapping srcBufferMapping = srcBuffer.getMemory().getMapping();
		if (srcBufferMapping == null) throw new VulkanException("Memory of srcBuffer " + srcBuffer + " is not mapped");
		
		this.srcBuffer = srcBuffer;
		this.srcOffset = srcOffset;
		this.dstBuffer = dstBuffer;
		this.dstOffset = dstOffset;
		this.size = size;
		cursor = 0;
		ByteBuffer b = srcBufferMapping.getBuffer();
		buffer = b.slice((int) srcBuffer.getOffset() + srcOffset, size).order(b.order());
	}
	
	@SuppressWarnings("unchecked")
	protected W self()
	{
		return (W) this;
	}
	
	protected IntBuffer asInt()
	{
		if (intBuffer != null) return intBuffer;
		return intBuffer = buffer.asIntBuffer();
	}
	
	protected FloatBuffer asFloat()
	{
		if (floatBuffer != null) return floatBuffer;
		return floatBuffer = buffer.asFloatBuffer();
	}
	
	@Override
	public W resetCursor() throws VulkanException
	{
		cursor = 0;
		return self();
	}
	
	@Override
	public W setCursor(int index) throws VulkanException
	{
		if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
		if (index != cursor) cursor = index;
		return self();
	}
	
	@Override
	public W put(int... values)
	{
		BufferWriter.super.put(values);
		return self();
	}
	
	@Override
	public W put(byte... values)
	{
		BufferWriter.super.put(values);
		return self();
	}
	
	@Override
	public W put(float... values)
	{
		BufferWriter.super.put(values);
		return self();
	}
	
	@Override
	public W put(ByteBuffer values)
	{
		BufferWriter.super.put(values);
		return self();
	}
	
	@Override
	public W put(ByteBuffer value, int offset, int size)
	{
		buffer.put(cursor * 4, value, offset, size);
		cursor += Math.ceilDiv(size, 4);
		return self();
	}
	
	@Override
	public W put(byte[] value, int offset, int size)
	{
		buffer.put(cursor * 4, value, offset, size);
		cursor += Math.ceilDiv(size, 4);
		return self();
	}
	
	@Override
	public W put(int[] value, int offset, int size)
	{
		asInt().put(cursor, value, offset, size);
		cursor += size;
		return self();
	}
	
	@Override
	public W put(float[] values, int offset, int size)
	{
		asFloat().put(cursor, values, offset, size);
		cursor += size;
		return self();
	}
	
	@Override
	public W put(Vector2f value)
	{
		return put(value.getValuesArray());
	}
	
	@Override
	public W put(Vector3f value)
	{
		return put(value.getValuesArray());
	}
	
	@Override
	public W put(Vector4f value)
	{
		return put(value.getValuesArray());
	}
	
	@Override
	public W put(Vector2i value)
	{
		return put(value.getValuesArray());
	}
	
	@Override
	public W put(Vector3i value)
	{
		return put(value.getValuesArray());
	}
	
	@Override
	public W put(Vector4i value)
	{
		return put(value.getValuesArray());
	}
	
	@Override
	public W put(Matrix4f value)
	{
		return put(value.toArray());
	}
	
	@Override
	public int getSrcOffset()
	{
		return srcOffset;
	}
	
	@Override
	public int getDstOffset()
	{
		return dstOffset;
	}
	
	@Override
	public VulkanBuffer getSrcBuffer()
	{
		return srcBuffer;
	}
	
	@Override
	public VulkanBuffer getDstBuffer()
	{
		return dstBuffer;
	}
	
	@Override
	public int getSize()
	{
		return size;
	}
}
