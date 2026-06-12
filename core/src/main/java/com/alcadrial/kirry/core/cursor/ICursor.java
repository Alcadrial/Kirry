package com.alcadrial.kirry.core.cursor;

public interface ICursor extends AutoCloseable {
	
	void destroy();
	long getId();
}
