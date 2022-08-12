package com.alcadra.renderengine.renderer.screen;

import java.nio.FloatBuffer;

import com.alcadra.renderengine.buffer.ArrayBuffer;

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
	protected FloatBuffer nativeBuffer()
	{
		return null;
	}
	
	@Override
	protected FloatBuffer nativeGlBuffer()
	{
		return null;
	}
	
	@Override
	protected void syncRewrite(FloatBuffer buffer)
	{}
	
	@Override
	protected void syncModify(FloatBuffer buffer, int from, int to)
	{}
	
	@Override
	protected boolean equals(FloatBuffer buffer, FloatBuffer other)
	{
		return false;
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
