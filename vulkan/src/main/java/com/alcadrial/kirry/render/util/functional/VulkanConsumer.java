package com.alcadrial.kirry.render.util.functional;

import java.util.function.Consumer;

import com.alcadrial.kirry.render.util.exception.VulkanException;

@FunctionalInterface
public interface VulkanConsumer<T> {
	
	static <T> VulkanConsumer<T> of(Consumer<T> consumer)
	{
		return consumer::accept;
	}
	
	void accept(T t) throws VulkanException;
}
