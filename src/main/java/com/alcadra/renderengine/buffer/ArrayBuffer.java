package com.alcadra.renderengine.buffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

public abstract class ArrayBuffer<B extends Buffer> extends SimpleBuffer {
	
	public static final ArrayBuffer<Buffer> NULL = new ArrayBuffer<>(0) {
		
		@Override
		public int size()
		{
			return 0;
		}
		
		@Override
		public int byteSize()
		{
			return 0;
		}
		
		@Override
		protected Buffer nativeBuffer()
		{
			return null;
		}
		
		@Override
		protected Buffer nativeGlBuffer()
		{
			return null;
		}
		
		@Override
		protected void syncRewrite(Buffer buffer)
		{}
		
		@Override
		protected void syncModify(Buffer buffer, int from, int to)
		{}
		
		@Override
		protected boolean equals(Buffer buffer, Buffer other)
		{
			return false;
		}
		
		@Override
		protected int elementSize()
		{
			return 0;
		}
	};
	
	protected boolean isModified = false;
	
	public ArrayBuffer()
	{
		
	}
	
	private ArrayBuffer(int id)
	{
		super(GL15.GL_ARRAY_BUFFER, id);
	}
	
	public abstract int size();
	
	public abstract int byteSize();
	
	@Override
	public SimpleBuffer getNull()
	{
		return null;
	}
	
	protected abstract B nativeBuffer();
	protected abstract B nativeGlBuffer();
	
	protected abstract void syncRewrite(B buffer);
	protected abstract void syncModify(B buffer, int from, int to);
	
	protected abstract boolean equals(B buffer, B other);
	
	public void sync()
	{
		if (isModified)
		{
			/*
			@off
			int byteSize = getGlByteSize();
			if (byteSize != byteSize())
			{
			@on
			*/
			B buffer = nativeBuffer();
			syncRewrite(buffer);
			MemoryUtil.memFree(buffer);
			/*
			@off
			}
			else
			{
				B local = nativeBuffer();
				B buffer = nativeGlBuffer();
				int start = -1;
				int size = size();
				for (int i = 0; i < size; i++)
				{
					if (!equals(buffer, local))
					{
						if (start == -1) start = i;
					}
					else if (start != -1)
					{
						syncModify(local, start, i);
						local.limit(size).position(i);
						start = -1;
					}
				}
				MemoryUtil.memFree(local);
				MemoryUtil.memFree(buffer);
			}
			@on
			*/
			isModified = false;
		}
	}
	
	protected abstract int elementSize();
	
	public int getGlSize()
	{
		return getGlByteSize() / elementSize();
	}
	
	public int getGlByteSize()
	{
		return GL15.glGetBufferParameteri(getTarget(), GL15.GL_BUFFER_SIZE);
	}
	
	public byte[] getGlData()
	{
		ByteBuffer buffer = MemoryUtil.memAlloc(getGlByteSize());
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		byte[] v = new byte[getGlByteSize()];
		for (int i = 0; i < v.length; i++) v[i] = buffer.get(i);
		MemoryUtil.memFree(buffer);
		return v;
	}
	
	public int[] getGlIntData()
	{
		IntBuffer buffer = MemoryUtil.memAllocInt(getGlByteSize() / 4);
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		int[] v = new int[buffer.limit()];
		for (int i = 0; i < v.length; i++) v[i] = buffer.get(i);
		MemoryUtil.memFree(buffer);
		return v;
	}
	
	public float[] getGlFloatData()
	{
		IntBuffer buffer = MemoryUtil.memAllocInt(getGlByteSize() / 4);
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		float[] v = new float[buffer.limit()];
		for (int i = 0; i < v.length; i++) v[i] = Float.intBitsToFloat(buffer.get(i));
		MemoryUtil.memFree(buffer);
		return v;
	}
}
