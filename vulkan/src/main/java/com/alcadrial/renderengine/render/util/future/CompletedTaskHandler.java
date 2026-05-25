package com.alcadrial.renderengine.render.util.future;

import java.util.concurrent.TimeUnit;

public class CompletedTaskHandler<V> implements VulkanFuture<V> {
	
	public static final VulkanFuture<?> NULL_COMPLETED = new CompletedTaskHandler<>(null);
	
	private V value;
	
	public CompletedTaskHandler(V value)
	{
		this.value = value;
	}
	
	@Override
	public void await()
	{}
	
	@Override
	public void await(long timeout, TimeUnit unit)
	{}
	
	@Override
	public V get()
	{
		return value;
	}
	
	@Override
	public V get(long timeout, TimeUnit unit)
	{
		return value;
	}
	
	@Override
	public boolean isDone()
	{
		return true;
	}
}