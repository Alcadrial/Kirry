package com.alcadra.renderengine.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class ArrayBuffer extends SimpleBuffer {
	
	protected List<Byte> values = new ArrayList<>();
	
	public ArrayBuffer()
	{}
	
	protected ArrayBuffer(int target)
	{
		super(target);
	}
	
	protected ArrayBuffer(int target, int id)
	{
		super(target, id);
	}
	
	public void add(byte i)
	{
		values.add(i);
	}
	
	public void add(short i)
	{
		values.addAll(Arrays.asList((byte) (i >>> 8), (byte) (i & 255)));
	}
	
	public void add(int i)
	{
		values.addAll(Arrays.asList((byte) (i >>> 24), (byte) (i >> 16 & 255), (byte) (i >> 8 & 255), (byte) (i & 255)));
	}
	
	public void add(float f)
	{
		add(Float.floatToRawIntBits(f));
	}
	
	public void add(long i)
	{
		values.addAll(Arrays.asList((byte) (i >>> 56), (byte) (i >> 48 & 255), (byte) (i >> 40 & 255), (byte) (i >> 32 & 255), (byte) (i >> 24 & 255), (byte) (i >> 16 & 255), (byte) (i >> 8 & 255), (byte) (i & 255)));
	}
	
	public void add(double f)
	{
		add(Double.doubleToRawLongBits(f));
	}
	
	public void merge(ArrayBuffer arrayBuffer)
	{
		values.addAll(arrayBuffer.values);
	}
	
	public void clear()
	{
		values.clear();
	}
	
	public void sync()
	{
		int size = getGlSize();
		if (size != size())
		{
			ByteBuffer b = BufferUtils.createByteBuffer(values.size());
			for (byte i : values) b.put(i);
			GL15.glBufferData(getTarget(), b.flip(), GL15.GL_DYNAMIC_DRAW);
		}
		else
		{
			ByteBuffer fixed = BufferUtils.createByteBuffer(size);
			for (byte i : values) fixed.put(i);
			fixed.flip();
			ByteBuffer buffer = BufferUtils.createByteBuffer(size);
			GL15.glGetBufferSubData(getTarget(), 0, buffer);
			int start = -1;
			for (int i = 0; i < size; i++)
			{
				if (buffer.get() != fixed.get())
				{
					if (start == -1) start = i;
				}
				else if (start != -1)
				{
					ByteBuffer l = BufferUtils.createByteBuffer(i - start);
					for (int j = start; j < i; j++) l.put(fixed.get(j));
					GL15.glBufferSubData(getTarget(), start, l.flip());
					start = -1;
				}
			}
		}
	}
	
	public int getGlSize()
	{
		return GL15.glGetBufferParameteri(getTarget(), GL15.GL_BUFFER_SIZE) / elementSize();
	}
	
	public int getGlByteSize()
	{
		return GL15.glGetBufferParameteri(getTarget(), GL15.GL_BUFFER_SIZE);
	}
	
	public int[] getGlIntData()
	{
		int[] v = new int[getGlSize()];
		GL15.glGetBufferSubData(getTarget(), 0, v);
		return v;
	}
	
	public float[] getGlFloatData()
	{
		float[] v = new float[getGlSize()];
		GL15.glGetBufferSubData(getTarget(), 0, v);
		return v;
	}
	
	public int size()
	{
		return values.size();
	}
	
	protected int elementSize()
	{
		return 1;
	}
	
	@Override
	public String toString()
	{
		return "ArrayBuffer{values=" + values + "}";
	}
}
