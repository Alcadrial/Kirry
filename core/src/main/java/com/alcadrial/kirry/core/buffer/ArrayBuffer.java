package com.alcadrial.kirry.core.buffer;

public abstract class ArrayBuffer extends SimpleBuffer {
	
	protected boolean isModified = false;
	
	protected ArrayBuffer(int target, int id)
	{
		super(target, id);
	}
	
	public abstract int size();
	
	public int byteSize()
	{
		return size() * elementSize();
	}
	
	public abstract void clear();
	
	protected abstract int elementSize();
	
	public int getGpuSize()
	{
		return getGpuByteSize() / elementSize();
	}
	
	public abstract int getGpuByteSize();
	
	public abstract byte[] getGpuData();
	public abstract int[] getGpuIntData();
	public abstract float[] getGpuFloatData();
}
