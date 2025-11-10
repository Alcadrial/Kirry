package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.util.exception.ExternalException;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public interface VulkanFuture<V> {
	
	void await() throws InterruptedException;
	V get() throws VulkanException, InterruptedException;
	
	default VulkanFuture<V> join() throws VulkanException
	{
		try
		{
			get();
		}
		catch (InterruptedException e)
		{
			throw new ExternalException(e);
		}
		return this;
	}
	
	default V forceGet() throws VulkanException
	{
		try
		{
			return get();
		}
		catch (InterruptedException e)
		{
			throw new ExternalException(e);
		}
	}
}
