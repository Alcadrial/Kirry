package com.alcadrial.kirry.render.buffer.writer;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2dr;
import com.alcadrial.kector.vector.Vector2fr;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector2lr;
import com.alcadrial.kector.vector.Vector3dr;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kector.vector.Vector3lr;
import com.alcadrial.kector.vector.Vector4dr;
import com.alcadrial.kector.vector.Vector4fr;
import com.alcadrial.kector.vector.Vector4ir;
import com.alcadrial.kector.vector.Vector4lr;
import com.alcadrial.kirry.render.buffer.Memory.Mapping;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
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
	private @Nullable LongBuffer longBuffer;
	private @Nullable DoubleBuffer doubleBuffer;
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
	
	protected LongBuffer asLong()
	{
		if (longBuffer != null) return longBuffer;
		return longBuffer = buffer.asLongBuffer();
	}
	
	protected DoubleBuffer asDouble()
	{
		if (doubleBuffer != null) return doubleBuffer;
		return doubleBuffer = buffer.asDoubleBuffer();
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
	public W put(byte... values)
	{
		return put(values, 0, values.length);
	}
	
	@Override
	public W put(int... values)
	{
		return put(values, 0, values.length);
	}
	
	@Override
	public W put(float... values)
	{
		return put(values, 0, values.length);
	}
	
	@Override
	public W put(long... values)
	{
		return put(values, 0, values.length);
	}
	
	@Override
	public W put(double... values)
	{
		return put(values, 0, values.length);
	}
	
	@Override
	public W put(ByteBuffer values)
	{
		return put(values, 0, values.limit());
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
	public W put(long[] value, int offset, int size)
	{
		asLong().put(cursor, value, offset, size);
		cursor += size;
		return self();
	}
	
	@Override
	public W put(double[] values, int offset, int size)
	{
		asDouble().put(cursor, values, offset, size);
		cursor += size;
		return self();
	}

	@Override
	public W put(Vector2ir v)
	{
		return put(v.x(), v.y());
	}
	@Override
	public W put(Vector2lr v)
	{
		return put(v.x(), v.y());
	}
	@Override
	public W put(Vector2fr v)
	{
		return put(v.x(), v.y());
	}
	@Override
	public W put(Vector2dr v)
	{
		return put(v.x(), v.y());
	}
	
	@Override
	public W put(Vector3ir v)
	{
		return put(v.x(), v.y(), v.z());
	}
	@Override
	public W put(Vector3lr v)
	{
		return put(v.x(), v.y(), v.z());
	}
	@Override
	public W put(Vector3fr v)
	{
		return put(v.x(), v.y(), v.z());
	}
	@Override
	public W put(Vector3dr v)
	{
		return put(v.x(), v.y(), v.z());
	}
	
	@Override
	public W put(Vector4ir v)
	{
		return put(v.x(), v.y(), v.z(), v.w());
	}
	@Override
	public W put(Vector4lr v)
	{
		return put(v.x(), v.y(), v.z(), v.w());
	}
	@Override
	public W put(Vector4fr v)
	{
		return put(v.x(), v.y(), v.z(), v.w());
	}
	@Override
	public W put(Vector4dr v)
	{
		return put(v.x(), v.y(), v.z(), v.w());
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
