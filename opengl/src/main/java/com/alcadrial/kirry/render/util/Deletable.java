package com.alcadrial.kirry.render.util;

public interface Deletable extends AutoCloseable {
	
	@Override
	void close();
}
