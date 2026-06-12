package com.alcadrial.kirry.render.util.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.util.exception.ExternalException;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public abstract class AsyncTaskHandler<V> implements VulkanFuture<V> {
	
	private CountDownLatch termination;
	@Nullable
	private V result;
	@Nullable
	private VulkanException exception;
	private boolean isDone;
	private StackTraceElement[] callerStackTrace;
	
	public AsyncTaskHandler()
	{
		termination = new CountDownLatch(1);
		result = null;
		callerStackTrace = new Exception().getStackTrace();
	}
	
	protected void start()
	{
		if (KirryVulkan.isDebug()) Thread.ofVirtual().name("Task thread").start(this::run);
		else Thread.startVirtualThread(this::run);
	}
	
	protected abstract V execute() throws VulkanException;
	
	private void run()
	{
		try
		{
			result = execute();
		}
		catch (VulkanException e)
		{
			StackTraceElement[] errorStackTrace = e.getStackTrace();
			StackTraceElement[] stackTrace = new StackTraceElement[errorStackTrace.length + callerStackTrace.length - 2];
			System.arraycopy(errorStackTrace, 0, stackTrace, 0, errorStackTrace.length);
			System.arraycopy(callerStackTrace, 2, stackTrace, errorStackTrace.length, callerStackTrace.length - 2);
			e.setStackTrace(stackTrace);
			exception = e;
		}
		catch (Throwable e)
		{
			VulkanException exception = new ExternalException(e);
			StackTraceElement[] stackTrace = new StackTraceElement[callerStackTrace.length - 2];
			System.arraycopy(callerStackTrace, 2, stackTrace, 0, callerStackTrace.length - 2);
			exception.setStackTrace(stackTrace);
			this.exception = exception;
		}
		finally
		{
			isDone = true;
			termination.countDown();
		}
	}
	
	@Override
	public void await() throws InterruptedException
	{
		termination.await();
	}
	
	@Override
	public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException
	{
		if (!termination.await(timeout, unit)) throw new TimeoutException("Execution timed out");
	}
	
	@Override
	public V get() throws VulkanException, InterruptedException
	{
		await();
		if (exception != null) throw exception;
		return result;
	}
	
	@Override
	public V get(long timeout, TimeUnit unit) throws VulkanException, InterruptedException, TimeoutException
	{
		await(timeout, unit);
		if (exception != null) throw exception;
		return result;
	}
	
	@Override
	public boolean isDone()
	{
		return isDone;
	}
}