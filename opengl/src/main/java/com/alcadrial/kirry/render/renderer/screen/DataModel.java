package com.alcadrial.kirry.render.renderer.screen;

import com.alcadrial.kirry.render.buffer.GlBuffer;

abstract class DataModel extends GlBuffer {
	
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
