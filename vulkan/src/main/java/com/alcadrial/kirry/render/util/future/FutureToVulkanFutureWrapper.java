package com.alcadrial.kirry.render.util.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jspecify.annotations.NonNull;

import com.alcadrial.kirry.render.util.exception.ExternalException;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class FutureToVulkanFutureWrapper<V> implements VulkanFuture<V> {
	
	private Future<V> future;
	
	public FutureToVulkanFutureWrapper(Future<V> future)
	{
		this.future = future;
	}
	
	@Override
	public boolean isDone()
	{
		return future.isDone();
	}
	
	@Override
	public void await() throws InterruptedException
	{
		try
		{
			future.get();
		}
		catch (InterruptedException e)
		{
			throw e;
		}
		catch (ExecutionException _)
		{}
	}
	@Override
	public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException
	{
		try
		{
			future.get(timeout, unit);
		}
		catch (InterruptedException e)
		{
			throw e;
		}
		catch (ExecutionException _)
		{}
	}
	
	@Override
	public V get() throws VulkanException, InterruptedException
	{
		try
		{
			return future.get();
		}
		catch (ExecutionException e)
		{
			throw new ExternalException((@NonNull Throwable) e.getCause());
		}
	}
	
	@Override
	public V get(long timeout, TimeUnit unit) throws VulkanException, InterruptedException, TimeoutException
	{
		try
		{
			return future.get(timeout, unit);
		}
		catch (ExecutionException e)
		{
			throw new ExternalException((@NonNull Throwable) e.getCause());
		}
	}
}
