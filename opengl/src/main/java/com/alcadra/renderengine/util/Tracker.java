package com.alcadra.renderengine.util;

public interface Tracker<D> {
	
	void modify(D data);
	
	void remove();
	
	boolean isValid();
}
