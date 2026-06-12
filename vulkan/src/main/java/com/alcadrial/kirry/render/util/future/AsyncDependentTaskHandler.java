package com.alcadrial.kirry.render.util.future;

import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.functional.VulkanFunction;

public class AsyncDependentTaskHandler<V, W> extends AsyncTaskHandler<W> {
	
	private VulkanFuture<V> other;
	private VulkanFunction<V, W> action;
	
	public AsyncDependentTaskHandler(VulkanFuture<V> other, VulkanFunction<V, W> action)
	{
		this.other = other;
		this.action = action;
		start();
	}
	
	@Override
	protected W execute() throws VulkanException
	{
		other.join();
		try
		{
			return action.apply(other.get());
		}
		catch (InterruptedException e)
		{
			throw new IllegalStateException("Interrupted after join", e);
		}
	}
}