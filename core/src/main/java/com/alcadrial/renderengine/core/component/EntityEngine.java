package com.alcadrial.renderengine.core.component;

import org.jspecify.annotations.NonNull;

public interface EntityEngine<I extends EngineInitParams, U extends EngineUpdateParams, P extends EngineProperties, E extends Exception> {
	
	void init(@NonNull I params) throws E;
	void update(@NonNull U params) throws E;

	P getProperties();
	void close();
}
