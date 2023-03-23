package com.alcadrial.renderengine.renderer.screen;

import java.nio.FloatBuffer;

import com.alcadrial.renderengine.buffer.ArrayBuffer;

abstract class DataModel extends ArrayBuffer<FloatBuffer> {
	
	public static final DataModel EMPTY = new EmptyDataModel();
	
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public int byteSize()
	{
		return 0;
	}
	
	@Override
	public void clear()
	{}
	
	@Override
	protected int elementSize()
	{
		return 0;
	}
}
