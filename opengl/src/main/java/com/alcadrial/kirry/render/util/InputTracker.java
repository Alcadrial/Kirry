package com.alcadrial.kirry.render.util;

import com.alcadrial.kirry.render.buffer.FloatArrayBuffer;

public class InputTracker implements Tracker<int[]> {
	
	private int index;
	private FloatArrayBuffer buffer;
	private int size;
	private boolean valid;
	
	public InputTracker(int index, FloatArrayBuffer buffer, int size)
	{
		this.index = index;
		this.buffer = buffer;
		this.size = size;
		valid = true;
	}
	
	@Override
	public void modify(int[] data)
	{
		if (valid)
		{
			int i = 0;
			for (; i < data.length && i < size; i++) buffer.setFloat(i + index, Float.intBitsToFloat(data[i]));
			for (; i < size; i++) buffer.setFloat(i + index, 0);
		}
	}
	
	@Override
	public void remove()
	{
		if (valid) for (int i = index; i < size + index; i++) buffer.setFloat(i, 0);
	}
	
	@Override
	public boolean isValid()
	{
		return valid;
	}
}