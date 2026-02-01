package com.alcadrial.renderengine.render.util;

import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.render.RenderEngine;

class NullTracker implements Tracker<Object> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static final Tracker<Object> INSTANCE = new NullTracker();
	
	private NullTracker()
	{}
	
	@Override
	public void modify(@Nullable Object data)
	{}
	
	@Override
	public void flush()
	{}
	
	@Override
	public void remove()
	{}
	
	@Override
	public boolean isValid()
	{
		return false;
	}
}
