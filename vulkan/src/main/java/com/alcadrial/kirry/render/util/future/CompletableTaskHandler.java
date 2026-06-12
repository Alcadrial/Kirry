package com.alcadrial.kirry.render.util.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.util.exception.VulkanException;

public class CompletableTaskHandler<V> implements VulkanFuture<V> {
	
	private CountDownLatch termination;
	@Nullable
	private VulkanException exception;
	@Nullable
	private V value;
	private boolean isDone;
	
	public CompletableTaskHandler()
	{
		termination = new CountDownLatch(1);
		isDone = false;
	}
	
	public void complete(V value)
	{
		if (!isDone)
		{
			this.value = value;
			isDone = true;
			termination.countDown();
		}
	}
	
	public void throwComplete(VulkanException exception)
	{
		if (!isDone)
		{
			this.exception = exception;
			isDone = true;
			termination.countDown();
		}
	}
	
	@Override
	public void await() throws InterruptedException
	{
		if (!isDone) termination.await();
	}
	
	@Override
	public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException
	{
		if (!isDone) termination.await(timeout, unit);
	}
	
	@Override
	public V get() throws VulkanException, InterruptedException
	{
		await();
		if (exception != null) throw exception;
		return value;
	}
	@Override
	public V get(long timeout, TimeUnit unit) throws VulkanException, InterruptedException, TimeoutException
	{
		await(timeout, unit);
		if (exception != null) throw exception;
		return value;
	}

	@Override
	public boolean isDone()
	{
		return isDone;
	}
}