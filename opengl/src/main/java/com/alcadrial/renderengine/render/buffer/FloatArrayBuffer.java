package com.alcadrial.renderengine.render.buffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

public class FloatArrayBuffer extends GlBuffer {
	
	protected ArrayList<Float> values = new ArrayList<>();
	
	public FloatArrayBuffer()
	{
		super(GL15.GL_ARRAY_BUFFER);
	}
	
	public void add(float f)
	{
		values.add(f);
		isModified = true;
	}
	
	public void addAll(Collection<Float> f)
	{
		values.addAll(f);
		isModified = true;
	}
	
	public void addAll(float[] f)
	{
		values.ensureCapacity(values.size() + f.length);
		for (float f0 : f) values.add(f0);
		isModified = true;
	}
	
	public float setFloat(int index, float f)
	{
		float f0 = values.set(index, f);
		if (f0 != f && !Float.isNaN(f0) || !Float.isNaN(f)) isModified = true;
		return f0;
	}
	
	public float getFloat(int index)
	{
		return values.get(index);
	}
	
	public void merge(FloatArrayBuffer arrayBuffer)
	{
		values.addAll(arrayBuffer.asList());
		isModified = true;
	}
	
	@Override
	public void clear()
	{
		values.clear();
		isModified = true;
	}
	
	public List<Float> asList()
	{
		return List.copyOf(values);
	}
	
	@Override
	public int size()
	{
		return values.size();
	}
	
	@Override
	protected ByteBuffer nativeBuffer()
	{
		FloatBuffer buffer = MemoryUtil.memAllocFloat(values.size());
		for (Float i : values) buffer.put(i);
		buffer.flip();
		return MemoryUtil.memByteBuffer(buffer);
	}
	
	@Override
	protected ByteBuffer nativeGpuBuffer()
	{
		FloatBuffer buffer = MemoryUtil.memAllocFloat(getGpuSize());
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
		return "FloatArrayBuffer{values=" + values + "}";
	}
}
