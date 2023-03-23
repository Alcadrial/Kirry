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
		protected int elementSize()
		{
			return 0;
		}
		
		@Override
		public void clear()
		{}
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
	
	public abstract void clear();
	
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
