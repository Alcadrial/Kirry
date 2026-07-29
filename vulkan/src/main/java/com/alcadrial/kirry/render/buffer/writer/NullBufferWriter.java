package com.alcadrial.kirry.render.buffer.writer;

import java.nio.ByteBuffer;

import org.jspecify.annotations.NonNull;

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
import com.alcadrial.kirry.render.buffer.VulkanBuffer;

final class NullBufferWriter implements BufferWriter {
	
	static final NullBufferWriter INSTANCE = new NullBufferWriter();
	
	private NullBufferWriter()
	{
		
	}
	
	@Override
	public BufferWriter resetCursor()
	{
		return this;
	}
	
	@Override
	public BufferWriter setCursor(int index)
	{
		return this;
	}
	
	@Override
	public int getCursor()
	{
		return 0;
	}
	
	@Override
	public BufferWriter put(int... values)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(byte... values)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(ByteBuffer value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(byte[] value, int offset, int size)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(int[] value, int offset, int size)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(float... value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Matrix4f value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(ByteBuffer values, int offset, int size)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(float[] values, int offset, int size)
	{
		return this;
	}
	
	@Override
	public int getSrcOffset()
	{
		throw new IllegalStateException("Missing source");
	}
	
	@Override
	public int getDstOffset()
	{
		throw new IllegalStateException("Missing destination");
	}
	
	@Override
	public VulkanBuffer getSrcBuffer()
	{
		throw new IllegalStateException("Missing source");
	}
	
	@Override
	public VulkanBuffer getDstBuffer()
	{
		throw new IllegalStateException("Missing destination");
	}
	
	@Override
	public int getSize()
	{
		throw new IllegalStateException("Missing destination");
	}
	
	@Override
	public BufferWriter put(long @NonNull... values)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(long[] values, int offset, int size)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(double @NonNull... values)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(double[] values, int offset, int size)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector2ir v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector2lr v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector2fr v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector2dr v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector3ir v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector3lr v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector3fr v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector3dr v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector4ir v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector4lr v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector4fr v)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector4dr v)
	{
		return this;
	}
}
