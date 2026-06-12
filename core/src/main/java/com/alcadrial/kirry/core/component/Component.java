package com.alcadrial.kirry.core.component;

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface Component<I, P, E extends Exception> {

	/**Loads data from a resource*/
	CompletableFuture<?> load();
	/**Reloads data from a resource*/
	CompletableFuture<?> reload();
	/**Listen to a resource for updates*/
	boolean listenResource();
	boolean isLoaded();
	/**
	 * May overflow, use only to detect if a reload has happened
	 * @return the count of reloads
	 */
	int reloadCount();
	
	void init(@NonNull I params) throws E;
	void reinit(@NonNull I params) throws E;
	/**
	 * Checks if a resource has been reloaded and revalidates this component if
	 * a non null argument is given.
	 * <p>
	 * If this component has never been initialized then this will do nothing.
	 * 
	 * @return true if validation occurred
	 */
	boolean validate(@Nullable I params) throws E;
	
	P getProperties();
	void close();
}
