package com.alcadrial.renderengine.render.util.functional;

import java.util.function.BiConsumer;

import com.alcadrial.renderengine.render.util.exception.VulkanException;

@FunctionalInterface
public interface VulkanBiConsumer<T, U> {
	
	static <T, U> VulkanBiConsumer<T, U> of(BiConsumer<T, U> consumer)
	{
		return consumer::accept;
	}
	
	void accept(T t, U u) throws VulkanException;
}
