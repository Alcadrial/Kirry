package com.alcadrial.kirry.render.util.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;


public class VulkanFutureToFutureWrapper<V> implements Future<V> {
	
	private VulkanFuture<V> future;

	public VulkanFutureToFutureWrapper(VulkanFuture<V> future)
	{
		this.future = future;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning)
	{
		return false;
	}
	
	@Override
	public boolean isCancelled()
	{
		return false;
	} 
	
	@Override
	public boolean isDone()
	{
		return future.isDone();
	}
	
	@Override
	public V get() throws InterruptedException, ExecutionException
	{
		try
		{
			return future.get();
		}
		catch (VulkanException e)
		{
			throw new ExecutionException(e);
		}
	}
	
	@Override
	public V get(long timeout, @Nullable TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		try
		{
			return future.get(timeout, VulkanErrorUtils.nonNull(unit));
		}
		catch (VulkanException e)
		{
			throw new ExecutionException(e);
		}
	}
}
