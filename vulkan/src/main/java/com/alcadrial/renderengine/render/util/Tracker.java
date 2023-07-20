package com.alcadrial.renderengine.render.util;

public interface Tracker<D> {
	
	@SuppressWarnings("unchecked")
	static <D> Tracker<D> nullTracker()
	{
		return (Tracker<D>) NullTracker.INSTANCE;
	}
	
	void modify(D data);
	void flush();
	
	void remove();
	
	boolean isValid();
}
