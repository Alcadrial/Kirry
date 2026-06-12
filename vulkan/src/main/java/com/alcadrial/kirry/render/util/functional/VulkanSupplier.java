package com.alcadrial.kirry.render.util.functional;

import java.util.function.Supplier;

import com.alcadrial.kirry.render.util.exception.VulkanException;

@FunctionalInterface
public interface VulkanSupplier<T> {
	
	static <T> VulkanSupplier<T> of(Supplier<T> supplier)
	{
		return supplier::get;
	}
	
	T get() throws VulkanException;
}
