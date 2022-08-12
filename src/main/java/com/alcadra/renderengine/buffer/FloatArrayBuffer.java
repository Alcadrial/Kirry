package com.alcadra.renderengine.buffer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

public class FloatArrayBuffer extends ArrayBuffer<FloatBuffer> {
	
	protected ArrayList<Float> values = new ArrayList<>();
	
	public FloatArrayBuffer()
	{}
	
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
	
	public float getFloat(int index)
	{
		return values.get(index);
	}
	
	public void merge(FloatArrayBuffer arrayBuffer)
	{
		values.addAll(arrayBuffer.values);
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
	public int byteSize()
	{
		return size() * 4;
	}
	
	@Override
	protected FloatBuffer nativeBuffer()
	{
		FloatBuffer buffer = MemoryUtil.memAllocFloat(values.size());
		for (Float i : values) buffer.put(i);
		buffer.flip();
		// if (buffer.capacity() == 336) System.out.println(Arrays.toString(getGlFloatData()));
		return buffer;
	}
	
	@Override
	protected FloatBuffer nativeGlBuffer()
	{
		FloatBuffer buffer = MemoryUtil.memAllocFloat(getGlSize());
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		return buffer;
	}
	
	@Override
	protected void syncRewrite(FloatBuffer buffer)
	{
		GL15.glBufferData(getTarget(), buffer, GL15.GL_DYNAMIC_DRAW);
	}
	
	@Override
	protected void syncModify(FloatBuffer buffer, int from, int to)
	{
		GL15.glBufferSubData(getTarget(), from, buffer.position(from - 1).limit(to));
	}
	
	@Override
	protected boolean equals(FloatBuffer buffer, FloatBuffer other)
	{
		return buffer.get() == other.get();
	}
	
	@Override
	protected int elementSize()
	{
		return 4;
	}
	
	@Override
	public float[] getGlFloatData()
	{
		FloatBuffer buffer = MemoryUtil.memAllocFloat(getGlSize());
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		float[] v = new float[buffer.limit()];
		for (int i = 0; i < v.length; i++) v[i] = buffer.get(i);
		MemoryUtil.memFree(buffer);
		return v;
	}
	
	@Override
	public String toString()
	{
		return "FloatArrayBuffer{values=" + values + "}";
	}
}
