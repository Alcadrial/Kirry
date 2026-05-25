package com.alcadrial.renderengine.core.component;

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.NonNull;

public abstract class AbstractComponent<I, P, E extends Exception> implements Component<I, P, E> {
	
	protected /*volatile*/ boolean isLoaded = false;
	protected volatile int reloadCounter = 0;

	//TODO: handle reload cancel
	//TODO: handle multiple reload concurrency
	@Override
	public @NonNull CompletableFuture<?> load()
	{
		CompletableFuture<?> future = reload();
		future.whenComplete((o, e) -> isLoaded = e == null);
		return future;
	}
	
	@Override
	public boolean isLoaded()
	{
		return isLoaded;
	}
	
	@Override
	public int reloadCount()
	{
		return reloadCounter;
	}
	
	@Override
	public void init(@NonNull I params) throws E
	{
		reinit(params);
	}
}
