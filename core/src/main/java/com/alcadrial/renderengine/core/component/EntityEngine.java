package com.alcadrial.renderengine.core.component;

import org.jspecify.annotations.NonNull;

public interface EntityEngine<I, R, U, P, E extends Exception> {
	
	void init(@NonNull I params) throws E;
	void reinit(@NonNull R params) throws E;
	void update(@NonNull U params) throws E;

	P getProperties();
	void close();
}
