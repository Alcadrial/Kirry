package com.alcadrial.kirry.render.util;

import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.VulkanObject;

public class VLazy<T extends VulkanObject> extends Lazy<T> implements AutoCloseable {
	
	public VLazy()
	{
		
	}
	
	public VLazy(@Nullable Consumer<Boolean> onSet)
	{
		super(onSet);
	}
	
	public void abortAndSet(@NonNull T value)
	{
		abort();
		super.set(value);
	}
	
	@Override
	public void close()
	{
		if (value != null) value.close();
	}
	
	@Override
	public void abort()
	{
		if (value != null)
		{
			value.close();
			value = null;
		}
	}
	
	@Override
	public void abort(@NonNull T oldValue)
	{
		if (value != null)
		{
			value.close();
			value = oldValue;
		}
	}
}
