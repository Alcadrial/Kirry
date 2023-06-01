package com.alcadrial.renderengine.render.util;

public interface Deletable extends AutoCloseable {
	
	@Override
	void close();
}
