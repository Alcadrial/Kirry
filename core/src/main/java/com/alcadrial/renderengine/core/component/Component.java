package com.alcadrial.renderengine.core.component;

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.NonNull;

public interface Component<I, P, E extends Exception> {

	CompletableFuture<?> load();
	CompletableFuture<?> reload();
	boolean isLoaded();
	/**
	 * May overflow, use only to detect if a reload has happened
	 * @return the count of reloads
	 */
	int reloadCount();
	
	void init(@NonNull I params) throws E;
	void reinit(@NonNull I params) throws E;
	void validate(@NonNull I params) throws E;
	
	P getProperties();
	void close();
}
