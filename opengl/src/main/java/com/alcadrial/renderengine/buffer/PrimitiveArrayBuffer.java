package com.alcadrial.renderengine.buffer;

import java.nio.ByteBuffer;

public abstract class PrimitiveArrayBuffer extends ArrayBuffer<ByteBuffer> {
	
	private PrimitiveArrayBuffer()
	{}
	
	public void add(byte i)
	{
		addAll(i);
	}
	
	public void add(short i)
	{
		addAll((byte) (i >>> 8), (byte) (i & 255));
	}
	
	public void add(int i)
	{
		addAll((byte) (i >>> 24), (byte) (i >> 16 & 255), (byte) (i >> 8 & 255), (byte) (i & 255));
	}
	
	public void add(float f)
	{
		add(Float.floatToRawIntBits(f));
	}
	
	public void add(long i)
	{
		addAll((byte) (i >>> 56), (byte) (i >> 48 & 255), (byte) (i >> 40 & 255), (byte) (i >> 32 & 255), (byte) (i >> 24 & 255), (byte) (i >> 16 & 255), (byte) (i >> 8 & 255), (byte) (i & 255));
	}
	
	public void add(double f)
	{
		add(Double.doubleToRawLongBits(f));
	}
	
	protected abstract void addAll(byte... bytes);
}
