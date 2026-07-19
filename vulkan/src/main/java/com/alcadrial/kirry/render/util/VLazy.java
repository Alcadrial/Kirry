package com.alcadrial.kirry.render.util;

import org.jspecify.annotations.NonNull;

import com.alcadrial.kirry.render.VulkanObject;

public class VLazy<T extends VulkanObject> extends Lazy<T> implements AutoCloseable {

	@Override
	public void close()
	{
		if (value != null) value.close();
	}
	
	public void abort()
	{
		if (value != null)
		{
			value.close();
			value = null;
		}
	}
	
	public void abort(@NonNull T oldValue)
	{
		if (value != null)
		{
			value.close();
			value = oldValue;
		}
	}
}
