package com.alcadrial.kirry.core.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.NonNull;

public abstract class AbstractComponent<I, P, E extends Exception> implements Component<I, P, E> {
	
	private static final VarHandle V;
	
	static
	{
		try
		{
			V = MethodHandles.lookup().findVarHandle(AbstractComponent.class, "isLoaded", Boolean.TYPE);
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}
	
	protected boolean isLoaded = false;
	protected volatile int reloadCounter = 0;
	
	//TODO: handle reload cancel
	//TODO: handle multiple reload concurrency
	@Override
	public @NonNull CompletableFuture<?> load()
	{
		CompletableFuture<?> future = reload();
		future.whenComplete((o, e) -> V.setRelease(e == null));
		return future;
	}
	
	@Override
	public boolean isLoaded()
	{
		return (boolean) V.getAcquire(this);
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
	
	@Override
	public boolean listenResource()
	{
		return false;
	}
}
