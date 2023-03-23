package com.alcadrial.renderengine.util;

public interface Deletable extends AutoCloseable {
	
	@Override
	void close();
}
