package com.alcadrial.kirry.render.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.util.functional.VulkanSupplier;

public class Lazy<T> implements Supplier<@NonNull T>, VulkanSupplier<@NonNull T> {
	
	@Nullable
	protected T value;
	
	public void set(@NonNull T value)
	{
		this.value = value;
	}
	
	@Override
	public @NonNull T get()
	{
		return VulkanErrorUtils.nonNull(value, "Value not initialized");
	}
	
	public void ifPresent(Consumer<@NonNull T> action)
	{
		if (value != null) action.accept(value);
	}

	public void abort()
	{
		value = null;
	}
}
