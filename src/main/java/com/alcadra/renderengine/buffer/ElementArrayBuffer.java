package com.alcadra.renderengine.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class ElementArrayBuffer extends SimpleBuffer {
	
	protected List<Integer> values = new ArrayList<>();
	
	public ElementArrayBuffer()
	{
		this(GL15.glGenBuffers());
	}
	
	protected ElementArrayBuffer(int id)
	{
		super(GL15.GL_ELEMENT_ARRAY_BUFFER, id);
	}
	
	public void add(int i)
	{
		values.add(i);
	}
	
	public void addAll(ElementArrayBuffer arrayBuffer)
	{
		values.addAll(arrayBuffer.values);
	}
	
	public void merge(ElementArrayBuffer arrayBuffer, int offset)
	{
		for (Integer i : arrayBuffer.values) values.add(i + offset);
	}
	
	public void clear()
	{
		values.clear();
	}
	
	public void sync()
	{
		int size = GL15.glGetBufferParameteri(getTarget(), GL15.GL_BUFFER_SIZE);
		if (size != values.size() * 4)
		{
			ByteBuffer b = BufferUtils.createByteBuffer(values.size() * 4);
			for (Integer i : values) b.putInt(i);
			GL15.glBufferData(getTarget(), b.flip(), GL15.GL_DYNAMIC_DRAW);
		}
		else
		{
			ByteBuffer fixed = BufferUtils.createByteBuffer(size);
			for (Integer i : values) fixed.putInt(i);
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
