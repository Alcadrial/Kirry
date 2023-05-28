package com.alcadrial.renderengine.opengl.buffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

public class ElementArrayBuffer extends GlBuffer {
	
	protected ArrayList<Integer> values;
	private boolean isModified;
	private int max;
	
	public ElementArrayBuffer()
	{
		super(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.glGenBuffers());
		values = new ArrayList<>();
		isModified = false;
		max = -1;
	}
	
	public void add(int i)
	{
		values.add(i);
		if (i > max) max = i;
		isModified = true;
	}
	
	public void addAll(ElementArrayBuffer elementArrayBuffer)
	{
		addAll(elementArrayBuffer.values);
	}
	
	public void addAll(Collection<Integer> collection)
	{
		values.addAll(collection);
		Optional<Integer> max = collection.stream().max(Integer::compare);
		max.ifPresent(i -> this.max = i);
		isModified = true;
	}
	
	public void addAll(int[] indices)
	{
		values.ensureCapacity(values.size() + indices.length);
		for (int i : indices)
		{
			values.add(i);
			if (i > max) max = i;
		}
		isModified = true;
	}
	
	public int get(int index)
	{
		return values.get(index);
	}
	
	public void merge(ElementArrayBuffer elementBuffer, int offset)
	{
		values.ensureCapacity(values.size() + elementBuffer.size());
		for (Integer i : elementBuffer.getValues())
		{
			int j = i + offset;
			values.add(j);
			if (j > max) max = j;
		}
		isModified = true;
	}
	
	@Override
	public void clear()
	{
		values.clear();
		max = -1;
		isModified = true;
	}
	
	public int getMax()
	{
		return max;
	}
	
	@Override
	public int size()
	{
		return values.size();
	}
	
	@Override
	public void sync()
	{
		if (isModified)
		{
			syncModify();
			isModified = false;
		}
	}
	
	protected void syncModify()
	{
		IntBuffer b = MemoryUtil.memAllocInt(values.size());
		for (Integer i : values) b.put(i);
		GL15.glBufferData(getTarget(), b.flip(), GL15.GL_DYNAMIC_DRAW);
		MemoryUtil.memFree(b);
	}
	
	@Override
	public int getGpuSize()
	{
		return GL15.glGetBufferParameteri(getTarget(), GL15.GL_BUFFER_SIZE) / 4;
	}
	
	@Override
	public int[] getGpuIntData()
	{
		IntBuffer buffer = MemoryUtil.memAllocInt(getGpuSize());
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		int[] v = new int[buffer.limit()];
		for (int i = 0; i < v.length; i++) v[i] = buffer.get(i);
		MemoryUtil.memFree(buffer);
		return v;
	}
	
	private ArrayList<Integer> getValues()
	{
		return values;
	}
	
	@Override
	protected ByteBuffer nativeBuffer()
	{
		IntBuffer buffer = MemoryUtil.memAllocInt(values.size());
		for (Integer i : values) buffer.put(i);
		buffer.flip();
		return MemoryUtil.memByteBuffer(buffer);
	}
	
	@Override
	protected ByteBuffer nativeGpuBuffer()
	{
		IntBuffer buffer = MemoryUtil.memAllocInt(getGpuSize());
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		return MemoryUtil.memByteBuffer(buffer);
	}
	
	@Override
	protected boolean equals(ByteBuffer buffer, ByteBuffer other)
	{
		return buffer.get() == other.get();
	}
	
	@Override
	protected int elementSize()
	{
		return 4;
	}
	
	@Override
	public String toString()
	{
		return "ElementArrayBuffer{values=" + values + "}";
	}
}
