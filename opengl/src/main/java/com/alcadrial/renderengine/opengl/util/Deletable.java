package com.alcadrial.renderengine.opengl.util;

public interface Deletable extends AutoCloseable {
	
	@Override
	void close();
}
