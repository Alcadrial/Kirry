package com.alcadra.renderengine.util;

public interface Deletable extends AutoCloseable {
	
	@Override
	void close();
}
