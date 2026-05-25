package com.alcadrial.renderengine.render.util.future;

import java.util.concurrent.Callable;

import com.alcadrial.renderengine.render.util.exception.ExternalException;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class AsyncSupplierTaskHandler<V> extends AsyncTaskHandler<V> {
	
	private Callable<V> action;
	
	public AsyncSupplierTaskHandler(Callable<V> action)
	{
		this.action = action;
		start();
	}
	
	@Override
	protected V execute() throws VulkanException
	{
		try
		{
			return action.call();
		}
		catch (Exception e)
		{
			throw new ExternalException(e);
		}
	}
}