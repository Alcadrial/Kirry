package com.alcadrial.renderengine.util;

class NullTracker implements Tracker<Object> {
	
	public static final Tracker<Object> INSTANCE = new NullTracker();
	
	private NullTracker()
	{}
	
	@Override
	public void modify(Object data)
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
