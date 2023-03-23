package com.alcadra.renderengine.renderer.screen;

import java.nio.FloatBuffer;

import com.alcadra.renderengine.buffer.ArrayBufferUpdate;

abstract class DataModel extends ArrayBufferUpdate<FloatBuffer> {
	
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
