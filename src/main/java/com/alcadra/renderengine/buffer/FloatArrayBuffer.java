package com.alcadra.renderengine.buffer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import com.alcadra.renderengine.Launch;

public class FloatArrayBuffer extends ArrayBuffer {
	
	protected List<Float> values = new ArrayList<>();
	
	public FloatArrayBuffer()
	{}
	
	protected FloatArrayBuffer(int target)
	{
		super(target);
	}
	
	protected FloatArrayBuffer(int target, int id)
	{
		super(target, id);
	}
	
	@Override
	public void add(byte i)
	{
		throw new IllegalArgumentException("Only floats accepted");
	}
	
	@Override
	public void add(short i)
	{
		throw new IllegalArgumentException("Only floats accepted");
	}
	
	@Override
	public void add(int i)
	{
		throw new IllegalArgumentException("Only floats accepted");
	}
	
	@Override
	public void add(float f)
	{
		values.add(f);
	}
	
	public void addAll(Collection<Float> f)
	{
		values.addAll(f);
	}
	
	@Override
	public void add(long i)
	{
		throw new IllegalArgumentException("Only floats accepted");
	}
	
	@Override
	public void add(double f)
	{
		throw new IllegalArgumentException("Only floats accepted");
	}
	
	@Override
	public void merge(ArrayBuffer arrayBuffer)
	{
		if (!(arrayBuffer instanceof FloatArrayBuffer)) throw new IllegalArgumentException("Only FloatArrayBuffers accepted");
		values.addAll(((FloatArrayBuffer) arrayBuffer).values);
	}
	
	@Override
	public void clear()
	{
		values.clear();
	}
	
	@Override
	public void sync()
	{
		bind();
		// int size = (int) (getGlByteSize() / 4F);
		// if (size != values.size())
		{
			FloatBuffer b = BufferUtils.createFloatBuffer(values.size());
			for (Float i : values) b.put(i);
			GL15.glBufferData(getTarget(), b.flip(), GL15.GL_DYNAMIC_DRAW);
			Launch.printError("ArrayBuffer[INTERNAL] data");
		}
		// else
		// {
		// FloatBuffer fixed = BufferUtils.createFloatBuffer(size);
		// for (Float i : values) fixed.put(i);
		// fixed.flip();
		//
		// FloatBuffer buffer = BufferUtils.createFloatBuffer(size);
		// GL15.glGetBufferSubData(getTarget(), 0, buffer);
		//
		// Launch.printError("ArrayBuffer[INTERNAL] get sub data " + size + "/" + getGlByteSize() / 4F);
		// int start = -1;
		// for (int i = 0; i < size; i++)
		// {
		// if (buffer.get() != fixed.get())
		// {
		// if (start == -1) start = i;
		// }
		// else if (start != -1)
		// {
		// FloatBuffer l = BufferUtils.createFloatBuffer(i - start);
		// for (int j = start; j < i; j++) l.put(fixed.get(j));
		//
		// GL15.glBufferSubData(getTarget(), start, l.flip());
		// Launch.printError("ArrayBuffer[INTERNAL] sub data");
		// start = -1;
		// }
		// }
		// }
		Launch.printError("ArrayBuffer[INTERNAL] sync");
	}
	
	public List<Float> asList()
	{
		return new ArrayList<>(values);
	}
	
	@Override
	protected int elementSize()
	{
		return 3;
	}
	
	@Override
	public int size()
	{
		return values.size();
	}
	
	@Override
	public String toString()
	{
		return "FloatArrayBuffer{values=" + values + "}";
	}
}
