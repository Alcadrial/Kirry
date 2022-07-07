package com.alcadra.renderengine.buffer;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

public class ElementArrayBuffer extends SimpleBuffer {
	
	public static final ElementArrayBuffer NULL = new ElementArrayBuffer(0) {
		
		@Override
		public void add(int i)
		{}
		
		@Override
		public void addAll(Collection<Integer> collection)
		{}
		
		@Override
		public void addAll(int[] indices)
		{}
		
		@Override
		public int get(int index)
		{
			throw new NoSuchElementException("Null buffer is always empty!");
		}
		
		@Override
		public void merge(ElementArrayBuffer arrayBuffer, int offset)
		{}
		
		@Override
		public void clear()
		{}
	};
	
	protected List<Integer> values;
	private boolean isModified;
	
	public ElementArrayBuffer()
	{
		this(GL15.glGenBuffers());
		values = new ArrayList<>();
		isModified = false;
	}
	
	private ElementArrayBuffer(int id)
	{
		super(GL15.GL_ELEMENT_ARRAY_BUFFER, id);
	}
	
	public void add(int i)
	{
		values.add(i);
		isModified = true;
	}
	
	public void addAll(ElementArrayBuffer elementArrayBuffer)
	{
		addAll(elementArrayBuffer.values);
	}
	
	public void addAll(Collection<Integer> collection)
	{
		values.addAll(collection);
		isModified = true;
	}
	
	public void addAll(int[] indices)
	{
		for (int i : indices) values.add(i);
		isModified = true;
	}
	
	public int get(int index)
	{
		return values.get(index);
	}
	
	public void merge(ElementArrayBuffer arrayBuffer, int offset)
	{
		for (Integer i : arrayBuffer.values) values.add(i + offset);
		isModified = true;
	}
	
	public void clear()
	{
		values.clear();
		isModified = true;
	}
	
	public void sync()
	{
		if (isModified)
		{
			isModified = false;
			// int size = getGlSize();
			// if (size != values.size())
			// {
			IntBuffer b = MemoryUtil.memAllocInt(values.size());
			for (Integer i : values) b.put(i);
			GL15.glBufferData(getTarget(), b.flip(), GL15.GL_DYNAMIC_DRAW);
			MemoryUtil.memFree(b);
			// }
			// else
			// {
			// IntBuffer fixed = MemoryUtil.memAllocInt(size);
			// for (Integer i : values) fixed.put(i);
			// fixed.flip();
			// IntBuffer buffer = MemoryUtil.memAllocInt(size);
			// GL15.glGetBufferSubData(getTarget(), 0, buffer);
			// int start = -1;
			// for (int i = 0; i < size; i++)
			// {
			// if (buffer.get() != fixed.get())
			// {
			// if (start == -1) start = i;
			// }
			// else if (start != -1)
			// {
			// GL15.glBufferSubData(getTarget(), start * 4, fixed.position(start).limit(i));
			// fixed.limit(size).position(i);
			// start = -1;
			// }
			// }
			// MemoryUtil.memFree(fixed);
			// MemoryUtil.memFree(buffer);
			// }
		}
	}
	
	public int getGlSize()
	{
		return GL15.glGetBufferParameteri(getTarget(), GL15.GL_BUFFER_SIZE) / 4;
	}
	
	public int[] getGlData()
	{
		IntBuffer buffer = MemoryUtil.memAllocInt(getGlSize());
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		int[] v = new int[buffer.limit()];
		for (int i = 0; i < v.length; i++) v[i] = buffer.get(i);
		MemoryUtil.memFree(buffer);
		return v;
	}
	
	@Override
	public SimpleBuffer getNull()
	{
		return null;
	}
	
	public int size()
	{
		return values.size();
	}
	
	@Override
	public String toString()
	{
		return "ElementArrayBuffer{values=" + values + "}";
	}
}
