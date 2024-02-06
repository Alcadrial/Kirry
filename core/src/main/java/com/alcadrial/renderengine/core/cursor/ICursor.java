package com.alcadrial.renderengine.core.cursor;

public interface ICursor extends AutoCloseable {
	
	void destroy();
	long getId();
}
